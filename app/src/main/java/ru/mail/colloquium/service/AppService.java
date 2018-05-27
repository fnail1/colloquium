package ru.mail.colloquium.service;

import ru.mail.colloquium.service.ab.AddressBookSyncHelper;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.appState;
import static ru.mail.colloquium.App.prefs;

public class AppService implements AppStateObserver.AppStateEventHandler {
    public static final long MAX_SYNCHRONIZATION_LAG = 60 * 60 * 1000;
    private final AppStateObserver appStateObserver;

    public AppService(AppStateObserver appStateObserver) {
        this.appStateObserver = appStateObserver;
        appStateObserver.stateEvent.add(this);
    }

    public void shutdown() {
        appStateObserver.stateEvent.remove(this);
    }

    @Override
    public void onAppStateChanged() {
        if (!prefs().hasAccount())
            return;
        if (appStateObserver.getServerTime() - prefs().serviceState().lastSync <= MAX_SYNCHRONIZATION_LAG)
            return;

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(() -> {
            AddressBookSyncHelper.doSync(app());
        });
    }
}
