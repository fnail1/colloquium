package ru.mail.colloquium.service.notifications;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import retrofit2.Call;
import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.service.MergeHelper;
import ru.mail.colloquium.service.SimpleRequestTask;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;

import static ru.mail.colloquium.App.api;
import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;
import static ru.mail.colloquium.diagnostics.Logger.trace;

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
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new SimpleRequestTask<GsonAnswers>("requestAnswers") {

            @Override
            protected Call<GsonAnswers> getRequest() {
                return api().getAnswers();
            }

            @Override
            protected void processResponse(AppData appData, GsonAnswers body) {
                MergeHelper.merge(appData, body.answers);
            }

            @Override
            protected void onFinish() {
                appService().answerUpdatedEvent.fire(null);
                jobFinished(params, false);
            }

        });
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


}
