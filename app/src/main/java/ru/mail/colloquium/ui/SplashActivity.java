package ru.mail.colloquium.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.main.MainActivity;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
