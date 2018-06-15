package ru.mail.colloquium.ui.main.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.mail.colloquium.R;
import ru.mail.colloquium.api.model.GsonResponse;
import ru.mail.colloquium.diagnostics.DebugUtils;
import ru.mail.colloquium.model.types.Profile;
import ru.mail.colloquium.service.fcm.FcmRegistrationService;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.phonenumbers.PhoneNumberUtils;
import ru.mail.colloquium.ui.ContactsActivity;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.settings.SettingsActivity;

import static ru.mail.colloquium.App.api;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.prefs;

public class ProfileFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.info) TextView info;
    @BindView(R.id.contacts) TextView contacts;

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
        info.setText(
                "phone: " + PhoneNumberUtils.formatPhone(profile.phone) + "\n" +
                        "serverId: " + profile.serverId + "\n" +
                        "age: " + profile.age + "\n" +
                        "gender: " + profile.gender + "\n" +
                        "createdAt: " + new Date(profile.createdAt) + "\n" +
                        "updatedAt: " + new Date(profile.updatedAt) + "\n");

        contacts.setPaintFlags(contacts.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.import_db, R.id.reset, R.id.copy_fcm, R.id.settings, R.id.contacts})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.contacts:
                startActivity(new Intent(getActivity(), ContactsActivity.class));
                break;
            case R.id.import_db:
                DebugUtils.importFile(getActivity());
                break;

            case R.id.reset:
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
                break;

            case R.id.copy_fcm:
                ClipboardManager clipboardManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    clipboardManager = Objects.requireNonNull(getActivity()).getSystemService(ClipboardManager.class);
                } else {
                    clipboardManager = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                }
                ClipData data = ClipData.newPlainText("Colloquium FCM token", FcmRegistrationService.getFcmToken());
                clipboardManager.setPrimaryClip(data);
                break;

            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
    }
}
