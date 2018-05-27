package ru.mail.colloquium.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.utils.Utils;

public class LoginPage4GenderViewHolder {

    private final View root;
    @BindView(R.id.male) TextView male;
    @BindView(R.id.female) TextView female;


    public LoginPage4GenderViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_4_gender, parent, false));
    }

    public LoginPage4GenderViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick({R.id.male, R.id.female})
    public void onViewClicked(View view) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        switch (view.getId()) {
            case R.id.male:
                activity.onGenderResolved(Gender.MALE);
                break;
            case R.id.female:
                activity.onGenderResolved(Gender.FEMALE);
                break;
        }
    }
}
