package app.laiki;

import java.io.Serializable;

public class Configuration implements Serializable {
    public final Notifications notifications = new Notifications();
    public int questionsFrameSize = BuildConfig.DEBUG ? 2 : 15;
    public int deadTime = BuildConfig.DEBUG ? 30 * 1000 : 60 * 60 * 1000;

    public static class Notifications {
        public boolean answer = true;
    }
}
