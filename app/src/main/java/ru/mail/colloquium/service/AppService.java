package ru.mail.colloquium.service;

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
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.service.ab.AddressBookSyncHelper;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.events.ObservableEvent;
import ru.mail.colloquium.toolkit.http.ServerException;
import ru.mail.colloquium.ui.base.BaseActivity;

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

    public final ObservableEvent<FeedbackUpdatedEventHandler, AppService, Void> feedbackUpdatedEvent = new ObservableEvent<FeedbackUpdatedEventHandler, AppService, Void>(this) {
        @Override
        protected void notifyHandler(FeedbackUpdatedEventHandler handler, AppService sender, Void args) {
            handler.onFeedbackUpdated();
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
                return api().feedback();
            }

            @Override
            protected void processResponse(AppData appData, GsonAnswers body) {
                MergeHelper.merge(appData, body.answers);
            }

            @Override
            protected void onFinish() {
                feedbackUpdatedEvent.fire(null);
            }

        });
    }

    public void syncAnsweredQuestions() {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new AbsRequestTask("syncAnsweredQuestions") {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                syncAnsweredQuestionsSync(appData);
            }

            @Override
            protected void onFinish() {
                feedbackUpdatedEvent.fire(null);
            }

        });
    }

    private void syncAnsweredQuestionsSync(AppData appData) throws IOException, ServerException {
        List<Question> questions = appData.questions.selectToSend().toList();
        for (Question question : questions) {
            Response<GsonResponse> response = api().answer(question.serverId, question.selectedPhone, question.allPhones).execute();
            switch (response.code()) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    throw new ServerException(response);
                default:
                    safeThrow(new ServerException(response));
                    break;
            }

            question.flags.set(Question.FLAG_SENT);
            appData.questions.save(question);
        }
    }

    public void answer(Question question, String allPhones, String selectedPhone) {
        ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.MEDIUM).execute(new AbsRequestTask("answer") {

            @Override
            protected void performRequest(AppData appData) throws IOException, ServerException {
                if (!question.flags.getAndSet(Question.FLAG_ANSWERED, true)) {
                    question.allPhones = allPhones;
                    question.selectedPhone = selectedPhone;
                    appData.questions.save(question);
                }
                syncAnsweredQuestionsSync(appData);
            }

            @Override
            protected void onFinish() {
                feedbackUpdatedEvent.fire(null);
            }

        });
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
                feedbackUpdatedEvent.fire(null);
            }

        });
    }

    public boolean waitForSynchronisationComplete(long timeout) {
        return true;
    }

    public interface NewQuestionEventHandler {
        void onNewQuestion(Question args);
    }

    public interface FeedbackUpdatedEventHandler {
        void onFeedbackUpdated();
    }
}
