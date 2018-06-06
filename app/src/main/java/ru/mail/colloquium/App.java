package ru.mail.colloquium;

import android.app.Application;

import com.google.gson.Gson;

import ru.mail.colloquium.api.ApiService;
import ru.mail.colloquium.api.model.GsonProfileResponse;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.model.types.Gender;
import ru.mail.colloquium.model.types.Profile;
import ru.mail.colloquium.service.AppService;
import ru.mail.colloquium.service.AppStateObserver;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.network.NetworkObserver;
import ru.mail.colloquium.ui.ScreenMetrics;
import ru.mail.colloquium.utils.DateTimeService;
import ru.mail.colloquium.utils.photomanager.PhotoManager;

public class App extends Application {
    private static App instance;
    private Preferences preferences;
    private AppData data;
    private AppStateObserver appStateObserver;
    private Gson gson;
    private AppService appService;
    private NetworkObserver networkObserver;
    private ScreenMetrics screenMetrics;
    private ApiService apiService;
    private DateTimeService dateTimeService;
    private PhotoManager photoManager;


    public static AppData data() {
        return instance.data;
    }

    public static Preferences prefs() {
        return instance.preferences;
    }

    public static AppStateObserver appState() {
        return instance.appStateObserver;
    }

    public static Gson gson() {
        return instance.gson;
    }

    public static AppService service() {
        return instance.appService;
    }

    public static NetworkObserver networkObserver() {
        return instance.networkObserver;
    }

    public static ScreenMetrics screenMetrics() {
        return instance.screenMetrics;
    }

    public static DateTimeService dateTimeService() {
        return instance.dateTimeService;
    }

    public static ApiService api() {
        return instance.apiService;
    }

    public static AppService appService() {
        return instance.appService;
    }

    public static PhotoManager photos() {
        return instance.photoManager;
    }

    public static App app() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(this);
        data = new AppData(this, preferences.getUserId());
        appStateObserver = new AppStateObserver(this, preferences);
        gson = new Gson();
        appService = new AppService(appStateObserver);
        networkObserver = new NetworkObserver(this);
        screenMetrics = new ScreenMetrics(this);
        apiService = ApiService.Creator.newService(preferences.getApiSet(), this);
        dateTimeService = new DateTimeService(this, preferences);
        photoManager = new PhotoManager(this, appStateObserver);

        instance = this;

    }

    public void onLogin(String accessToken, Profile profile) {
        preferences = new Preferences(this, profile.phone, profile);
        preferences.onLogin(accessToken);
        preferences.save(profile);

        AppData old = this.data;
        this.data = new AppData(this, profile.phone);
        ThreadPool.UI.postDelayed(old::close, 10 * 1000);
        appService.shutdown();
        appService = new AppService(appStateObserver);
        if (this.data.questions.selectCurrent() == null) {
            appService.requestNextQuestion();
        }
    }

    public void logout() {
        preferences.onLogout();
        System.exit(0);
    }
}
