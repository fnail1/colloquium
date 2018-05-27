package ru.mail.colloquium.ui.login;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.internal.Util;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.utils.Utils;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.toolkit.collections.Query.query;

public class LoginPage2PhoneViewHolder implements LoginActivity.LoginPageViewHolder, TextView.OnEditorActionListener {

    private final View root;
    @BindView(R.id.phone_title) TextView phoneTitle;
    @BindView(R.id.phone_prefix) TextView phonePrefix;
    @BindView(R.id.phone_edit) EditText phoneEdit;
    @BindView(R.id.phone_underline) View phoneUnderline;
    @BindView(R.id.phone_error) TextView phoneError;
    @BindView(R.id.phone_container) RelativeLayout phoneContainer;
    @BindView(R.id.button) TextView button;
    @BindView(R.id.progress) ProgressBar progress;

    public LoginPage2PhoneViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_2_phone, parent, false));
    }

    public LoginPage2PhoneViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
        phoneEdit.setOnEditorActionListener(this);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        String phone = "+7" + phoneEdit.getText().toString();
        phoneContainer.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        progress.postDelayed(() -> {
            LoginActivity activity = (LoginActivity) Utils.getActivity(root);
            activity.onPhone(phone);
        }, 1000);
    }

    @Override
    public void onShow() {
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
}
