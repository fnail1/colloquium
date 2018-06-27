package app.laiki.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import app.laiki.api.model.GsonAnswers;
import app.laiki.api.model.GsonQuestionResponse;
import app.laiki.api.model.GsonResponse;
import app.laiki.model.AppData;
import app.laiki.model.entities.Answer;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.service.ab.AddressBookSyncHelper;
import app.laiki.service.fcm.FcmRegistrationService;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.events.ObservableEvent;
import app.laiki.toolkit.http.ServerException;

import static app.laiki.App.api;
import static app.laiki.App.app;
import static app.laiki.App.data;
import static app.laiki.App.dateTimeService;
import static app.laiki.App.prefs;
import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

@SuppressWarnings("WeakerAccess")
public class AppService implements AppStateObserver.AppStateEventHandler {
    public static final long MAX_SYNCHRONIZATION_LAG = 60 * 60 * 1000;
    private final Context context;
    private final AppStateObserver appStateObserver;
    private final ContentObserver contentObserver;

    private long lastContactsSync;

    public final ObservableEvent<NewQuestionEventHandler, AppService, Question> newQuestionEvent = new ObservableEvent<NewQuestionEventHandler, AppService, Question>(this) {
        @Override
        protected void notifyHandler(NewQuestionEventHandler handler, AppService sender, Question args) {
            handler.onNewQuestion(args);
        }
    };

    public final ObservableEvent<AnswerSentEventHandler, AppService, Question> answerSentEvent = new ObservableEvent<AnswerSentEventHandler, AppService, Question>(this) {
        @Override
        protected void notifyHandler(AnswerSentEventHandler handler, AppService sender, Question args) {
            handler.onAnswerSent(args);
        }
    };

    public final ObservableEvent<AnswerUpdatedEventHandler, AppService, List<Answer>> answerUpdatedEvent = new ObservableEvent<AnswerUpdatedEventHandler, AppService, List<Answer>>(this) {
        @Override
        protected void notifyHandler(AnswerUpdatedEventHandler handler, AppService sender, List<Answer> args) {
            handler.onAnswerUpdated(args);
        }
    };

    public final ObservableEvent<ContactsSynchronizationEventHandler, AppService, Void> contactsSynchronizationEvent = new ObservableEvent<ContactsSynchronizationEventHandler, AppService, Void>(this) {
        @Override
        protected void notifyHandler(ContactsSynchronizationEventHandler handler, AppService sender, Void args) {
            handler.onContactsSynchronizationComplete();
        }
    };

    public final ObservableEvent<InviteSentEventHandler, AppService, Contact> inviteSentEvent = new ObservableEvent<InviteSentEventHandler, AppService, Contact>(this) {
        @Override
        protected void notifyHandler(InviteSentEventHandler handler, AppService sender, Contact args) {
            handler.onInviteSent(args);
        }
    };

    public final LongSparseArray<Contact> sentInvites = new LongSparseArray<>();

    public AppService(Context context, AppStateObserver appStateObserver) {
        this.context = context;
        this.appStateObserver = appStateObserver;
        appStateObserver.stateEvent.add(this);

        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                trace();
                super.onChange(selfChange, uri);
                if (lastContactsSync > 0)
                    lastContactsSync = 1;
                onAppStateChanged();
            }
        };
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            context.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, false, contentObserver);
        }
    }

    public void shutdown() {
        appStateObserver.stateEvent.remove(this);
        context.getContentResolver().unregisterContentObserver(contentObserver);
    }

    public long getLastContactsSync() {
        return lastContactsSync;
    }

    @Override
    public void onAppStateChanged() {
        if (!prefs().hasAccount())
            return;

        if (!appStateObserver.isForeground())
            return;

        if (dateTimeService().getServerTime() - lastContactsSync <= MAX_SYNCHRONIZATION_LAG)
            return;

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(() -> {
            try {
                AddressBookSyncHelper.doSync(app());
            } catch (SecurityException e) {
                return;
            }
            ServiceState serviceState = prefs().serviceState();
            lastContactsSync = dateTimeService().getServerTime();
            prefs().save(serviceState);
            contactsSynchronizationEvent.fire(null);

            try {
                syncAnsweredQuestionsSync(data());
                syncReadAnswersSync(data());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServerException e) {
                safeThrow(e);
            }
        });
    }

    public boolean pingApi() {
        try {
            if (api().ping().execute().code() == HttpURLConnection.HTTP_OK)
                return true;
        } catch (IOException | AssertionError | NullPointerException ignored) {
        }
        return false;
    }

    public void requestNextQuestion() {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(
                new SimpleRequestTask<GsonQuestionResponse>("requestNextQuestion") {

                    private Question question;

                    @Override
                    protected void processResponse(AppData appData, GsonQuestionResponse body) {
                        question = new Question();
                        MergeHelper.merge(question, body.question, body.question_cycle);
                        data().questions.save(question);
                    }

                    @Override
                    protected Call<GsonQuestionResponse> getRequest() {
                        return api().nextQuestion();
                    }

                    @Override
                    protected void onFinish() {
                        newQuestionEvent.fire(question);
                    }
                });
    }

    public void requestAnswers() {
        requestAnswers(null);
    }

    public void requestAnswers(Runnable callback) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new SimpleRequestTask<GsonAnswers>("requestAnswers") {
            public boolean success;
            ArrayList<Answer> newAnswers = new ArrayList<>();


            @Override
            protected Call<GsonAnswers> getRequest() {
                return api().getAnswers();
            }

            @Override
            protected void processResponse(AppData appData, GsonAnswers body) {
                MergeHelper.merge(appData, body.answers, newAnswers);
                success = true;
            }

            @Override
            protected void onFinish() {
                if (!newAnswers.isEmpty()) {
                    answerUpdatedEvent.fire(newAnswers);
                }

                if (callback != null)
                    callback.run();

                if (success) {
                    ServiceState serviceState = prefs().serviceState();
                    if (!serviceState.answersInitialSyncComplete) {
                        serviceState.answersInitialSyncComplete = true;
                        prefs().save(serviceState);
                    }
                }
            }

        });
    }

    public void syncAnswers(Question question, @NonNull Choice answer) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new AbsRequestTask("syncAnswers") {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                syncAnsweredQuestionsSync(appData);
            }

            @Override
            protected void onFinish() {
                answerSentEvent.fire(question);
            }

        });
    }

    public void syncAnsweredQuestionsSync(AppData appData) throws IOException, ServerException {
        List<Question> questions = appData.questions.selectToSend().toList();
        LongSparseArray<Contact> contacts = appData.contacts.selectQuestionsVariants().toLongSparseArray(c -> c._id);

        for (Question question : questions) {
            Contact contact1 = contacts.get(question.variant1);
            Contact contact2 = contacts.get(question.variant2);
            Contact contact3 = contacts.get(question.variant3);
            Contact contact4 = contacts.get(question.variant4);

            if (contact1 == null || contact2 == null || contact3 == null || contact4 == null) {
                appData.questions.delete(question);
                continue;
            }

            String name;
            switch (question.answer) {
                case A:
                    name = contact1.displayName;
                    break;
                case B:
                    name = contact2.displayName;
                    break;
                case C:
                    name = contact3.displayName;
                    break;
                case D:
                    name = contact4.displayName;
                    break;
                case E:
                default:
                    name = null;
                    break;
            }

            Response<GsonResponse> response = api().answer(question.serverId, question.answer, name,
                    contact1.serverId, contact2.serverId, contact3.serverId, contact4.serverId).execute();

            switch (response.code()) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    throw new ServerException(response);
                default:
                    safeThrow(new ServerException(response));
                    break;
            }

            question.flags.set(Question.FLAG_SENT, true);
            appData.questions.save(question);
        }
    }

    private void syncReadAnswersSync(AppData appData) throws IOException {
        for (Answer answer : appData.answers.selectToSync()) {
            Response<GsonResponse> response = api().viewAnswer(answer.serverId).execute();
            if (response.code() != HttpURLConnection.HTTP_OK) {
                safeThrow(new ServerException(response));
                return;
            }
            GsonResponse body = response.body();
            if (body == null || !body.success) {
                safeThrow(new Exception("" + body));
                return;
            }
            answer.flags.set(Answer.FLAG_SENT, true);
            appData.answers.save(answer);
        }
    }


    public void answerRead(Answer answer) {
        if (answer.flags.get(Answer.FLAG_SENT))
            return;

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new SimpleRequestTask("answerRead" + answer.serverId) {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                if (!answer.flags.getAndSet(Answer.FLAG_READ, true)) {
                    appData.answers.save(answer);
                }
                super.performRequest(appData);
            }

            @Override
            protected Call getRequest() {
                return api().viewAnswer(answer.serverId);
            }

            @Override
            protected void processResponse(AppData appData, Object body) {
                if (!answer.flags.getAndSet(Answer.FLAG_SENT, true)) {
                    appData.answers.save(answer);
                }
            }

            @Override
            protected void onFinish() {
                answerUpdatedEvent.fire(Collections.singletonList(answer));
            }

        });
    }

    public boolean waitForSynchronisationComplete(long timeout) {
        return true;
    }

    public void syncFcm() {
        if (!prefs().hasAccount())
            return;

        String token = FcmRegistrationService.getFcmToken();
        if (TextUtils.isEmpty(token))
            return;

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(new SimpleRequestTask<GsonResponse>("syncFcm") {
            @Override
            protected void onFinish() {

            }

            @Override
            protected Call<GsonResponse> getRequest() {
                return api().subscribeFcm(token);
            }

            @Override
            protected void processResponse(AppData appData, GsonResponse body) {
                if (body != null && body.success) {
                    ServiceState serviceState = prefs().serviceState();
                    serviceState.fcmTokenSent = true;
                    prefs().save(serviceState);
                }
            }

        });
    }

    public void sendInvite(Contact contact) {
        sentInvites.put(contact._id, contact);

        AppData appData = data();
        api().invite(contact.phone).enqueue(new Callback<GsonResponse>() {
            @Override
            public void onResponse(Call<GsonResponse> call, Response<GsonResponse> response) {
                GsonResponse body = response.body();
                if (body != null && body.success) {
                    contact.inviteSent = true;
                    ServiceState serviceState = prefs().serviceState();
                    serviceState.lastAnswerTime = 0;
                    prefs().save(serviceState);
                    ThreadPool.DB.execute(() -> {

                        appData.contacts.save(contact);
                        onFinish();
                    });
                }
            }

            @Override
            public void onFailure(Call<GsonResponse> call, Throwable t) {
                onFinish();
            }

            protected void onFinish() {
                sentInvites.remove(contact._id);
                inviteSentEvent.fire(contact);
            }
        });
    }

    public interface NewQuestionEventHandler {
        void onNewQuestion(Question args);
    }

    public interface AnswerSentEventHandler {
        void onAnswerSent(Question args);
    }

    public interface AnswerUpdatedEventHandler {
        void onAnswerUpdated(List<Answer> args);
    }

    public interface ContactsSynchronizationEventHandler {
        void onContactsSynchronizationComplete();
    }

    public interface InviteSentEventHandler {
        void onInviteSent(Contact args);
    }
}
