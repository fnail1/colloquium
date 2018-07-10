package app.laiki.ui.login;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.HttpURLConnection;

import app.laiki.R;
import app.laiki.api.model.GsonResponse;
import app.laiki.utils.PhoneEditListener;
import app.laiki.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static app.laiki.App.api;
import static app.laiki.App.statistics;
import static app.laiki.toolkit.phonenumbers.PhoneNumberUtils.digitsOnly;

public class LoginPage2PhoneViewHolder implements LoginActivity.LoginPageViewHolder, TextView.OnEditorActionListener {

    private final View root;
    @BindView(R.id.title) TextView phoneTitle;
    @BindView(R.id.phone_prefix) TextView phonePrefix;
    @BindView(R.id.phone_edit) EditText phoneEdit;
    @BindView(R.id.phone_error) TextView phoneError;
    @BindView(R.id.button) TextView button;
    @BindView(R.id.progress) ProgressBar progress;

    public LoginPage2PhoneViewHolder(ViewGroup parent, String phone) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_2_phone, parent, false), phone);
    }

    public LoginPage2PhoneViewHolder(View root, String phone) {
        this.root = root;
        ButterKnife.bind(this, root);
        phoneEdit.setOnEditorActionListener(this);
        phoneEdit.addTextChangedListener(new MyPhoneEditListener(phoneEdit));
        if (phone != null && phone.length() > 1) {
            phone = phone.substring(1);
            phoneEdit.setText(phone);
            phoneEdit.setSelection(phoneEdit.length());
        }
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        Utils.hideKeyboard(phoneEdit);
        phoneEdit.clearFocus();

        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        String phone = "7" + digitsOnly(phoneEdit.getText().toString());
//        phoneContainer.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        int buttonVisibility = button.getVisibility();
        button.setVisibility(View.INVISIBLE);

        statistics().login().login();

        api().login(phone).enqueue(new Callback<GsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<GsonResponse> call, @NonNull Response<GsonResponse> response) {
                progress.setVisibility(View.GONE);
                button.setVisibility(buttonVisibility);
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    LoginActivity activity = (LoginActivity) Utils.getActivity(root);
                    if (activity != null) {
                        activity.onPhone(phone);
                    }
                    return;
                }
                onError();
            }

            @Override
            public void onFailure(@NonNull Call<GsonResponse> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                button.setVisibility(buttonVisibility);
                onError();
            }

            private void onError() {
                phoneError.setText(R.string.login_phone_error);
                phoneError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onShow() {
        phoneEdit.clearFocus();
        phoneEdit.requestFocus();
        Utils.showKeyboard(phoneEdit);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onViewClicked();
            return true;
        }
        return false;
    }

    private class MyPhoneEditListener extends PhoneEditListener {

        MyPhoneEditListener(EditText editText) {
            super(editText);
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            int digits = calcDigits(s);
            button.setVisibility(digits == 10 ? View.VISIBLE : View.INVISIBLE);
            phoneError.setVisibility(View.GONE);
        }

        private int calcDigits(Editable s) {
            int digits = 0;
            for (int i = 0; i < s.length(); i++) {
                char currentChar = s.charAt(i);
                if (Character.isDigit(currentChar)) {
                    digits++;
                }
            }
            return digits;
        }
    }
}
