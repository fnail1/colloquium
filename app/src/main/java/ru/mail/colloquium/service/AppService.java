package ru.mail.colloquium.service;

import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.api.model.GsonQuestionResponse;
import ru.mail.colloquium.api.model.GsonResponse;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.service.ab.AddressBookSyncHelper;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.events.ObservableEvent;
import ru.mail.colloquium.toolkit.http.ServerException;

import static ru.mail.colloquium.App.api;
import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.dateTimeService;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class AppService implements AppStateObserver.AppStateEventHandler {
    public static final long MAX_SYNCHRONIZATION_LAG = 60 * 60 * 1000;
    private final AppStateObserver appStateObserver;

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

    public final ObservableEvent<AnswerUpdatedEventHandler, AppService, Question> answerUpdatedEvent = new ObservableEvent<AnswerUpdatedEventHandler, AppService, Question>(this) {
        @Override
        protected void notifyHandler(AnswerUpdatedEventHandler handler, AppService sender, Question args) {
            handler.onAnswerUpdated(args);
        }
    };

    public AppService(AppStateObserver appStateObserver) {
        this.appStateObserver = appStateObserver;
        appStateObserver.stateEvent.add(this);
    }

    public void shutdown() {
        appStateObserver.stateEvent.remove(this);
    }

    @Override
    public void onAppStateChanged() {
        if (!prefs().hasAccount())
            return;

        if (!appStateObserver.isForeground())
            return;

        if (dateTimeService().getServerTime() - prefs().serviceState().lastSync <= MAX_SYNCHRONIZATION_LAG)
            return;

        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOW).execute(() -> {
            AddressBookSyncHelper.doSync(app());
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
                        MergeHelper.merge(question, body.question);
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
                answerUpdatedEvent.fire(null);
            }

        });
    }

    public void answer(Question question, @NonNull Choice answer) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new AbsRequestTask("answer") {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                if (!question.flags.getAndSet(Question.FLAG_ANSWERED, true)) {
                    question.answer = answer;
                    appData.questions.save(question);
                }
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
            String p1 = contacts.get(question.variant1).serverId;
            String p2 = contacts.get(question.variant2).serverId;
            String p3 = contacts.get(question.variant3).serverId;
            String p4 = contacts.get(question.variant4).serverId;

            Response<GsonResponse> response = api().answer(question.serverId, question.answer, p1, p2, p3, p4).execute();
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

    public void answerViewed(Answer answer) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new SimpleRequestTask("answerViewed") {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                if (!answer.flags.getAndSet(Answer.FLAG_VIEWED, true)) {
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
                answerUpdatedEvent.fire(null);
            }

        });
    }

    public boolean waitForSynchronisationComplete(long timeout) {
        return true;
    }

    public interface NewQuestionEventHandler {
        void onNewQuestion(Question args);
    }

    public interface AnswerSentEventHandler {
        void onAnswerSent(Question args);
    }

    public interface AnswerUpdatedEventHandler {
        void onAnswerUpdated(Question args);
    }
}
