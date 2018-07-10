package app.laiki.service;

import android.os.Build;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import app.laiki.BuildConfig;
import app.laiki.model.AppData;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.http.LoginRequiredException;
import app.laiki.toolkit.http.ServerException;

import static app.laiki.App.app;
import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.networkObserver;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;


public abstract class AbsRequestTask implements Runnable {

    private static final HashSet<String> executingTasks = new HashSet<>();

    private static final long TIMEOUT = 30 * 1000;
    public final int MAX_ERRORS = 3;

    protected final String key;
    private final AppData appData;
    protected int errors = 0;

    protected AbsRequestTask(String key) {
        this.key = key;
        appData = data();
    }

    @Override
    public void run() {
        trace("'1");
        if (ThreadPool.isUiThread()) {
            ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(this);
            return;
        }
//        boolean wiFi = networkObserver().isNetworkTypeWiFi();
//        trace("'2 " + (wiFi ? "wifi" : "nowifi"));

        synchronized (executingTasks) {
            if (!executingTasks.add(key)) {
                return;
            }
        }

        try {
            if (requiresConsistency() && !appService().waitForSynchronisationComplete(TIMEOUT)) {
                reschedule();
                return;
            }

            performRequest(appData);

            networkObserver().onNetworkSuccess(app());
        } catch (UnknownHostException | ConnectException | SSLHandshakeException e) {
            e.printStackTrace();
            networkObserver().onNetworkError();
            reschedule();
        } catch (LoginRequiredException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
            else
                safeThrow(e);
        } catch (IllegalStateException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && appData == data() && appData.isOpen())
                safeThrow(e);
            else
                e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if (networkObserver().isNetworkAvailable() && appService().pingApi())
                restart();
            else {
                networkObserver().onNetworkError();
                reschedule();
            }
        } catch (NullPointerException | AssertionError e) {
            // хорошо известная проблема 4.х http://crashes.to/s/48b8f0deaae
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                safeThrow(e);
        } catch (Exception e) {
            safeThrow(e);
        } finally {
            synchronized (executingTasks) {
                executingTasks.remove(key);
            }
            onFinish();
        }
//        trace("" + wiFi);
    }

    protected boolean requiresConsistency() {
        return true;
    }

    private void restart() {
        if (errors >= MAX_ERRORS)
            return;
        errors++;

        trace("" + errors);

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(this);
    }

    private void reschedule() {
        if (errors >= MAX_ERRORS)
            return;
        errors++;

        trace("" + errors);

        networkObserver().scheduleTask(key, this);
    }


    protected abstract void performRequest(AppData appData) throws IOException, ServerException;


    protected abstract void onFinish();
}
