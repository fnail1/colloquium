package ru.mail.colloquium.ui.login;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import ru.mail.colloquium.R;
import ru.mail.colloquium.api.model.GsonAuth;
import ru.mail.colloquium.api.model.GsonProfileResponse;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.http.ServerException;
import ru.mail.colloquium.utils.Utils;

import static ru.mail.colloquium.App.api;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class LoginPage3CodeViewHolder implements LoginActivity.LoginPageViewHolder, TextView.OnEditorActionListener {

    private final View root;
    private final String phone;
    @BindView(R.id.code_explanation) TextView codeExplanation;
    @BindView(R.id.code_title) TextView codeTitle;
    @BindView(R.id.code_edit) EditText codeEdit;
    @BindView(R.id.code_underline) View codeUnderline;
    @BindView(R.id.code_repeat) ImageView codeRepeat;
    @BindView(R.id.code_repeat_countdown) TextView codeRepeatCountdown;
    @BindView(R.id.code_container) RelativeLayout codeContainer;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.button) TextView button;


    public LoginPage3CodeViewHolder(ViewGroup parent, String phone) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_3_code, parent, false), phone);
    }

    public LoginPage3CodeViewHolder(View root, String phone) {
        this.root = root;
        this.phone = phone;
        ButterKnife.bind(this, root);
        codeEdit.setOnEditorActionListener(this);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        codeContainer.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);


        String code = codeEdit.getText().toString();

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.HIGH).execute(new Runnable() {
            @Override
            public void run() {
                GsonAuth authBbody = null;
                GsonProfileResponse.GsonUser profileBody = null;
                try {
                    Response<GsonAuth> authResponse = api().auth(phone, code).execute();
                    if (authResponse.code() != HttpURLConnection.HTTP_OK) {
                        throw new ServerException(authResponse);
                    }
                    authBbody = authResponse.body();
                    if (authBbody == null) {
                        throw new ServerException(200, "authBbody is null");
                    }

                    Response<GsonProfileResponse.GsonUser> profileResponse = api().getProfile("Bearer " + authBbody.token).execute();
                    if (profileResponse.code() != HttpURLConnection.HTTP_OK) {
                        throw new ServerException(authResponse);
                    }

                    profileBody = profileResponse.body();
                    if (profileBody == null) {
                        throw new ServerException(200, "profileBody is null");
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServerException e) {
                    safeThrow(e);
                }
                LoginActivity activity = (LoginActivity) Utils.getActivity(root);
                if (activity == null)
                    return;

                GsonAuth finalAuthBbody = authBbody;
                GsonProfileResponse.GsonUser finalProfileBody = profileBody;

                activity.runOnUiThread(() -> {
                    if (finalAuthBbody != null) {
                        activity.onSmsCode(finalAuthBbody.token, finalProfileBody);
                    } else {
                        Toast.makeText(activity, "Что-то пошло не так, но мне некогда разбираться", Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });

    }

    @Override
    public void onShow() {
        Utils.showKeyboard(codeEdit);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onViewClicked();
            return true;
        }
        return false;
    }
}
