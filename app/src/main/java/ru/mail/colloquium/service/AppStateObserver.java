package ru.mail.colloquium.service;

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
import ru.mail.colloquium.App;
import ru.mail.colloquium.Preferences;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.events.ObservableEvent;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;
import static ru.mail.colloquium.diagnostics.Logger.trace;


public class AppStateObserver {
    public final ObservableEvent<DateChangedEventHandler, AppStateObserver, Void> dateChangedEvent = new ObservableEvent<DateChangedEventHandler, AppStateObserver, Void>(this) {
        @Override
        protected void notifyHandler(DateChangedEventHandler handler, AppStateObserver sender, Void args) {
            handler.onDateTimeChanged();
        }
    };

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
    private long serverTimeOffset;
    public boolean initialized;
    private boolean serverTimeOffsetDirty;
    private Runnable onBackgroundTask = this::onBackground;

    public AppStateObserver(App context, Preferences preferences) {
        serverTimeOffset = preferences.getServerTimeOffset();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                serverTimeOffsetDirty = true;
                onServerTimeOffsetChanged();
                onStateChanged();
            }
        }, filter);
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

    protected void onServerTimeOffsetChanged() {
        dateChangedEvent.fire(null);
    }

    public long getServerTime() {
        return getServerTime(System.currentTimeMillis());
    }

    public long getServerTime(long currentTimeMillis) {
        return currentTimeMillis + serverTimeOffset;
    }

    public long getLocalTime(long serverTimeMillis) {
        return serverTimeMillis - serverTimeOffset;
    }

    public long adjustServerTimeOffset(Response<?> response) {
        String dateString = response.headers().get("Date");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        try {
            Date date = format.parse(dateString);
            return adjustServerTimeOffset(date.getTime());
        } catch (ParseException e) {
            safeThrow(e);
            return getServerTime();
        }
    }

    public long adjustServerTimeOffset(long time) {
        long localTime = System.currentTimeMillis();
        long offset = time - localTime;
        if (serverTimeOffsetDirty || Math.abs(offset - serverTimeOffset) > 3000) {
            serverTimeOffsetDirty = false;
            serverTimeOffset = offset;
            prefs().setServerTimeOffset(serverTimeOffset);
            onServerTimeOffsetChanged();
        }
        return localTime + serverTimeOffset;
    }

    public boolean serverTimeOffsetDirty() {
        return serverTimeOffsetDirty;
    }

    public long getServerTimeOffset() {
        return serverTimeOffset;
    }

    public long getServerTimeOffsetWithTZ() {
        return -serverTimeOffset + TimeZone.getDefault().getRawOffset();
    }

    public interface AppStateEventHandler {
        void onAppStateChanged();
    }

    public interface LowMemoryEventHandler {
        void onLowMemory();
    }

    public interface DateChangedEventHandler {
        void onDateTimeChanged();
    }
}
