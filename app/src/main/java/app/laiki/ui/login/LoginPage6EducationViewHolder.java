package app.laiki.ui.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit2.Response;
import app.laiki.R;
import app.laiki.api.model.GsonProfileResponse;
import app.laiki.model.types.Age;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.utils.Utils;

import static app.laiki.App.api;
import static app.laiki.api.ApiService.serialize;

public class LoginPage6EducationViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;


    public LoginPage6EducationViewHolder(ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(R.layout.fr_login_6_high, parent, false));
    }

    public LoginPage6EducationViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    @Optional
    @OnClick({R.id.back,
            R.id.grade6, R.id.grade7, R.id.grade8, R.id.grade9, R.id.grade10, R.id.grade11,
            R.id.year1, R.id.year2, R.id.year3, R.id.year4, R.id.year5, R.id.year6})
    public void onViewClicked(View view) {

        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;
        switch (view.getId()) {
            case R.id.back:
                activity.onBack();
                break;
            case R.id.grade6:
                onAgeResolved(activity, Age.GRADE6);
                break;
            case R.id.grade7:
                onAgeResolved(activity, Age.GRADE7);
                break;
            case R.id.grade8:
                onAgeResolved(activity, Age.GRADE8);
                break;
            case R.id.grade9:
                onAgeResolved(activity, Age.GRADE9);
                break;
            case R.id.grade10:
                onAgeResolved(activity, Age.GRADE10);
                break;
            case R.id.grade11:
                onAgeResolved(activity, Age.GRADE11);
                break;
            case R.id.year1:
                onAgeResolved(activity, Age.YEAR1);
                break;
            case R.id.year2:
                onAgeResolved(activity, Age.YEAR2);
                break;
            case R.id.year3:
                onAgeResolved(activity, Age.YEAR3);
                break;
            case R.id.year4:
                onAgeResolved(activity, Age.YEAR4);
                break;
            case R.id.year5:
                onAgeResolved(activity, Age.YEAR5);
                break;
            case R.id.year6:
                onAgeResolved(activity, Age.YEAR6);
                break;
        }

    }

    private void onAgeResolved(LoginActivity activity, Age age) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.HIGH).execute(() -> {
            try {
                Response<GsonProfileResponse> response = api().saveProfile("Bearer " + activity.accessToken, null,
                        serialize(age), serialize(activity.profile.gender)).execute();
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    onSuccess(age);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            onError();

        });
    }

    private void onSuccess(Age age) {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;
        activity.runOnUiThread(() -> {
            activity.onAgeResolved(age);
        });
    }

    private void onError() {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            Toast.makeText(activity, R.string.error_common, Toast.LENGTH_SHORT).show();
        });
    }


}
