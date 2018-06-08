package ru.mail.colloquium;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import ru.mail.colloquium.api.ApiSet;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.service.ServiceState;
import ru.mail.colloquium.model.types.Profile;

import static android.text.TextUtils.isEmpty;
import static ru.mail.colloquium.App.appState;
import static ru.mail.colloquium.App.gson;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class Preferences {
    private static final String VERSION = "version";
    private static final String STORED_UNIQUE_ID = "stored_unique_id";
    private static final String USER_ID = "user_id";
    private static final String API_BASE_URL = "api_base_url";
    private static final String SERVER_TIME_OFFSET = "server_time_offset";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String PREFIX_PERMISSION_REQUESTED = "permission:";

    private static final String LAST_CONTENT_SYNCHRONIZATION_TS = "last_content_synchronization_ts";

    private static final String PROFILE = "profile";
    private static final String CONFIG = "config";
    private static final String SERVICE_STATE = "serviceState";


    private final SharedPreferences common;
    private final SharedPreferences personal;

    private Profile profile;
    private Configuration config;
    private ServiceState serviceState;

    private volatile int uniqueIdStoredValue;
    private final AtomicInteger uniqueIdCurrentValue;

    private int oldVersion;
    private ApiSet apiSet;

    Preferences(App context) {
        common = PreferenceManager.getDefaultSharedPreferences(context);

        uniqueIdStoredValue = common.getInt(STORED_UNIQUE_ID, 0);
        uniqueIdCurrentValue = new AtomicInteger(uniqueIdStoredValue + 100);

        String userId = common.getString(USER_ID, null);
        personal = context.getSharedPreferences(AppData.normalizeDbName(userId), Context.MODE_PRIVATE);
        onOpen(context);
    }

    Preferences(App context, String userId) {
        common = PreferenceManager.getDefaultSharedPreferences(context);
        common.edit().putString(USER_ID, userId).apply();

        uniqueIdStoredValue = common.getInt(STORED_UNIQUE_ID, 0);
        uniqueIdCurrentValue = new AtomicInteger(uniqueIdStoredValue + 100);

        personal = context.getSharedPreferences(AppData.normalizeDbName(userId), Context.MODE_PRIVATE);
        onOpen(context);
    }

    public boolean clear() {
        return common.edit().clear().commit() && personal.edit().clear().commit();
    }

    private void onOpen(Context context) {
        oldVersion = common.getInt(VERSION, 1);

        if (oldVersion != BuildConfig.VERSION_CODE) {
            SharedPreferences.Editor personalEditor = personal.edit();
            SharedPreferences.Editor commonEditor = common.edit();

            personalEditor.putLong(LAST_CONTENT_SYNCHRONIZATION_TS, 0);
            personalEditor.apply();
            commonEditor.putInt(VERSION, BuildConfig.VERSION_CODE).apply();
        }
    }

    public int getOldVersion() {
        return oldVersion;
    }

    @SuppressLint("ApplySharedPref")
    public int uniqueId() {
        int value = uniqueIdCurrentValue.incrementAndGet();
        if (value - uniqueIdStoredValue >= 100) {
            synchronized (uniqueIdCurrentValue) {
                if (value - uniqueIdStoredValue >= 100) {
                    common.edit().putInt(STORED_UNIQUE_ID, value).commit();
                    uniqueIdStoredValue = value;
                }
            }
        }
        return value;
    }

    public ApiSet getApiSet() {
        ApiSet defaultSet = BuildConfig.DEBUG ? ApiSet.TEST : ApiSet.PROD;
//        ApiSet defaultSet = ApiSet.PROD;
        if (apiSet == null) {
            apiSet = ApiSet.valueOf(common.getString(API_BASE_URL, defaultSet.name()));
        }
        return apiSet;
    }

    void setApiBaseUrl(ApiSet apiSet) {
        common.edit().putString(API_BASE_URL, apiSet.name()).apply();
        this.apiSet = null;
    }

    public String getUserId() {
        return common != null ? common.getString(USER_ID, null) : null;
    }

    public void setServerTimeOffset(long serverTimeOffset) {
        common.edit().putLong(SERVER_TIME_OFFSET, serverTimeOffset).apply();
    }

    public long getServerTimeOffset() {
        return common.getLong(SERVER_TIME_OFFSET, 0L);
    }

    public boolean hasAccount() {
        return getUserId() != null;
    }

    public String getAccessToken() {
        return personal.getString(ACCESS_TOKEN, null);
    }

    public void onLogin(String accessToken, Profile profile) {
        this.profile = profile;
        personal.edit()
                .putString(ACCESS_TOKEN, accessToken)
                .apply();
    }

    void onLogout() {
        profile = null;
        if (common != null)
            common.edit()
                    .remove(USER_ID)
                    .apply();
    }

    public boolean isPermissionRequested(String permission) {
        return common.getBoolean(PREFIX_PERMISSION_REQUESTED + permission, false);
    }

    public void onPermissionRequested(String permission) {
        common.edit().putBoolean(PREFIX_PERMISSION_REQUESTED + permission, true).apply();
    }


    public Profile profile() {
        if (profile != null)
            return profile;

        synchronized (this) {
            if (profile != null)
                return profile;

            Profile profile;
            String json = personal.getString(PROFILE, null);
            if (json != null) {
                profile = gson().fromJson(json, Profile.class);
            } else {
                profile = new Profile();
            }
            return this.profile = profile;
        }
    }

    public void save(Profile profile) {
        save(personal, PROFILE, this.profile, profile);
    }

    public Configuration config() {
        if (config == null) {
            synchronized (this) {
                if (config == null) {
                    String json = common.getString(CONFIG, null);
                    if (json != null) {
                        config = gson().fromJson(json, Configuration.class);
                    } else {
                        config = new Configuration();
                    }
                }
            }
        }
        return config;
    }

    public void save(Configuration config) {
        save(common, CONFIG, this.config, config);
    }

    public ServiceState serviceState() {
        if (serviceState == null) {
            synchronized (this) {
                if (serviceState == null) {
                    String json = personal.getString(SERVICE_STATE, null);
                    if (json != null) {
                        serviceState = gson().fromJson(json, ServiceState.class);
                    } else {
                        serviceState = new ServiceState();
                    }
                }
            }
        }
        return serviceState;
    }

    public void save(ServiceState serviceState) {
        save(personal, SERVICE_STATE, this.serviceState, serviceState);
    }

    private static void save(SharedPreferences preferences, String key, Object expecting, Object data) {
        if (expecting != data) {
            safeThrow(new IllegalArgumentException(key + " save error"));
            return;
        }

        String json = gson().toJson(data);

        preferences.edit()
                .putString(key, json)
                .apply();
    }
}
