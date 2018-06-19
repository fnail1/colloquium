package app.laiki;

import java.io.Serializable;

public class Configuration implements Serializable {
    public final Notifications notifications = new Notifications();

    public static class Notifications {
        public boolean answer = true;
    }
}
