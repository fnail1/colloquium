package app.laiki.service.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import static app.laiki.App.notifications;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class NotificationsHelperService extends IntentService {

    public static final String ACTION_STOP_SCREEN_OUT = "stop_screen_out";

    public NotificationsHelperService() {
        super("NotificationsHelperService");
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            safeThrow(new Exception("intent is null"));
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            safeThrow(new Exception("action is null"));
            return;
        }

        trace(action);

        switch (action) {
            case ACTION_STOP_SCREEN_OUT:
                notifications().onStopScreenOut();
        }
    }
}
