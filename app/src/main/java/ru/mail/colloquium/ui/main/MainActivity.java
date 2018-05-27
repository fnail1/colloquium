package ru.mail.colloquium.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.ui.ReqCodes;
import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.login.LoginActivity;

import static ru.mail.colloquium.App.prefs;

public class MainActivity extends BaseActivity {

    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.pages) ViewPager pages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

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

    private class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
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
                    return "Мои";
                case 1:
                    return "Контакты";
                case 2:
                    return "Опрос";
                case 3:
                    return "Профиль";
            }
            throw new IllegalArgumentException();
        }
    }
}
