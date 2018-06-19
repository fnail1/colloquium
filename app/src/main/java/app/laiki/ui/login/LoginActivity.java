package app.laiki.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import app.laiki.R;
import app.laiki.api.model.GsonProfileResponse;
import app.laiki.model.types.Age;
import app.laiki.model.types.Gender;
import app.laiki.model.types.Profile;
import app.laiki.service.MergeHelper;
import app.laiki.ui.ReqCodes;
import app.laiki.ui.base.BaseActivity;
import app.laiki.ui.main.MainActivity;

import static app.laiki.App.app;
import static app.laiki.App.statistics;

public class LoginActivity extends BaseActivity {

    private static long startTime = SystemClock.elapsedRealtime() >> 3;
    private static int[] PAGES = {
            R.layout.fr_login_1_intro,
            R.layout.fr_login_2_phone,
            R.layout.fr_login_3_code,
            R.layout.fr_login_4_gender,
            R.layout.fr_login_5_education,
            R.layout.fr_login_6_high,
            R.layout.fr_login_7_permission
    };

    private static final String STATE_PAGE = "page";
    private String phone;
    String accessToken;
    Profile profile = new Profile();

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
        else
            statistics().login().start();

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

    @NonNull
    private LoginPageViewHolder inflatePage(FrameLayout parent, int page) {
        int viewType = PAGES[page];
        return inflatePage(parent, page, viewType);
    }

    @NonNull
    private LoginPageViewHolder inflatePage(FrameLayout parent, int page, int viewType) {
        parent.removeAllViews();
        View view = LayoutInflater.from(this).inflate(viewType, parent);
        LoginPageViewHolder pageViewHolder;
        switch (viewType) {
            case R.layout.fr_login_1_intro:
                pageViewHolder = new LoginPage1IntroViewHolder(view);
                break;
            case R.layout.fr_login_2_phone:
                pageViewHolder = new LoginPage2PhoneViewHolder(view, phone);
                break;
            case R.layout.fr_login_3_code:
                pageViewHolder = new LoginPage3CodeViewHolder(view, phone);
                break;
            case R.layout.fr_login_4_gender:
                pageViewHolder = new LoginPage4GenderViewHolder(view);
                break;
            case R.layout.fr_login_5_education:
                pageViewHolder = new LoginPage5EducationViewHolder(view);
                break;
            case R.layout.fr_login_6_secondary:
                pageViewHolder = new LoginPage6EducationViewHolder(view);
                break;
            case R.layout.fr_login_6_high:
                pageViewHolder = new LoginPage6EducationViewHolder(view);
                break;
            case R.layout.fr_login_7_permission:
                pageViewHolder = new LoginPage7PermissionViewHolder(view);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(viewType));
        }
        currentPage = page;
        return pageViewHolder;
    }

    public void onContinue() {
        if (currentPage == PAGES.length - 1) {
            onComplete();
            return;
        }

        LoginPageViewHolder pageViewHolder = inflatePage(background, currentPage + 1);

        animateTransition(pageViewHolder, -foreground.getWidth(), background.getWidth());
    }

    private void animateTransition(LoginPageViewHolder pageViewHolder, int foregroundEnd, int backgroundStart) {
        foreground.animate()
                .translationX(foregroundEnd)
                .setDuration(350);

        background.setVisibility(View.VISIBLE);
        background.setTranslationX(backgroundStart);
        background.animate()
                .translationX(0)
                .setDuration(350)
                .withEndAction(() -> {
                    FrameLayout t = this.foreground;
                    foreground = background;
                    background = t;
                    background.setVisibility(View.GONE);
                    background.removeAllViews();
                    pageViewHolder.onShow();
                });
    }

    public void onPhone(String phone) {
        this.phone = phone;
        onContinue();
    }

    public void onSmsCode(String accessToken, GsonProfileResponse.GsonUser profile) {
        this.accessToken = accessToken;
        MergeHelper.merge(this.profile, profile);

        if (profile.education != null && profile.sex != null) {
            LoginPageViewHolder pageViewHolder = inflatePage(background, 6);
            animateTransition(pageViewHolder, -foreground.getWidth(), background.getWidth());
        } else {
            onContinue();
        }
    }

    private void onComplete() {
        app().onLogin(accessToken, this.profile);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onGenderResolved(Gender gender) {
        profile.gender = gender;
        onContinue();
    }

    public void onAgeResolved(Age age) {
        profile.age = age;
        onContinue();
    }

    public void onBack() {
        if (currentPage == 0) {
            finish();
            return;
        }

        LoginPageViewHolder pageViewHolder = inflatePage(background, currentPage - 1);
        animateTransition(pageViewHolder, foreground.getWidth(), -background.getWidth());
    }

    public void onPermissionGranted() {
        onContinue();
    }

    public void onAgeStage1(AgeStrage1 strage) {
        LoginPageViewHolder pageViewHolder;
        switch (strage) {
            case SECONDARY:
                pageViewHolder = inflatePage(background, 5, R.layout.fr_login_6_secondary);
                break;
            case HIGH:
                pageViewHolder = inflatePage(background, 5, R.layout.fr_login_6_high);
                break;
            default:
                throw new IllegalArgumentException("" + strage);
        }
        animateTransition(pageViewHolder, -foreground.getWidth(), background.getWidth());
    }

    public interface LoginPageViewHolder {
        default void onShow() {
        }
    }

    public enum AgeStrage1 {
        SECONDARY, HIGH
    }
}
