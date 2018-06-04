package ru.mail.colloquium.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.utils.Utils;

public class LoginPage5EducationViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;


    public LoginPage5EducationViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_5_education, parent, false));
    }

    public LoginPage5EducationViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick({R.id.back, R.id.secondary, R.id.high})
    public void onViewClicked(View view) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        switch (view.getId()) {
            case R.id.back:
                activity.onBack();
                break;
            case R.id.secondary:
                activity.onAgeStage1(LoginActivity.AgeStrage1.SECONDARY);
                break;
            case R.id.high:
                activity.onAgeStage1(LoginActivity.AgeStrage1.HIGH);
                break;
        }
    }

}
