package ru.mail.colloquium.service;

import android.os.Build;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import retrofit2.Call;
import retrofit2.Response;
import ru.mail.colloquium.BuildConfig;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.http.LoginRequiredException;
import ru.mail.colloquium.toolkit.http.ServerException;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.networkObserver;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;
import static ru.mail.colloquium.diagnostics.Logger.trace;


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
