package app.laiki;

import android.app.Application;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.gson.Gson;

import app.laiki.api.ApiService;
import app.laiki.diagnostics.DebugUtils;
import app.laiki.diagnostics.statistics.Statistics;
import app.laiki.model.AppData;
import app.laiki.model.types.Profile;
import app.laiki.service.AppService;
import app.laiki.service.AppStateObserver;
import app.laiki.service.notifications.NotificationsHelper;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.network.NetworkObserver;
import app.laiki.ui.ScreenMetrics;
import app.laiki.utils.DateTimeService;
import app.laiki.utils.photomanager.PhotoManager;

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
    private FirebaseJobDispatcher jobDispatcher;
    private NotificationsHelper notificationsHelper;
    private Statistics statistics;


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

    public static FirebaseJobDispatcher dispatcher() {
        return instance.jobDispatcher;
    }

    public static NotificationsHelper notifications() {
        return instance.notificationsHelper;
    }

    public static Statistics statistics() {
        return instance.statistics;
    }

    public static App app() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        DebugUtils.init(this);
        statistics = new Statistics(this);
        preferences = new Preferences(this);
        data = new AppData(this, preferences.getUserId());
        appStateObserver = new AppStateObserver(this, preferences);
        gson = new Gson();
        appService = new AppService(this, appStateObserver);
        networkObserver = new NetworkObserver(this);
        screenMetrics = new ScreenMetrics(this);
        apiService = ApiService.Creator.newService(preferences.getApiSet(), this);
        dateTimeService = new DateTimeService(this, preferences);
        photoManager = new PhotoManager(this, appStateObserver);
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        notificationsHelper = new NotificationsHelper(appService);

        instance = this;


//        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(() -> {
//            try {
//                Logger.logV("FCM", FcmRegistrationService.getAccessToken());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    public void onLogin(String accessToken, Profile profile) {
        preferences = new Preferences(this, profile.phone);
        preferences.onLogin(accessToken, profile);
        preferences.save(profile);

        AppData old = this.data;
        this.data = new AppData(this, profile.phone);
        ThreadPool.UI.postDelayed(old::close, 10 * 1000);
        appService.shutdown();
        appService = new AppService(this, appStateObserver);
        notificationsHelper = new NotificationsHelper(appService);

        if (this.data.questions.selectCurrent() == null) {
            appService.requestNextQuestion();
        }

        appService.syncFcm();
    }

    public void logout() {
        preferences.onLogout();
        data.close();
        System.exit(0);
    }
}
