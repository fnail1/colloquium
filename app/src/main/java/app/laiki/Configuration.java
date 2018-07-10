package app.laiki;

import java.io.Serializable;

public class Configuration implements Serializable {
    public final Notifications notifications = new Notifications();
    public int questionsFrameSize = BuildConfig.DEBUG ? 5 : 12;
    public int deadTime = BuildConfig.DEBUG ? 30 * 1000 : 60 * 60 * 1000;
    public int inviteTrigger = BuildConfig.DEBUG ? 3 : 8;
    public boolean emulateSlowConnection = false;
    public String fcmToken;

    public static class Notifications {
        public boolean answer = true;
    }
}
