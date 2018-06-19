package app.laiki.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Response;
import app.laiki.App;
import app.laiki.Preferences;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.events.ObservableEvent;

import static app.laiki.App.app;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;


public class AppStateObserver {

    public final ObservableEvent<AppStateEventHandler, AppStateObserver, Void> stateEvent = new ObservableEvent<AppStateEventHandler, AppStateObserver, Void>(this) {
        @Override
        protected void notifyHandler(AppStateEventHandler handler, AppStateObserver sender, Void args) {
            handler.onAppStateChanged();
        }
    };

    public final ObservableEvent<LowMemoryEventHandler, AppStateObserver, Void> lowMemoryEvent = new ObservableEvent<LowMemoryEventHandler, AppStateObserver, Void>(this) {
        @Override
        protected void notifyHandler(LowMemoryEventHandler handler, AppStateObserver sender, Void args) {
            handler.onLowMemory();
        }
    };

    private Activity topActivity;
    private Activity closedActivity;
    public boolean initialized;
    private Runnable onBackgroundTask = this::onBackground;

    public AppStateObserver(App context, Preferences preferences) {
        initialized = true;
    }

    public Activity getTopActivity() {
        return topActivity;
    }

    @UiThread
    public void setTopActivity(@NonNull Activity topActivity) {
        trace("%s", topActivity);
        if (closedActivity != null) {
            closedActivity = null;
            ThreadPool.UI.removeCallbacks(onBackgroundTask);
        }

        if (this.topActivity != topActivity) {
            this.topActivity = topActivity;
            onStateChanged();
        }
    }

    @UiThread
    public void resetTopActivity(@NonNull Activity activity) {
        trace("%s", activity);
        if (topActivity == activity) {
            closedActivity = activity;
            ThreadPool.UI.postDelayed(onBackgroundTask, 3000);
        }
    }

    @UiThread
    private void onBackground() {
        boolean actual = topActivity == closedActivity;
        trace(Boolean.toString(actual));
        if (actual) {
            topActivity = null;
            closedActivity = null;
            onStateChanged();
        }
    }

    public boolean isForeground() {
        return topActivity != null;
    }

    public void onStateChanged() {
        stateEvent.fire(null);
    }

    public void onLowMemory() {
        // java.lang.IllegalArgumentException: You must call this method on the main thread
        lowMemoryEvent.fire(null);
    }



    public interface AppStateEventHandler {
        void onAppStateChanged();
    }

    public interface LowMemoryEventHandler {
        void onLowMemory();
    }

}
