package ru.mail.colloquium.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Response;
import ru.mail.colloquium.App;
import ru.mail.colloquium.Preferences;
import ru.mail.colloquium.R;
import ru.mail.colloquium.toolkit.events.ObservableEvent;

import static java.util.Calendar.AM;
import static java.util.Calendar.AM_PM;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class DateTimeService {

    public static final long[] durationStatScale = {
            /*ms*/ 100, 200, 500,
            /*s */ 1 * 1000, 2 * 1000, 3 * 1000, 5 * 1000, 10 * 1000, 15 * 1000, 20 * 1000, 30 * 1000, 40 * 1000, 50 * 1000, 60 * 1000, 70 * 1000, 80 * 1000, 90 * 1000,
            /*m */ 2 * 60 * 1000, 3 * 60 * 1000, 5 * 60 * 1000, 10 * 60 * 1000, 15 * 60 * 1000, 20 * 60 * 1000, 30 * 60 * 1000, 40 * 60 * 1000, 50 * 60 * 1000, 60 * 60 * 1000, 70 * 60 * 1000, 80 * 60 * 1000, 90 * 60 * 1000,
            /*h */ 2 * 60 * 60 * 1000, 3 * 60 * 60 * 1000, 5 * 60 * 60 * 1000, 8 * 60 * 60 * 1000, 12 * 60 * 60 * 1000, 16 * 60 * 60 * 1000, 20 * 60 * 60 * 1000, 24 * 60 * 60 * 1000, 30 * 60 * 60 * 1000, 36 * 60 * 60 * 1000
    };

    public static final String[] durationStatValues = {
            /*ms*/ "100ms", "200ms", "500ms",
            /*s */ "1s", "2s", "3s", "5s", "10s", "15s", "20s", "30s", "40s", "50s", "60s", "70s", "80s", "90s",
            /*m */ "2m", "3m", "5m", "10m", "15m", "20m", "30m", "40m", "50m", "60m", "70m", "80m", "90m",
            /*h */ "2h", "3h", "5h", "8h", "12h", "16h", "20h", "24h", "30h", "36h"
    };

    public final ObservableEvent<DateChangedEventHandler, DateTimeService, Void> dateChangedEvent = new ObservableEvent<DateChangedEventHandler, DateTimeService, Void>(this) {
        @Override
        protected void notifyHandler(DateChangedEventHandler handler, DateTimeService sender, Void args) {
            handler.onDateTimeChanged();
        }
    };


    public final String[] weekDaysPrepositional;
    public final String[] weekDays;
    public final String[] monthsNamesGenitive;
    public final String[] monthsNamesNominative;

    private final SimpleDateFormat dateLongFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
    private final SimpleDateFormat dateLongFormatWithYear = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat serverDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final SimpleDateFormat httpDateTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    private final String f0;
    private final String f1;
    private final String f2;
    private final String f3;
    private final Resources resources;

    private long serverTimeOffset;
    private boolean serverTimeOffsetDirty;


    public DateTimeService(App context, Preferences preferences) {
        this.resources = context.getResources();
        weekDaysPrepositional = resources.getStringArray(R.array.week_days_prepositional);
        weekDays = resources.getStringArray(R.array.week_days);
        monthsNamesGenitive = resources.getStringArray(R.array.months_genitive);
        monthsNamesNominative = resources.getStringArray(R.array.months_nominative);
        f0 = resources.getString(R.string.calendar_day_format_2);
        f1 = resources.getString(R.string.calendar_day_format_1);
        f2 = resources.getString(R.string.calendar_week_format_1);
        f3 = resources.getString(R.string.calendar_week_format_2);

        serverTimeOffset = preferences.getServerTimeOffset();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                serverTimeOffsetDirty = true;
                onServerTimeOffsetChanged();
            }
        }, filter);
    }


    public String formatDate(byte month, byte day) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        return dateLongFormat.format(date.getTime());
    }

    public String formatDateLongForm(Calendar date) {
        return String.format(Locale.getDefault(), f0,
                weekDays[date.get(Calendar.DAY_OF_WEEK) - date.getActualMinimum(Calendar.DAY_OF_WEEK)],
                date.get(Calendar.DAY_OF_MONTH), monthsNamesGenitive[date.get(Calendar.MONTH)]);
    }

    public boolean isToday(byte month, byte day) {
        Calendar date = Calendar.getInstance();
        return month == date.get(Calendar.MONTH) && day == date.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Отвечает на вопрос "когда это случилось?"
     * Например, может вернуть "завтра", "во вторник"...
     */
    public String whenItWas(byte month, byte day) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        zeroTimeOfDay(date);

        return whenItWas(date);
    }

    /**
     * Отвечает на вопрос "когда это случилось?"
     * Например, может вернуть "завтра", "во вторник"...
     */
    public String whenItWas(int year, byte month, byte day) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.YEAR, year);
        zeroTimeOfDay(date);

        return whenItWas(date);
    }

    public String whenItWas(Calendar date) {
        Calendar now = Calendar.getInstance();
        zeroTimeOfDay(now);


        long t0 = now.getTimeInMillis();
        long t1 = date.getTimeInMillis();
        int days = (int) ((t1 - t0) / (24 * 60 * 60 * 1000));

        switch (days) {
            case -2:
                return resources.getString(R.string.before_yesterday);
            case -1:
                return resources.getString(R.string.yesterday);
            case 0:
                return resources.getString(R.string.today);
            case 1:
                return resources.getString(R.string.tomorrow);
            case 2:
                return resources.getString(R.string.after_tomorrow);

        }

        if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR) && now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR))
            return weekDaysPrepositional[date.get(Calendar.DAY_OF_WEEK) - date.getActualMinimum(Calendar.DAY_OF_WEEK)];

        return dateLongFormat.format(date.getTime());
    }

    /**
     * Отвечает на вопрос "когда это случилось?"
     * Например, может вернуть "Только что", "5 минут назад", "завтра", "во вторник"...
     *
     * @param timestamp время в миллисекундах
     */
    public String whenItWas(long timestamp) {
        Calendar now = Calendar.getInstance();
        long t0 = now.getTimeInMillis();
        long diff = timestamp - t0;

        if (-12 * 60 * 60 * 1000 <= diff && diff <= -60 * 60 * 1000) {
            int s = (int) (-diff / 1000);
            int m = s / 60;
            int h = m / 60;
            m %= 60;
            if (m > 0) {
                String sh = resources.getQuantityString(R.plurals.smart_time_units_h, h, h);
                String sm = resources.getQuantityString(R.plurals.smart_time_units_m, m, m);
                return resources.getString(R.string.smart_time_format_h_m_ago, sh, sm);
            } else {
                String sh = h == 1
                        ? resources.getString(R.string.smart_time_units_1_h_genitive)
                        : resources.getQuantityString(R.plurals.smart_time_units_h, h, h);
                return resources.getString(R.string.smart_time_format_ago, sh);
            }
        }

        if (-60 * 60 * 1000 < diff && diff <= -60 * 1000) {
            int m = (int) (-diff / (60 * 1000));

            String sm = m == 1
                    ? resources.getString(R.string.smart_time_units_1_m_genitive)
                    : resources.getQuantityString(R.plurals.smart_time_units_m, m, m);
            return resources.getString(R.string.smart_time_format_ago, sm);
        }

        if (-60 * 1000 < diff && diff <= 0)
            return resources.getString(R.string.now_past);

        if (0 < diff && diff <= 60 * 1000)
            return resources.getString(R.string.now);


        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);

        zeroTimeOfDay(date);
        zeroTimeOfDay(now);

        diff = date.getTimeInMillis() - now.getTimeInMillis();

        int days = (int) (diff / (24 * 60 * 60 * 1000));

        switch (days) {
            case -2:
                return resources.getString(R.string.before_yesterday);
            case -1:
                return resources.getString(R.string.yesterday);
            case 0:
                return resources.getString(R.string.today);
            case 1:
                return resources.getString(R.string.tomorrow);
            case 2:
                return resources.getString(R.string.after_tomorrow);

        }

        if (now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR))
            return weekDaysPrepositional[date.get(Calendar.DAY_OF_WEEK) - date.getActualMinimum(Calendar.DAY_OF_WEEK)];

        if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR))
            return dateLongFormat.format(date.getTime());
        else
            return dateLongFormatWithYear.format(date.getTime());
    }

    public void zeroTimeOfDay(Calendar date) {
        date.set(AM_PM, AM);
        date.set(HOUR_OF_DAY, 0);
        date.set(MINUTE, 0);
        date.set(SECOND, 0);
        date.set(MILLISECOND, 0);
    }

    public String formatDuration(long millis) {
        if (millis < 100)
            return "100ms";
//            return String.valueOf(millis);

        if (millis > 36 * 60 * 60 * 1000)
            return "" + (millis / (24 * 60 * 60 * 1000)) + 'd';

        int i = Arrays.binarySearch(durationStatScale, millis);
        if (i < 0)
            i = -i - 1;

        return durationStatValues[i];
    }

    public boolean inRange(Calendar start, Calendar end, int eventMonth, int eventDay) {
        int s = PDOY(start);
        int e = PDOY(end);
        int x = PDOY(eventMonth, eventDay);
        return s <= x && x <= e;
    }

    /**
     * Pseudo Day-Of-Year
     *
     * @return metric to simple comparison of two dates. If value for date A less then B, then day A is early then day B
     */
    public int PDOY(Calendar calendar) {
        return PDOY(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Pseudo Day-Of-Year
     *
     * @return metric to simple comparison of two dates. If value for date A less then B, then day A is early then day B
     */
    public int PDOY(int month, int day) {
        return 31 * month + day;
    }

    /**
     * Pseudo Day-Of-Age
     * The same as {@link DateTimeService#PDOY(java.util.Calendar)} but taking into account the year.
     *
     * @return metric to simple comparison of two dates. If value for date A less then B, then day A is early then day B
     */
    public int PDOA(Calendar calendar) {
        return PDOA(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Pseudo Day-Of-Age
     * The same as {@link DateTimeService#PDOY(java.util.Calendar)} but taking into account the year.
     *
     * @return metric to simple comparison of two dates. If value for date A less then B, then day A is early then day B
     */
    public int PDOA(int year, int month, int day) {
        return 366 * year + 31 * month + day;
    }


    public long parseServerTime(String dateString) {
        if (dateString == null)
            return 0;
        try {
            return serverDateTimeFormat.parse(dateString).getTime();
        } catch (ParseException e) {
            safeThrow(e);
            return 0;
        }
    }

    protected void onServerTimeOffsetChanged() {
        dateChangedEvent.fire(null);
    }

    public long getServerTime() {
        return getServerTime(System.currentTimeMillis());
    }

    public long getServerTime(long currentTimeMillis) {
        return currentTimeMillis + serverTimeOffset;
    }

    public long getLocalTime(long serverTimeMillis) {
        return serverTimeMillis - serverTimeOffset;
    }

    public long adjustServerTimeOffset(Response<?> response) {
        String dateString = response.headers().get("Date");
        try {
            long time = httpDateTimeFormat.parse(dateString).getTime();
            return adjustServerTimeOffset(time);
        } catch (ParseException e) {
            safeThrow(e);
            return getServerTime();
        }

    }

    public long adjustServerTimeOffset(long time) {
        long localTime = System.currentTimeMillis();
        long offset = time - localTime;
        if (serverTimeOffsetDirty || Math.abs(offset - serverTimeOffset) > 3000) {
            serverTimeOffsetDirty = false;
            serverTimeOffset = offset;
            prefs().setServerTimeOffset(serverTimeOffset);
            onServerTimeOffsetChanged();
        }
        return localTime + serverTimeOffset;
    }

    public boolean serverTimeOffsetDirty() {
        return serverTimeOffsetDirty;
    }

    public long getServerTimeOffset() {
        return serverTimeOffset;
    }

    public long getServerTimeOffsetWithTZ() {
        return -serverTimeOffset + TimeZone.getDefault().getRawOffset();
    }

    public interface DateChangedEventHandler {
        void onDateTimeChanged();
    }
}
