package app.laiki.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;

import static app.laiki.diagnostics.DebugUtils.safeThrow;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    private static final String PARAM_PAGE_IDENTIFY_KEY = "page_identify_key";
    private final Context mContext;
    private final ArrayList<TabInfo> tabs = new ArrayList<>();
    private final SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public TabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    public void addTab(String title, Class<?> cls, Bundle args, String key) {
        TabInfo info = new TabInfo(title, cls, args, key);
        tabs.add(info);
    }

    public void addTab(int position, String title, Class<?> cls, Bundle args, String key) {
        TabInfo info = new TabInfo(title, cls, args, key);
        tabs.add(position, info);
    }

    public void setTab(int position, String title, Class<?> cls, Bundle args, String key) {
        TabInfo info = new TabInfo(title, cls, args, key);
        tabs.set(position, info);
    }

    @Override
    public Fragment getItem(int index) {
        TabInfo info = tabs.get(index);
        return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment;
        // далее следует конструкция, которая может помочь в решении http://crashes.to/s/f1e438fe258
        // BadParcelableException: ClassNotFoundException when unmarshalling: android.support.v7.widget.RecyclerView$s
        try {
            fragment = (Fragment) super.instantiateItem(container, position);
        } catch (Exception e) {
            safeThrow(e);
            fragment = (Fragment) super.instantiateItem(container, position);
        }
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getTab(position).title;
    }

    public TabInfo getTab(int position) {
        return tabs.get(position);
    }


    public Fragment getFragment(int index) {
        return registeredFragments.get(index);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        Fragment fragment = (Fragment) object;
        Bundle arguments = fragment.getArguments();
        if (arguments == null)
            return POSITION_UNCHANGED;

        String key = arguments.getString(PARAM_PAGE_IDENTIFY_KEY);
        for (int i = 0; i < tabs.size(); i++) {
            TabInfo tab = tabs.get(i);
            if (tab.key.equals(key))
                return i;
        }

        return POSITION_NONE;

    }

    public class TabInfo {
        public final String title;
        public final Class<?> clss;
        public final Bundle args;
        public final String key;

        TabInfo(String title, Class<?> clss, Bundle args, String key) {
            this.title = title;
            this.clss = clss;
            this.args = args;
            this.key = key;
            this.args.putString(PARAM_PAGE_IDENTIFY_KEY, key);
        }
    }
}
