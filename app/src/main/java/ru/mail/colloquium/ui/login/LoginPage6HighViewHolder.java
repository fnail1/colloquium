package ru.mail.colloquium.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.utils.Utils;

public class LoginPage6HighViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;


    public LoginPage6HighViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_6_high, parent, false));
    }

    public LoginPage6HighViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick({R.id.back, R.id.year1, R.id.year2, R.id.year3, R.id.year4, R.id.year5, R.id.year6})
    public void onViewClicked(View view) {

        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;
        switch (view.getId()) {
            case R.id.back:
                activity.onBack();
                break;
            case R.id.year1:
                activity.onAgeResolved(Age.YEAR1);
                break;
            case R.id.year2:
                activity.onAgeResolved(Age.YEAR2);
                break;
            case R.id.year3:
                activity.onAgeResolved(Age.YEAR3);
                break;
            case R.id.year4:
                activity.onAgeResolved(Age.YEAR4);
                break;
            case R.id.year5:
                activity.onAgeResolved(Age.YEAR5);
                break;
            case R.id.year6:
                activity.onAgeResolved(Age.YEAR6);
                break;
        }

    }

}
