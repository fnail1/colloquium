package app.laiki.service.notifications;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import static app.laiki.App.appService;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class NotificationJobService extends JobService {

    public static final String EXTRA_PUSH_TYPE = "push_type";
    public static final String TYPE_SYNC_ANSWERS = "sync_answers";

    public static String getJobTag(String type, String id) {
        return type + "_" + id;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        trace();
        Bundle args = params.getExtras();
        if (args == null)
            return false;

        String type = params.getTag();
//        String type = args.getString(EXTRA_PUSH_TYPE, "");
        switch (type) {
            case TYPE_SYNC_ANSWERS:
                onSyncAnswers(params);
                return true;
            default:
                safeThrow(new IllegalArgumentException("unsupported push type in job " + type));
                return false;
        }
    }

    private void onSyncAnswers(JobParameters params) {
        appService().requestAnswers(() -> jobFinished(params, false));
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


}
