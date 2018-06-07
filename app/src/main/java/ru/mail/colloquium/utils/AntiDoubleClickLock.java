package ru.mail.colloquium.utils;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class AntiDoubleClickLock {
    private static final Entry[] history = new Entry[16];
    public static final int DOUBLE_CLICK_TIME_MS = 500;
    private static int ptr = 0;

    public static boolean onClick(@NonNull Object context, int buttonId) {
        long ts = SystemClock.elapsedRealtime();
        for (Entry entry : history) {
            if (entry == null)
                continue;
            if (ts - entry.ts > DOUBLE_CLICK_TIME_MS)
                continue;
            if (entry.get() == context && entry.buttonId == buttonId) {
//                trace("%s %d MISS", context, buttonId);

                return false;
            }
        }

        history[ptr++ % history.length] = new Entry(context, buttonId, ts);
//        trace("%s %d HIT", context, buttonId);
        return true;
    }

    private static class Entry extends WeakReference<Object> {
        final int buttonId;
        final long ts;

        private Entry(Object context, int buttonId, long ts) {
            super(context);
            this.buttonId = buttonId;
            this.ts = ts;
        }
    }
}
