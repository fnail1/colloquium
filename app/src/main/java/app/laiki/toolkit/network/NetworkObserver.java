package app.laiki.toolkit.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.UiThread;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.events.ObservableEvent;

import static app.laiki.diagnostics.Logger.trace;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
public class NetworkObserver {
    private BroadcastReceiver mInstance;

    private final ObservableEvent<NetworkStateEventHandler, NetworkObserver, Void> mNetworkStateChangedEvent = new ObservableEvent<NetworkStateEventHandler, NetworkObserver, Void>(this) {
        @Override
        protected void notifyHandler(NetworkStateEventHandler handler, NetworkObserver sender, Void args) {
            handler.onNetworkStateChanged(sender, mNetworkAvailable);
        }
    };

    private final Object mQueueMonitor = new Object();
    private LinkedHashMap<String, Runnable> mQueue;
    private boolean mNetworkAvailable;
    private int mNetworkType = -1;

    public NetworkObserver(Context context) {
        if (mInstance != null) {
            throw new IllegalStateException("Already started");
        }
        mInstance = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onNetworkStateChanged(context);
            }
        };
        onNetworkStateChanged(context);
        context.registerReceiver(mInstance, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void cleanup(Context context) {
        context.unregisterReceiver(mInstance);
        mInstance = null;
        mNetworkAvailable = false;
        mNetworkType = -1;
    }

    public boolean isNetworkAvailable() {
        return mNetworkAvailable;
    }

    public void scheduleTask(String key, Runnable task) {
        trace(key);
        if (mInstance == null)
            return;

        synchronized (mQueueMonitor) {
            if (mQueue == null)
                mQueue = new LinkedHashMap<>();
            mQueue.put(key, task);
        }
    }

    @UiThread
    public boolean unscheduleTask(String key) {
        synchronized (mQueueMonitor) {
            return mQueue != null && mQueue.remove(key) != null;
        }
    }

    public void addNetworkStateEventHandler(NetworkStateEventHandler handler) {
        mNetworkStateChangedEvent.add(handler);
    }

    public void removeNetworkStateEventHandler(NetworkStateEventHandler handler) {
        mNetworkStateChangedEvent.remove(handler);
    }

    public boolean waitForOnline(@SuppressWarnings("SameParameterValue") int timeout) throws InterruptedException {
        if (ThreadPool.isUiThread())
            throw new IllegalStateException("Do not lock UI-thread!");

        if (isNetworkAvailable())
            return true;

        final CountDownLatch latch = new CountDownLatch(1);
        NetworkStateEventHandler handler = (playerService, online) -> {
            if (online)
                latch.countDown();
        };
        addNetworkStateEventHandler(handler);
        try {
            if (timeout > 0)
                return latch.await(timeout, TimeUnit.MILLISECONDS);

            latch.await();
            return true;
        } finally {
            removeNetworkStateEventHandler(handler);
        }
    }

    public boolean waitForOnline() throws InterruptedException {
        return waitForOnline(0);
    }

    private void onNetworkStateChanged(Context context) {
        trace(context != null ? "system" : "manual");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            mNetworkAvailable = false;
            mNetworkType = -1;
            return;
        }

        mNetworkAvailable = netInfo.isAvailable();
        mNetworkType = netInfo.getType();
        mNetworkStateChangedEvent.fire(null);

        Iterator<Map.Entry<String, Runnable>> iterator;
        synchronized (mQueueMonitor) {
            if (mQueue == null || !mNetworkAvailable)
                return;
            iterator = mQueue.entrySet().iterator();
            mQueue = null;
        }

        if (iterator.hasNext()) {
            do {
                Map.Entry<String, Runnable> entry = iterator.next();

                trace("%s", entry.getKey());

                entry.getValue().run();
            }
            while (iterator.hasNext());
        }
    }

    public int getNetworkType() {
        return mNetworkType;
    }

    public boolean isNetworkTypeWiFi() {
        return getNetworkType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * set {@link NetworkObserver#mNetworkAvailable} to false and notify the app that network serviceState changed.
     * <p>
     * Call this method if {@link UnknownHostException} or {@link ConnectException} occurs or if parser detect the incorrect response format.
     * i.g. response contains html instead of json.
     * <p>
     * DO NOT call this method if server returns any kind of provided error i.g. 3xx, 4xx.
     * <p>
     * Internal server errors (5xx) processing depends on server stability.
     */
    public void onNetworkError() {
        trace();
        mNetworkAvailable = false;
        mNetworkStateChangedEvent.fire(null);
    }

    public void clearQueue() {
        synchronized (mQueueMonitor) {
            mQueue = null;
        }
    }

    public void onNetworkSuccess(Context context) {
        trace();
        if (!mNetworkAvailable) {
            onNetworkStateChanged(context);
        }
    }


    public interface NetworkStateEventHandler {
        void onNetworkStateChanged(NetworkObserver sender, boolean online);
    }
}
