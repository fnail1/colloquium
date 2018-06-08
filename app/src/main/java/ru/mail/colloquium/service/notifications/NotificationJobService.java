package ru.mail.colloquium.service.notifications;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class NotificationJobService extends JobService {

    public static final String EXTRA_PUSH_TYPE = "push_type";

    public static String getJobTag(String type, String id) {
        return type + "_" + id;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle args = params.getExtras();
        if (args == null)
            return false;

        String type = args.getString(EXTRA_PUSH_TYPE, "");
        switch (type) {

            default:
                safeThrow(new IllegalArgumentException("unsupported push type in job " + type));
                return false;
        }
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


}
