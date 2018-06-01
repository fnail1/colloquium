package ru.mail.colloquium.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.utils.Utils;

public class LoginPage5AgeViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;


    public LoginPage5AgeViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_5_age, parent, false));
    }

    public LoginPage5AgeViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick({R.id.back, R.id.skip, R.id.title, R.id.secondary, R.id.grade6, R.id.grade7, R.id.grade8, R.id.grade9, R.id.grade10, R.id.grade11, R.id.institute, R.id.year1, R.id.year2, R.id.year3, R.id.year4, R.id.year5, R.id.year6})
    public void onViewClicked(View view) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;
        switch (view.getId()) {
            case R.id.back:
                activity.onBack();
                break;
            case R.id.skip:
                activity.onAgeResolved(Age.SUPERSTAR);
                break;
            case R.id.grade6:
                activity.onAgeResolved(Age.GRADE6);
                break;
            case R.id.grade7:
                activity.onAgeResolved(Age.GRADE7);
                break;
            case R.id.grade8:
                activity.onAgeResolved(Age.GRADE8);
                break;
            case R.id.grade9:
                activity.onAgeResolved(Age.GRADE9);
                break;
            case R.id.grade10:
                activity.onAgeResolved(Age.GRADE10);
                break;
            case R.id.grade11:
                activity.onAgeResolved(Age.GRADE11);
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
