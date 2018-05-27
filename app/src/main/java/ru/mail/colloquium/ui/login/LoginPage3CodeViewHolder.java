package ru.mail.colloquium.ui.login;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.utils.Utils;

public class LoginPage3CodeViewHolder implements LoginActivity.LoginPageViewHolder, TextView.OnEditorActionListener {

    private final View root;
    @BindView(R.id.code_explanation) TextView codeExplanation;
    @BindView(R.id.code_title) TextView codeTitle;
    @BindView(R.id.code_edit) EditText codeEdit;
    @BindView(R.id.code_underline) View codeUnderline;
    @BindView(R.id.code_repeat) ImageView codeRepeat;
    @BindView(R.id.code_repeat_countdown) TextView codeRepeatCountdown;
    @BindView(R.id.code_container) RelativeLayout codeContainer;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.button) TextView button;


    public LoginPage3CodeViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_3_code, parent, false));
    }

    public LoginPage3CodeViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
        codeEdit.setOnEditorActionListener(this);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        codeContainer.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.postDelayed(() -> {
            LoginActivity activity = (LoginActivity) Utils.getActivity(root);
            if (activity == null)
                return;
            String code = codeEdit.getText().toString();
            activity.onSmsCode(code, "", "", Long.MAX_VALUE);
        }, 1000);
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
    }}
