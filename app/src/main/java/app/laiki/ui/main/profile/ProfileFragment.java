package app.laiki.ui.main.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import app.laiki.BuildConfig;
import app.laiki.R;
import app.laiki.api.model.GsonResponse;
import app.laiki.model.types.Profile;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.phonenumbers.PhoneNumberUtils;
import app.laiki.ui.ContactsActivity;
import app.laiki.ui.base.BaseFragment;
import app.laiki.ui.settings.SettingsActivity;
import app.laiki.utils.GraphicUtils;

import static app.laiki.App.api;
import static app.laiki.App.data;
import static app.laiki.App.prefs;
import static app.laiki.App.screenMetrics;
import static app.laiki.diagnostics.DebugUtils.safeThrow;

public class ProfileFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.contacts) TextView contacts;
    @BindView(R.id.gender) TextView gender;
    @BindView(R.id.age) TextView age;
    @BindView(R.id.reset) TextView reset;
    @BindView(R.id.phone) TextView phone;
    @BindView(R.id.likes) TextView likes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Profile profile = prefs().profile();

        Drawable genderDrawable = GraphicUtils.getDrawable(getActivity(), profile.gender.iconResId, screenMetrics().icon.width, screenMetrics().icon.height);
        gender.setCompoundDrawablesRelative(null, null, genderDrawable, null);
        age.setText(profile.age.profileNameResId);
        phone.setText(PhoneNumberUtils.formatPhone(profile.phone));
        long likesCount = data().answers.count();
        if (likesCount > 0) {
            likes.setText(String.valueOf(likesCount));
        } else {
            ((View) likes.getParent()).setVisibility(View.GONE);
        }
        if (!BuildConfig.DEBUG) {
            reset.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.reset, R.id.settings, R.id.contacts, R.id.support, R.id.vk})
    public void onViewClicked(View view) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        switch (view.getId()) {
            case R.id.contacts:
                startActivity(new Intent(activity, ContactsActivity.class));
                break;

            case R.id.reset:
                resetAnswers();
                break;
            case R.id.settings:
                startActivity(new Intent(activity, SettingsActivity.class));
                break;

            case R.id.support:
                sendEmail();
                break;
            case R.id.vk:
                break;
        }
    }

    private void resetAnswers() {
        api().resetAnswers().enqueue(new Callback<GsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<GsonResponse> call, @NonNull Response<GsonResponse> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    ThreadPool.DB.execute(() -> {
                        data().questions.deleteAll();
                        FragmentActivity a1 = getActivity();
                        if (a1 == null)
                            return;
                        a1.runOnUiThread(() -> {
                            showToast("Ответы удалены");
                        });
                    });
                } else {
                    showToast("Что-то пошло не так");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GsonResponse> call, @NonNull Throwable t) {
                showToast("Что-то пошло не так");
            }

            private void showToast(String s) {
                FragmentActivity activity = getActivity();
                if (activity == null)
                    return;
                Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmail() {
        String email = "icqdev@mail.com";

        String login = prefs().profile().phone;
        String device = Build.MANUFACTURER + " " + Build.MODEL;

        try {
            PackageInfo pInfo;
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);

            String version = pInfo.packageName + " " + pInfo.versionName + "/" + pInfo.versionCode;
            String template = "Здравствуйте!\n" +
                    "Вопервых хочу вас поблагодарить за прекрасное приложение.\n" +
                    "Пользователь " + login + "\n" +
                    "Устройство " + device + "\n" +
                    "Версия приложения " + version + "\n";


            Intent send = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode(email) +
                    "?subject=" + Uri.encode("WTF, дорогая редакция") +
                    "&body=" + Uri.encode(template);
            Uri uri = Uri.parse(uriText);

            send.setData(uri);
            startActivity(Intent.createChooser(send, "Написать разработчику"));

        } catch (Exception e) {
            safeThrow(e);
        }
    }

}
