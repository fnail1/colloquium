package ru.mail.colloquium.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.ui.ReqCodes;
import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.main.MainActivity;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.toolkit.collections.Query.query;

public class LoginActivity extends BaseActivity {

    private static long startTime = SystemClock.elapsedRealtime() >> 3;
    private static int[] PAGES = {
            R.layout.fr_login_1_intro,
            R.layout.fr_login_2_phone,
            R.layout.fr_login_3_code,
            R.layout.fr_login_4_gender,
            R.layout.fr_login_5_age,
            R.layout.fr_login_6_permission
    };

    private static final String STATE_PAGE = "page";
    private String phone;
    private String code;
    private String accessToken;
    private String refreshToken;
    private long expireIn;
    private Gender gender;
    private Age age;

    private int getBackgroundColor(int page) {
        int random = (int) ((startTime + page) & 0x000fffff);
        int r = 170 + (random * 33) % 80;
        int g = 170 + (random * 57) % 80;
        int b = 170 + (random * 79) % 80;

        int w = (random >> 2) % 6;
        switch (w) {
            case 0:
                r -= (random * 29) % 120;
                break;
            case 1:
                g -= (random * 29) % 120;
                break;
            case 2:
                b -= (random * 29) % 120;
                break;
            case 3:
                r -= (random * 29) % 120;
                g -= (random * 29) % 120;
                break;
            case 4:
                g -= (random * 29) % 120;
                b -= (random * 29) % 120;
                break;
            case 5:
                r -= (random * 29) % 120;
                b -= (random * 29) % 120;
                break;
        }
        return Color.argb(0xff, r, g, b);
    }


    @BindView(R.id.page1) FrameLayout page1;
    @BindView(R.id.page2) FrameLayout page2;
    private FrameLayout background;
    private FrameLayout foreground;

    private int currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        foreground = page1;
        background = page2;
        if (savedInstanceState != null)
            currentPage = savedInstanceState.getInt(STATE_PAGE);
        inflatePage(foreground, currentPage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGE, currentPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (ReqCodes.byCode(requestCode)) {
            case CONTACTS_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            onContinue();
                        break;
                    }
                }

                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void inflatePage(FrameLayout parent, int page) {
        parent.removeAllViews();
        parent.setBackgroundColor(getBackgroundColor(page));
        int viewType = PAGES[page];
        View view = LayoutInflater.from(this).inflate(viewType, parent);
        switch (viewType) {
            case R.layout.fr_login_1_intro:
                new LoginPage1IntroViewHolder(view);
                break;
            case R.layout.fr_login_2_phone:
                new LoginPage2PhoneViewHolder(view);
                break;
            case R.layout.fr_login_3_code:
                new LoginPage3CodeViewHolder(view);
                break;
            case R.layout.fr_login_4_gender:
                new LoginPage4GenderViewHolder(view);
                break;
            case R.layout.fr_login_5_age:
                new LoginPage5AgeViewHolder(view);
                break;
            case R.layout.fr_login_6_permission:
                new LoginPage6PermissionViewHolder(view);
                break;
        }
        currentPage = page;
    }

    public void onContinue() {
        if (currentPage == PAGES.length - 1) {
            app().onLogin(phone, accessToken, refreshToken, expireIn, gender, age);
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        inflatePage(background, currentPage + 1);

        foreground.animate()
                .translationX(-foreground.getWidth())
                .setDuration(350);

        background.setVisibility(View.VISIBLE);
        background.setTranslationX(background.getWidth());
        background.animate()
                .translationX(0)
                .setDuration(350)
                .withEndAction(() -> {
                    FrameLayout t = this.foreground;
                    foreground = background;
                    background = t;
                    background.setVisibility(View.GONE);
                    background.removeAllViews();
                });
    }

    public void onPhone(String phone) {
        this.phone = phone;
        onContinue();
    }

    public void onSmsCode(String code, String accessToken, String refreshToken, long expireIn) {
        this.code = code;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireIn = expireIn;
        String first = query(databaseList()).first(db -> db.equals(AppData.normalizeDbName(phone)));
        if (first != null) {
            app().onLogin(phone, accessToken, refreshToken, expireIn);
        } else {
            onContinue();
        }
    }

    public void onGenderResolved(Gender gender) {
        this.gender = gender;
        onContinue();
    }

    public void onAgeResolved(Age age) {
        this.age = age;
        onContinue();
    }

    public void onBack() {
        if (currentPage == 0) {
            finish();
            return;
        }

        inflatePage(background, currentPage - 1);

        foreground.animate()
                .translationX(foreground.getWidth())
                .setDuration(350);

        background.setVisibility(View.VISIBLE);
        background.setTranslationX(-background.getWidth());
        background.animate()
                .translationX(0)
                .setDuration(350)
                .withEndAction(() -> {
                    FrameLayout t = this.foreground;
                    foreground = background;
                    background = t;
                    background.setVisibility(View.GONE);
                    background.removeAllViews();
                });
    }

    public void onPermissionGranted() {
        onContinue();
    }
}
