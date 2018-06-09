package ru.mail.colloquium.service.fcm;

import android.os.Bundle;
import android.text.TextUtils;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;


import ru.mail.colloquium.service.notifications.NotificationJobService;

import static ru.mail.colloquium.App.dispatcher;
import static ru.mail.colloquium.App.gson;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;
import static ru.mail.colloquium.diagnostics.Logger.logFcm;

public class FcmMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        logFcm("new message %s, %s, %s",
                message.getTo(), message.getMessageType(), message.getData());

//        String uid = data.get("uid");
//        String loggedUid = prefs().profile().phone;
//        if (!TextUtils.equals(uid, loggedUid)) {
//            logFcm("Message ignored: addressee is \'%s\', but logged user is \'%s\'", uid, loggedUid);
//            return;
//        }

        Gson gson = gson();
        try {
            startNotificationJob(NotificationJobService.TYPE_SYNC_ANSWERS, null);

        } catch (Exception e) {
            safeThrow(e);
        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void startNotificationJob(String tag, Bundle extras) {
        FirebaseJobDispatcher dispatcher = dispatcher();
        Job job = dispatcher.newJobBuilder()
                .setService(NotificationJobService.class)
                .setTag(tag)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 120))
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(extras)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.mustSchedule(job);
    }

}
