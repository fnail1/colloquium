package app.laiki.ui.login;

import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import app.laiki.R;
import app.laiki.diagnostics.statistics.Statistics;
import app.laiki.ui.ReqCodes;
import app.laiki.utils.Utils;

import static app.laiki.App.statistics;

public class LoginPage7PermissionViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;


    public LoginPage7PermissionViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_7_permission, parent, false));
    }

    public LoginPage7PermissionViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @OnClick(R.id.contacts)
    public void onViewClicked(View view) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;
        statistics().login().permission();
        switch (view.getId()) {
            case R.id.contacts:
                if (activity.requestPermissions(ReqCodes.CONTACTS_PERMISSIONS, R.string.contacts_permission_explanation, Manifest.permission.READ_CONTACTS)) {
                    activity.onPermissionGranted();
                }
                break;
        }
    }
}
