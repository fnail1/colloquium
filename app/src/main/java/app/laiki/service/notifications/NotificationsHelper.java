package app.laiki.service.notifications;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import app.laiki.App;
import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.service.AppService;
import app.laiki.service.AppStateObserver;
import app.laiki.ui.AnswerActivity;
import app.laiki.ui.main.MainActivity;
import app.laiki.utils.GraphicUtils;

import static app.laiki.App.app;
import static app.laiki.App.appState;
import static app.laiki.App.dateTimeService;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class NotificationsHelper implements AppService.AnswerUpdatedEventHandler, AppStateObserver.AppStateEventHandler {

    private static final int STOP_SCREEN_OUT_NOTIFICATION_ID = 1;
    private static final int RETENTION_ALERT_NOTIFICATION_ID = 2;

    private static final int ON_STOP_SCREEN_OUT_TIMER_ID = 1;
    private static final int RETENTION_ALERT_TIMER_ID = 2;

    public static final String INCOMING_LIKES_CHANNEL = "incoming_likes_channel";
    public static final String RETENTION_ALERTS_CHANNEL = "retention_alerts_channel";

    private NotificationManager notificationManager;
    public static final AtomicInteger actionRequestCode = new AtomicInteger((int) SystemClock.elapsedRealtime());


    public NotificationsHelper(AppService appService, AppStateObserver appStateObserver) {
        appService.answerUpdatedEvent.add(this);
        appStateObserver.stateEvent.add(this);
    }


    private int getNotificationId(Answer answer) {
        return (int) (answer._id & 0xffff) + 0x10000;
    }

    public void showNotificationSingleLike(Answer answer) {
        if (!prefs().config().notifications.answers)
            return;
        NotificationManager nm = getNotificationManager();
        Bitmap bitmap = GraphicUtils.getResourceBitmap(app(), answer.gender.heartIconResId);

        String title = "\uD83D\uDE09 Новое мнение о тебе\n";
        String content = "Смотри скорее, что о тебе думают...";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(app(), INCOMING_LIKES_CHANNEL)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_female_heart)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(app().getResources().getColor(R.color.colorAccent))
                .setAutoCancel(true);

        Intent click = new Intent(app(), MainActivity.class);
        click.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        click.setAction(MainActivity.ACTION_OPEN_ANSWER);
        click.putExtra(AnswerActivity.EXTRA_ANSWER_ID, answer._id);
        PendingIntent cmd = PendingIntent.getActivity(app(), actionRequestCode.incrementAndGet(), click, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(cmd);

        nm.notify(getNotificationId(answer), builder.build());
    }

    @Override
    public void onAnswerUpdated(List<Answer> args) {
        if (args == null)
            return;

        if (!prefs().serviceState().answersInitialSyncComplete)
            return;

        for (Answer answer : args) {
            if (!answer.flags.get(Answer.FLAG_READ))
                showNotificationSingleLike(answer);
            else
                getNotificationManager().cancel(getNotificationId(answer));
        }

    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = app().getSystemService(NotificationManager.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.createNotificationChannel(new NotificationChannel(INCOMING_LIKES_CHANNEL, "New likes", NotificationManager.IMPORTANCE_DEFAULT));
                    notificationManager.createNotificationChannel(new NotificationChannel(RETENTION_ALERTS_CHANNEL, "Alerts", NotificationManager.IMPORTANCE_DEFAULT));
                }
            } else {
                notificationManager = (NotificationManager) app().getSystemService(Context.NOTIFICATION_SERVICE);
            }
        }
        return notificationManager;
    }


    @Nullable
    private AlarmManager getAlarmManager(Context context) {
        AlarmManager alarmManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager = context.getSystemService(AlarmManager.class);
        else
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            safeThrow(new NullPointerException());
        }

        return alarmManager;
    }

    public void onStopScreenIn() {
        trace();

        long timeSpan = prefs().config().deadTime - (dateTimeService().getServerTime() - prefs().serviceState().lastAnswerTime);
        if (timeSpan < 0)
            return;

        Context context = app();

        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, NotificationsHelperService.class);
        intent.setAction(NotificationsHelperService.ACTION_STOP_SCREEN_OUT);
        PendingIntent command = PendingIntent.getService(context, ON_STOP_SCREEN_OUT_TIMER_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long t = System.currentTimeMillis() + timeSpan;
        alarmManager.setExact(AlarmManager.RTC, t, command);
    }


    public void onStopScreenOut() {
        trace();

        if (!prefs().config().notifications.alerts)
            return;

        Activity topActivity = appState().getTopActivity();
        if (topActivity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) topActivity;
            if (mainActivity.currentPage() == MainActivity.PAGE_QUESTION) {
                return;
            }
        }

        NotificationManager nm = getNotificationManager();
        Bitmap bitmap = GraphicUtils.getResourceBitmap(app(), R.mipmap.ic_launcher_round);

        String title = "\uD83D\uDC4D Новые вопросы готовы!";
        String content = "Заходи, они сами себя не ответят)))";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(app(), RETENTION_ALERTS_CHANNEL)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_female_heart)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(app().getResources().getColor(R.color.colorAccent))
                .setAutoCancel(true);

        Intent click = new Intent(app(), MainActivity.class);
        click.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent cmd = PendingIntent.getActivity(app(), actionRequestCode.incrementAndGet(), click, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(cmd);

        nm.notify(STOP_SCREEN_OUT_NOTIFICATION_ID, builder.build());
    }

    public void clearStopScreenOut() {
        getNotificationManager().cancel(STOP_SCREEN_OUT_NOTIFICATION_ID);
    }

    @Override
    public void onAppStateChanged(Boolean fromForeground) {
        if (!Boolean.FALSE.equals(fromForeground))
            return;

        App context = app();
        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null)
            return;

        trace();

        Intent intent = new Intent(context, NotificationsHelperService.class);
        intent.setAction(NotificationsHelperService.ACTION_RETENTION_ALERT);
        PendingIntent command = PendingIntent.getService(context, RETENTION_ALERT_TIMER_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + prefs().config().retentionAlertTime, command);
    }

    public void onRetentionAlert() {
        trace();
        if (!prefs().config().notifications.alerts)
            return;

        if (appState().isForeground())
            return;

        NotificationManager nm = getNotificationManager();
        if (nm == null)
            return;


        Bitmap bitmap = GraphicUtils.getResourceBitmap(app(), R.mipmap.ic_launcher_round);

        String title = "\uD83D\uDE18 Ждем тебя";
        String content = "Заходи потупить в вопросики)))";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(app(), RETENTION_ALERTS_CHANNEL)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_female_heart)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(app().getResources().getColor(R.color.colorAccent))
                .setAutoCancel(true);

        Intent click = new Intent(app(), MainActivity.class);
        click.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent cmd = PendingIntent.getActivity(app(), actionRequestCode.incrementAndGet(), click, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(cmd);

        nm.notify(RETENTION_ALERT_NOTIFICATION_ID, builder.build());
    }
}
