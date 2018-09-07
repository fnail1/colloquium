package app.laiki;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.Serializable;

import app.laiki.toolkit.concurrent.ThreadPool;

import static app.laiki.App.prefs;

public class Configuration implements Serializable, OnCompleteListener<Void> {
    private final transient FirebaseRemoteConfig remoteConfig;

    public final Notifications notifications = new Notifications();
    public int questionsFrameSize = BuildConfig.DEBUG ? 5 : 12;
    public int deadTime = BuildConfig.DEBUG ? 30 * 1000 : 60 * 60 * 1000;
    public int retentionAlertTime = BuildConfig.DEBUG ? 5 * 60 * 1000 : 24 * 60 * 60 * 1000;
    public int inviteTrigger = BuildConfig.DEBUG ? 3 : 8;
    public boolean emulateSlowConnection = false;
    public String fcmToken;
    public String contestLink = null;

    public Configuration() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
    }

    public void requestRemoteConfig() {
        remoteConfig.fetch(60 * 60).addOnCompleteListener(this);
    }


    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            remoteConfig.activateFetched();
            String contestLink = remoteConfig.getString("show_contest_link");
            if (!TextUtils.equals(this.contestLink, contestLink)) {
                this.contestLink = contestLink;
                ThreadPool.UI.post(() -> prefs().save(this));
            }
        }
    }

    public static class Notifications {
        public boolean answers = true;
        public boolean alerts = true;
    }
}
