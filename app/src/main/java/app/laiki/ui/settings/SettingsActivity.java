package app.laiki.ui.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import app.laiki.BuildConfig;
import app.laiki.Configuration;
import app.laiki.R;
import app.laiki.api.ApiSet;
import app.laiki.diagnostics.DebugUtils;
import app.laiki.service.fcm.FcmRegistrationService;
import app.laiki.toolkit.phonenumbers.PhoneNumberUtils;
import app.laiki.ui.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.app;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.toolkit.collections.Query.query;

public class SettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.notifications) Switch notifications;
    @BindView(R.id.line1) View line1;
    @BindView(R.id.import_db) TextView importDb;
    @BindView(R.id.copy_fcm) TextView copyFcm;
    @BindView(R.id.version) TextView version;
    @BindView(R.id.api) Spinner api;
    @BindView(R.id.questions_frame_size) EditText questionsFrameSize;
    @BindView(R.id.questions_dead_time) EditText questionsDeadTime;
    @BindView(R.id.invite_threshold) EditText inviteThreshold;
    @BindView(R.id.questions_frame_size_container) LinearLayout questionsFrameSizeContainer;
    @BindView(R.id.questions_dead_time_container) LinearLayout questionsDeadTimeContainer;
    @BindView(R.id.invite_threshold_container) LinearLayout inviteThresholdContainer;

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
            api.setVisibility(View.GONE);
            questionsFrameSizeContainer.setVisibility(View.GONE);
            questionsDeadTimeContainer.setVisibility(View.GONE);
            inviteThresholdContainer.setVisibility(View.GONE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_item, query(ApiSet.values()).select(Enum::name).toList());
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            api.setAdapter(adapter);
            api.setSelection(prefs().getApiSet().ordinal());
            api.setPrompt(prefs().getApiSet().name());
            api.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    app().setApiSet(ApiSet.values()[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            setupQuestionsFrameSize();
            setupQuestionsDeadTime();
            setupInviteThreshold();
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

//        safeThrow(new Exception("test"));
    }

    private void setupQuestionsFrameSize() {
        questionsFrameSize.setText(String.valueOf(prefs().config().questionsFrameSize));
        questionsFrameSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String digits = PhoneNumberUtils.digitsOnly(s.toString());
                if (digits == null)
                    return;
                Configuration config = prefs().config();
                config.questionsFrameSize = Integer.parseInt(digits);
                prefs().save(config);
            }
        });
    }

    private void setupQuestionsDeadTime() {
        questionsDeadTime.setText(String.valueOf(prefs().config().deadTime));
        questionsDeadTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String digits = PhoneNumberUtils.digitsOnly(s.toString());
                if (digits == null)
                    return;
                Configuration config = prefs().config();
                config.deadTime = Integer.parseInt(digits);
                prefs().save(config);
            }
        });
    }

    private void setupInviteThreshold() {
        inviteThreshold.setText(String.valueOf(prefs().config().inviteTrigger));
        inviteThreshold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String digits = PhoneNumberUtils.digitsOnly(s.toString());
                if (digits == null)
                    return;
                Configuration config = prefs().config();
                config.inviteTrigger = Integer.parseInt(digits);
                prefs().save(config);
            }
        });
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
