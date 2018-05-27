package ru.mail.colloquium.ui.login;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.utils.Utils;

public class LoginPage1IntroViewHolder {

    private final View root;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.subtitle) TextView subtitle;
    @BindView(R.id.button) TextView button;

    public LoginPage1IntroViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        activity.onContinue();
    }
}
