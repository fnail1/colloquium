package ru.mail.colloquium.ui.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.BuildConfig;
import ru.mail.colloquium.Configuration;
import ru.mail.colloquium.R;
import ru.mail.colloquium.diagnostics.DebugUtils;
import ru.mail.colloquium.service.fcm.FcmRegistrationService;
import ru.mail.colloquium.ui.base.BaseActivity;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class SettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.notifications) Switch notifications;
    @BindView(R.id.line1) View line1;
    @BindView(R.id.import_db) TextView importDb;
    @BindView(R.id.copy_fcm) TextView copyFcm;
    @BindView(R.id.version) TextView version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        notifications.setOnCheckedChangeListener(this);
        notifications.setChecked(prefs().config().notifications.answer);

        if (!BuildConfig.DEBUG) {
            line1.setVisibility(View.GONE);
            importDb.setVisibility(View.GONE);
            copyFcm.setVisibility(View.GONE);
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String ver = "Версия: " + pInfo.versionName + " (" + pInfo.versionCode + ")";
            version.setText(ver);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version.setVisibility(View.GONE);
            safeThrow(e);
        }
    }


    @OnClick({R.id.back, R.id.logout, R.id.import_db, R.id.copy_fcm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.logout:
                app().logout();
                finish();
                break;
            case R.id.import_db:
                DebugUtils.importFile(this);
                break;
            case R.id.copy_fcm:
                ClipboardManager clipboardManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    clipboardManager = getSystemService(ClipboardManager.class);
                } else {
                    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                }
                ClipData data = ClipData.newPlainText("Colloquium FCM token", FcmRegistrationService.getFcmToken());
                clipboardManager.setPrimaryClip(data);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Configuration config = prefs().config();
        config.notifications.answer = isChecked;
        prefs().save(config);
    }
}
