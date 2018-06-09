package ru.mail.colloquium.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.Configuration;
import ru.mail.colloquium.R;
import ru.mail.colloquium.ui.base.BaseActivity;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.prefs;

public class SettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.notifications) Switch notifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        notifications.setOnCheckedChangeListener(this);
    }


    @OnClick({R.id.back, R.id.logout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.logout:
                app().logout();
                finish();
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
