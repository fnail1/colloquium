package ru.mail.colloquium.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.service.AppService;
import ru.mail.colloquium.ui.ReqCodes;
import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.login.LoginActivity;
import ru.mail.colloquium.ui.main.answers.AnswersFragment;
import ru.mail.colloquium.ui.main.contacts.ContactsFragment;
import ru.mail.colloquium.ui.main.profile.ProfileFragment;
import ru.mail.colloquium.ui.main.questions.QuestionsFragment;
import ru.mail.colloquium.utils.Utils;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class MainActivity extends BaseActivity implements AppService.AnswerUpdatedEventHandler {

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.pages) ViewPager pages;
    private TabsTheme tabsTheme;
    private int answersCounter = -1;
    private TextView answersTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                if (1.5f < f && f <= 2.5f) {
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
                        break;
                    case LIGHT:
                        colorTitle = Utils.getColor(MainActivity.this, R.color.colorTitle);
                        colorSubtitle = Utils.getColor(MainActivity.this, R.color.colorSubtitle);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            View decorView = getWindow().getDecorView();
                            int flags = decorView.getSystemUiVisibility();
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                            decorView.setSystemUiVisibility(flags);
                        }
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

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabs.setupWithViewPager(pages);
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
        updateAnswersCounter();
        appService().answerUpdatedEvent.add(this);
    }

    @Override
    protected void onPause() {
        appService().answerUpdatedEvent.remove(this);
        super.onPause();
    }

    private void updateAnswersCounter() {
        if (answersTitle == null) {
            TextView textView = findTitleMy(tabs);
            if (textView == null)
                return;

            answersTitle = textView;
        }

        answersCounter = data().answers.countUnread();
        CharSequence text = formatAnswersTitle();
        answersTitle.setText(text);
    }

    private CharSequence formatAnswersTitle() {
        String text;
        if (answersCounter < 0)
            answersCounter = data().answers.countUnread();

        if (answersCounter == 0)
            return "Мои";
        if (answersCounter > 99)
            text = "Мои <font color='red'><b>(99+)</b></font>";
        else
            text = "Мои <font color='red'><b>(" + answersCounter + ")</b></font>";
        return Html.fromHtml(text);
    }

    private TextView findTitleMy(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (textView.getText().toString().equals("Мои"))
                    return textView;
            } else if (child instanceof ViewGroup) {
                TextView textView = findTitleMy((ViewGroup) child);
                if (textView != null)
                    return textView;
            }
        }
        return null;
    }

    @Override
    public void onAnswerUpdated() {
        runOnUiThread(this::updateAnswersCounter);
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
                    return new ContactsFragment();
                case 2:
                    return new QuestionsFragment();
                case 3:
                    return new ProfileFragment();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return formatAnswersTitle();
                case 1:
                    return "Контакты";
                case 2:
                    return "Вопросы";
                case 3:
                    return "Профиль";
            }
            throw new IllegalArgumentException();
        }
    }

    private enum TabsTheme {
        DARK, LIGHT
    }
}
