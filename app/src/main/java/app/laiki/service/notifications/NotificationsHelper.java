package app.laiki.service.notifications;

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

import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.service.AppService;
import app.laiki.ui.AnswerActivity;
import app.laiki.ui.main.MainActivity;
import app.laiki.utils.GraphicUtils;

import static app.laiki.App.app;
import static app.laiki.App.dateTimeService;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class NotificationsHelper implements AppService.AnswerUpdatedEventHandler {

    private static final int STOP_SCREEN_OUT_NOTIFICATION_ID = 1;
    public static final String INCOMING_LIKES_CHANNEL = "incoming_likes_channel";
    private NotificationManager notificationManager;
    public static final AtomicInteger actionRequestCode = new AtomicInteger((int) SystemClock.elapsedRealtime());
    private long stopScreenOutTrigger;


    public NotificationsHelper(AppService appService) {
        appService.answerUpdatedEvent.add(this);
    }


    private int getNotificationId(Answer answer) {
        return (int) (answer._id & 0xffff) + 0x10000;
    }


    public void showNotificationSingleLike(Answer answer) {
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

        long t = System.currentTimeMillis() + timeSpan;
        if (t <= stopScreenOutTrigger)
            return;

        stopScreenOutTrigger = t;

        Context context = app();

        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, NotificationsHelperService.class);
        intent.setAction(NotificationsHelperService.ACTION_STOP_SCREEN_OUT);
        PendingIntent command = PendingIntent.getService(context, actionRequestCode.incrementAndGet(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setExact(AlarmManager.RTC, this.stopScreenOutTrigger, command);
    }


    public void onStopScreenOut() {
        trace();
        NotificationManager nm = getNotificationManager();
        Bitmap bitmap = GraphicUtils.getResourceBitmap(app(), R.drawable.logo);

        String title = "\uD83D\uDC4D Новые вопросы!\n";
        String content = "Заходи, а то сами себя не ответят";

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
        PendingIntent cmd = PendingIntent.getActivity(app(), actionRequestCode.incrementAndGet(), click, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(cmd);

        nm.notify(STOP_SCREEN_OUT_NOTIFICATION_ID, builder.build());
    }
}
