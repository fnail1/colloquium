package app.laiki.ui.main;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.view.View;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.service.AppService;
import app.laiki.ui.AnswerActivity;
import app.laiki.ui.ReqCodes;
import app.laiki.ui.base.BaseActivity;
import app.laiki.ui.login.LoginActivity;
import app.laiki.ui.main.answers.AnswersFragment;
import app.laiki.ui.main.contacts.ContactsFragment;
import app.laiki.ui.main.profile.ProfileFragment;
import app.laiki.ui.main.questions.QuestionsFragment;
import app.laiki.utils.Utils;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.notifications;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class MainActivity extends BaseActivity implements AppService.AnswerUpdatedEventHandler {

    public static final String ACTION_OPEN_ANSWER = "action_open_answer";
    public static final int PAGE_QUESTION = 1;

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.pages) ViewPager pages;

    private TabsTheme tabsTheme;
    private int answersCounter = -1;
    private int answersCounterColor = 0xffffffff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        super.onCreate(savedInstanceState);
        if (!prefs().hasAccount()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermissions(ReqCodes.CONTACTS_PERMISSIONS, R.string.contacts_permission_explanation, Manifest.permission.READ_CONTACTS);

        pages.setAdapter(new MyAdapter(getSupportFragmentManager()));
        pages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float f = position + positionOffset;
                if (.5f < f && f <= 1.5f) {
                    if (tabsTheme == TabsTheme.LIGHT)
                        return;
                    tabsTheme = TabsTheme.LIGHT;
                } else {
                    if (tabsTheme == TabsTheme.DARK)
                        return;
                    tabsTheme = TabsTheme.DARK;
                }

                int colorTitle;
                int colorSubtitle;
                switch (tabsTheme) {
                    case DARK:
                        colorTitle = 0xff546d79;
                        colorSubtitle = 0xff9ab3c0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            View decorView = getWindow().getDecorView();
                            int flags = decorView.getSystemUiVisibility();
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                            decorView.setSystemUiVisibility(flags);
                        }
                        answersCounterColor = 0xffff0000;
                        break;
                    case LIGHT:
                        colorTitle = 0xFFFFFFFF;
                        colorSubtitle = 0xB3FFFFFF;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            View decorView = getWindow().getDecorView();
                            int flags = decorView.getSystemUiVisibility();
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                            decorView.setSystemUiVisibility(flags);
                        }
                        answersCounterColor = 0xffffffff;
                        break;
                    default:
                        safeThrow(new Exception("" + tabsTheme));
                        return;
                }

                tabs.setTabTextColors(colorSubtitle, colorTitle);
                tabs.setSelectedTabIndicatorColor(colorTitle);
            }

            @Override
            public void onPageSelected(int position) {
                trace();
                if(position == PAGE_QUESTION){
                    notifications().clearStopScreenOut();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabs.setupWithViewPager(pages);
        pages.setCurrentItem(1);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        switch (action) {
            case ACTION_OPEN_ANSWER:
                pages.setCurrentItem(0);
                startActivity(new Intent(this, AnswerActivity.class).putExtras(intent));
                break;
        }
    }

    @Override
    protected void onRequestedPermissionsGranted(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestedPermissionsGranted(requestCode, permissions, grantResults);
        switch (ReqCodes.byCode(requestCode)) {
            case CONTACTS_PERMISSIONS:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appService().answerUpdatedEvent.add(this);
        onAnswerUpdated(null);
    }

    @Override
    protected void onPause() {
        appService().answerUpdatedEvent.remove(this);
        super.onPause();
    }

    private CharSequence formatAnswersTitle() {
        String text;
        if (answersCounter < 0)
            answersCounter = data().answers.countUnread();

        trace("%d", answersCounter);


        if (answersCounter == 0)
            return "Мои";

        if (answersCounter > 99)
            text = "Мои (99+)";
        else
            text = "Мои (" + answersCounter + ")";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new AnswersCounterSpan(), 4, text.length(), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public int currentPage() {
        return pages.getCurrentItem();
    }

    private class AnswersCounterSpan extends CharacterStyle {

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setColor(answersCounterColor);
            tp.setTypeface(Typeface.create(tp.getTypeface(), Typeface.BOLD));
        }
    }

    @Override
    public void onAnswerUpdated(List<Answer> args) {
        runOnUiThread(() -> {
            answersCounter = data().answers.countUnread();
            trace("" + answersCounter);
            pages.getAdapter().notifyDataSetChanged();
        });
    }

    private class MyAdapter extends FragmentStatePagerAdapter {
        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AnswersFragment();
                case 1:
                    return new QuestionsFragment();
                case 2:
                    return new ProfileFragment();
                case 3:
                    return new ContactsFragment();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return formatAnswersTitle();
                case 1:
                    return "Вопросы";
                case 2:
                    return "Профиль";
                case 3:
                    return "Контакты";
            }
            throw new IllegalArgumentException();
        }
    }

    private enum TabsTheme {
        DARK, LIGHT
    }
}
