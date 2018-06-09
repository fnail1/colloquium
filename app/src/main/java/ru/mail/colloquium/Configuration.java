package ru.mail.colloquium;

import java.io.Serializable;

public class Configuration implements Serializable {
    public final Notifications notifications = new Notifications();

    public static class Notifications {
        public boolean answer;
    }
}
