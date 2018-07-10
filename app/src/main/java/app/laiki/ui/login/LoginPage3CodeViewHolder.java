package app.laiki.ui.login;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import app.laiki.R;
import app.laiki.api.model.GsonAuth;
import app.laiki.api.model.GsonProfileResponse;
import app.laiki.api.model.GsonResponse;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.http.ServerException;
import app.laiki.toolkit.phonenumbers.PhoneNumberUtils;
import app.laiki.utils.Utils;

import static app.laiki.App.api;
import static app.laiki.App.statistics;
import static app.laiki.diagnostics.DebugUtils.safeThrow;

public class LoginPage3CodeViewHolder implements LoginActivity.LoginPageViewHolder, TextView.OnEditorActionListener {

    private final View root;
    private final String phone;
    @BindView(R.id.code_explanation) TextView codeExplanation;
    @BindView(R.id.code_edit) EditText codeEdit;
    @BindView(R.id.code_repeat) View codeRepeat;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.button) TextView button;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.back) TextView back;
    @BindView(R.id.code_error) TextView codeError;
    private long startTs;


    public LoginPage3CodeViewHolder(ViewGroup parent, String phone) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_3_code, parent, false), phone);
    }

    public LoginPage3CodeViewHolder(View root, String phone) {
        this.root = root;
        this.phone = phone;
        ButterKnife.bind(this, root);
        codeEdit.setOnEditorActionListener(this);
        codeEdit.addTextChangedListener(new MyTextWatcher());
        String textPhone = PhoneNumberUtils.formatPhone(phone);
        codeExplanation.setText(codeExplanation.getResources().getString(R.string.enter_auth_code_explanation, textPhone));
    }


    private void setViewMode(ViewMode mode) {
        button.setVisibility(mode != ViewMode.CHECK_CODE && codeEdit.length() == 4 ? View.INVISIBLE : View.INVISIBLE);
        title.setVisibility(mode != ViewMode.CHECK_CODE ? View.VISIBLE : View.INVISIBLE);
        codeEdit.setVisibility(mode != ViewMode.CHECK_CODE ? View.VISIBLE : View.INVISIBLE);
        codeRepeat.setVisibility(mode == ViewMode.WAIT_CODE_2 ? View.VISIBLE : View.INVISIBLE);
        codeExplanation.setVisibility(mode != ViewMode.CHECK_CODE ? View.VISIBLE : View.INVISIBLE);
        progress.setVisibility(mode == ViewMode.CHECK_CODE ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onShow() {
        codeEdit.clearFocus();
        codeEdit.postDelayed(() -> {
            codeEdit.requestFocus();
            codeEdit.post(() -> {
                Utils.showKeyboard(codeEdit);
            });
        }, 200);

        setViewMode(ViewMode.WAIT_CODE_1);

        root.postDelayed(() -> {
            setViewMode(ViewMode.WAIT_CODE_2);
            codeRepeat.setAlpha(0f);
            codeRepeat.animate()
                    .setDuration(1000)
                    .alpha(1);
        }, 30 * 1000);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onViewClicked(button);
            return true;
        }
        return false;
    }

    @OnClick({R.id.button, R.id.code_repeat, R.id.back})
    public void onViewClicked(View view) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        switch (view.getId()) {
            case R.id.button:
                onConfirmCode();
                break;
            case R.id.code_repeat:
                onRepeatCode();
                break;
            case R.id.back:
                activity.onBack();
                break;
        }
    }

    private void onRepeatCode() {
        setViewMode(ViewMode.CHECK_CODE);

        api().login(phone).enqueue(new Callback<GsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<GsonResponse> call, @NonNull Response<GsonResponse> response) {
                if (response.code() == HttpURLConnection.HTTP_OK)
                    onShow();
                else
                    onError();
            }

            @Override
            public void onFailure(@NonNull Call<GsonResponse> call, @NonNull Throwable t) {
                onError();
            }

            private void onError() {
                LoginActivity activity = (LoginActivity) Utils.getActivity(root);
                if (activity == null)
                    return;

                setViewMode(ViewMode.WAIT_CODE_2);
                Toast.makeText(activity, "Не узалось отправить запрос", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onConfirmCode() {
        statistics().login().auth();
        setViewMode(ViewMode.CHECK_CODE);

        String code = codeEdit.getText().toString();
//        startTs = SystemClock.elapsedRealtime();

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.HIGH).execute(() -> {
            GsonAuth authBody;
            GsonProfileResponse.GsonUser profileBody;
            try {
                Response<GsonAuth> authResponse = api().auth(phone, code).execute();
                if (authResponse.code() != HttpURLConnection.HTTP_OK) {
                    throw new ServerException(authResponse);
                }
                authBody = authResponse.body();
                if (authBody == null) {
                    throw new ServerException(200, "authBody is null");
                }

                Response<GsonProfileResponse.GsonUser> profileResponse = api().getProfile("Bearer " + authBody.token).execute();
                if (profileResponse.code() != HttpURLConnection.HTTP_OK) {
                    throw new ServerException(authResponse);
                }

                profileBody = profileResponse.body();
                if (profileBody == null) {
                    throw new ServerException(200, "profileBody is null");
                }
                onSuccess(authBody, profileBody);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                onError();
                return;
            } catch (ServerException e) {
                if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    onCodeMismatch();
                    return;
                }
                safeThrow(e);
            }
            onError();

        });
    }

    private void onCodeMismatch() {

        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            setViewMode(ViewMode.WAIT_CODE_2);
            codeError.setVisibility(View.VISIBLE);
            codeExplanation.setVisibility(View.GONE);
            codeEdit.setBackgroundColor(0xffffb68d);
            codeExplanation.setVisibility(View.INVISIBLE);
        });

    }

    private void onError() {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            Toast.makeText(activity, R.string.error_common, Toast.LENGTH_SHORT).show();
            setViewMode(ViewMode.WAIT_CODE_2);
        });
    }

    private void onSuccess(GsonAuth authBody, GsonProfileResponse.GsonUser profileBody) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            activity.onSmsCode(authBody.token, profileBody);
            setViewMode(ViewMode.WAIT_CODE_2);
        });

    }


    private enum ViewMode {
        WAIT_CODE_1, WAIT_CODE_2, CHECK_CODE
    }

    private class MyTextWatcher implements TextWatcher {
        private boolean recursiveProtectionLock = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (recursiveProtectionLock)
                return;
            recursiveProtectionLock = true;

            codeError.setVisibility(View.GONE);
            codeExplanation.setVisibility(View.VISIBLE);
            codeEdit.setBackgroundColor(0xffffffff);
            button.setVisibility(s.length() == 4 ? View.VISIBLE : View.INVISIBLE);

            recursiveProtectionLock = false;

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 4)
                onViewClicked(button);
        }
    }
}
