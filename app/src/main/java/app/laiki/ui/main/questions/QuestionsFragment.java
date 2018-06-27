package app.laiki.ui.main.questions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import app.laiki.service.ServiceState;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.ContactsActivity;
import app.laiki.ui.base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
import static app.laiki.App.appService;
import static app.laiki.App.appState;
import static app.laiki.App.data;
import static app.laiki.App.dateTimeService;
import static app.laiki.App.notifications;
import static app.laiki.App.prefs;
import static app.laiki.App.screenMetrics;
import static app.laiki.App.statistics;

public class QuestionsFragment extends BaseFragment
        implements AppService.NewQuestionEventHandler,
        QuestionViewHolder.QuestionAnsweredCallback,
        AppService.AnswerSentEventHandler,
        AppService.ContactsSynchronizationEventHandler,
        RateUsViewHolder.Callback {
    public static final String STATE_QUESTION_ID = "question_id";

    @BindView(R.id.page1) View page1;
    @BindView(R.id.page2) View page2;
    Unbinder unbinder;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.error) TextView error;
    @BindView(R.id.placeholders) FrameLayout placeholders;
    @BindView(R.id.timer) TextView timer;
    @BindView(R.id.contacts) TextView contacts;
    @BindView(R.id.stopscreen) FrameLayout stopscreen;
    @BindView(R.id.counter) TextView counter;
    @BindView(R.id.root) RelativeLayout root;
    private QuestionViewHolder background;
    private QuestionViewHolder foreground;
    private Question question;
    private boolean requestSent;
    private boolean questionBindComplete;
    private View activePage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_questions, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        View.OnLayoutChangeListener onLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                onPageLayout();
            }
        };
        page1.addOnLayoutChangeListener(onLayoutChangeListener);
        page2.addOnLayoutChangeListener(onLayoutChangeListener);
        foreground = new QuestionViewHolder(page1, this);
        background = new QuestionViewHolder(page2, this);

        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);

        onNewQuestion(null);
    }

    private void onPageLayout() {
        foreground.rebind();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_QUESTION_ID, question != null ? question._id : 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        appService().newQuestionEvent.add(this);
        appService().answerSentEvent.add(this);
        appService().contactsSynchronizationEvent.add(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        appService().newQuestionEvent.remove(this);
        appService().answerSentEvent.remove(this);
        appService().contactsSynchronizationEvent.remove(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onNewQuestion(Question args) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        if (!questionBindComplete) {
            activity.runOnUiThread(this::updateViews);
        }
    }

    private void updateViews() {
        updateViews(data().questions.selectCurrent());
    }

    private void updateViews(Question q) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        ServiceState serviceState = prefs().serviceState();

        if (serviceState.rateUsRequired) {
            if (question == Question.RATE_US)
                return;
            question = Question.RATE_US;

            RateUsViewHolder rateUsViewHolder = new RateUsViewHolder(LayoutInflater.from(activity), root, this);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rateUsViewHolder.root.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.alignWithParent = true;
            root.addView(rateUsViewHolder.root);
            rateUsViewHolder.bind();
            statistics().rateUs().start();
            showPage(rateUsViewHolder.root);
            return;
        }

        if (q == null || (q.variant1 == 0 && appService().getLastContactsSync() <= 0)) {
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.GONE);
            updateCounter(false);
            setupPlaceholders(true, null);
            if (q == null && !requestSent) {
                requestSent = true;
                appService().requestNextQuestion();
            }
            return;
        }

        if (q.equals(question))
            return;

        List<Contact> contacts;
        if (q.variant1 <= 0) {
            contacts = data().contacts.selectRandom(4).toList();
        } else {
            contacts = data().contacts.selectById(q.variant1, q.variant2, q.variant3, q.variant4).toList();
        }

        if (contacts.isEmpty()) {
            setupPlaceholders(false, "Похоже, что у вас нет контактов в телефоне. Добавьте 4-х друзей в адресную книгу и попробуйте еще разок \uD83D\uDE09");
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.GONE);
            updateCounter(false);
            return;
        }

        setupPlaceholders(false, null);

        Collections.sort(contacts, (c1, c2) -> c1.displayNameOrder.compareTo(c2.displayNameOrder));
        Contact contact1 = contacts.get(0);
        Contact contact2 = contacts.get(1 % contacts.size());
        Contact contact3 = contacts.get(2 % contacts.size());
        Contact contact4 = contacts.get(3 % contacts.size());

        if (q.variant1 == 0) {
            q.variant1 = contact1._id;
            q.variant2 = contact2._id;
            q.variant3 = contact3._id;
            q.variant4 = contact4._id;
            ThreadPool.DB.execute(() -> data().questions.save(q));
        }


        requestSent = false;
        question = q;
        questionBindComplete = true;

        int N = prefs().config().questionsFrameSize;
        int n = serviceState.questionNumber % N;
        if (n == 0 &&
                dateTimeService().getServerTime() - serviceState.lastAnswerTime < prefs().config().deadTime) {
            background.bind(question, contact1, contact2, contact3, contact4);
            updateTimer();

            showPage(stopscreen);
            updateCounter(false);
            return;
        }

        updateCounter(true);
        swapPages();
        foreground.bind(question, contact1, contact2, contact3, contact4);
        showPage(foreground.root);
    }

    private void updateTimer() {
        if (timer == null)
            return;
        long timeSpan = prefs().config().deadTime - (dateTimeService().getServerTime() - prefs().serviceState().lastAnswerTime);
        if (timeSpan > 0) {
            timer.setText(dateTimeService().formatTime(timeSpan, false));
            timer.postDelayed(this::updateTimer, 1000);
        } else {
            swapPages();
            showPage(foreground.root);
            updateCounter(true);
        }
    }

    private void updateCounter(boolean visible) {
        if (visible) {
            int i = prefs().serviceState().questionNumber;
            int n = prefs().config().questionsFrameSize;
            counter.setVisibility(View.VISIBLE);
            counter.setText("" + ((i % n) + 1) + "/" + n);
        } else {
            counter.setVisibility(View.GONE);
        }
    }

    private void swapPages() {
        QuestionViewHolder t = foreground;
        foreground = background;
        background = t;
    }

    private void setupPlaceholders(boolean progress, String error) {
        if (TextUtils.isEmpty(error) && !progress) {
            placeholders.setVisibility(View.GONE);
            return;
        }

        placeholders.setVisibility(View.VISIBLE);

        if (progress) {
            this.progress.setVisibility(View.VISIBLE);
        } else {
            this.progress.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(error)) {
            this.error.setVisibility(View.VISIBLE);
            this.error.setText(error);
        } else {
            this.error.setVisibility(View.GONE);
        }

    }


    @Override
    public void onQuestionAnswered(Choice a) {
        if (question.variant1 == 0)
            return;

        appState().numberOfAnswers++;
        ServiceState serviceState = prefs().serviceState();
        serviceState.lastAnswerTime = dateTimeService().getServerTime();
        serviceState.questionNumber++;
        if (!serviceState.rateUsComplete && serviceState.sessionNumber > 0 && appState().numberOfAnswers == 1) {
            serviceState.rateUsRequired = true;
        }
        prefs().save(serviceState);

        statistics().questions().answer(a);

        if (serviceState.questionNumber % prefs().config().questionsFrameSize == 0) {
            statistics().questions().stopScreen();
            notifications().onStopScreenIn();
        }

        if (!question.flags.getAndSet(Question.FLAG_ANSWERED, true)) {
            question.answer = a;
            ThreadPool.DB.execute(() -> {
                data().questions.save(question);
                appService().syncAnswers(question, a);
            });
        }
    }

    @Override
    public void onNextClick() {
        questionBindComplete = false;
        updateViews();
    }

    private void showPage(View next) {
        View prev = this.activePage;
        activePage = next;

        if (next == prev)
            return;

        if (prev == null) {
            next.setVisibility(View.VISIBLE);
            return;
        }

        prev.animate()
                .setDuration(500)
                .translationY(-screenMetrics().screen.height)
                .withEndAction(() -> {
                    prev.setTranslationY(0);
                    prev.setVisibility(View.GONE);
                });

        next.setVisibility(View.VISIBLE);
        next.setTranslationY(screenMetrics().screen.height);
        next.animate()
                .setDuration(500)
                .translationY(0);
    }

    @Override
    public void onAnswerSent(Question args) {
//        FragmentActivity activity = getActivity();
//        if (activity != null) {
//            activity.runOnUiThread(() -> {
//                foreground.rebind();
//            });
//        }
        appService().requestNextQuestion();
    }

    @Override
    public void onContactsSynchronizationComplete() {
        onNewQuestion(null);
    }

    @OnClick(R.id.contacts)
    public void onViewClicked() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            statistics().questions().contacts();
            startActivity(new Intent(activity, ContactsActivity.class));
        }
    }
}
