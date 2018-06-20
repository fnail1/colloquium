package app.laiki.service.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.model.types.Gender;
import app.laiki.service.AppService;
import app.laiki.ui.AnswerActivity;
import app.laiki.ui.main.MainActivity;
import app.laiki.utils.GraphicUtils;

import static app.laiki.App.app;

public class NotificationsHelper implements AppService.AnswerUpdatedEventHandler {

    public static final String INCOMING_LIKES_CHANNEL = "incoming_likes_channel";
    private NotificationManager notificationManager;
    public static final AtomicInteger actionRequestCode = new AtomicInteger((int) SystemClock.elapsedRealtime());


    public NotificationsHelper(AppService appService) {
        appService.answerUpdatedEvent.add(this);
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

    private int getNotificationId(Answer answer) {
        return (int) (answer._id & 0xffff);
    }

    @Override
    public void onAnswerUpdated(List<Answer> args) {
        if (args == null)
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
}
