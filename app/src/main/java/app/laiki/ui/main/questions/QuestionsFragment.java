package app.laiki.ui.main.questions;

import android.annotation.SuppressLint;
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
import app.laiki.ui.base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static app.laiki.App.appService;
import static app.laiki.App.appState;
import static app.laiki.App.data;
import static app.laiki.App.dateTimeService;
import static app.laiki.App.networkObserver;
import static app.laiki.App.notifications;
import static app.laiki.App.prefs;
import static app.laiki.App.statistics;

public class QuestionsFragment extends BaseFragment
        implements AppService.NewQuestionEventHandler,
        QuestionViewHolder.QuestionAnsweredCallback,
        AppService.AnswerSentEventHandler,
        AppService.ContactsSynchronizationEventHandler,
        RateUsViewHolder.Callback,
        InviteViewHolder.Callback,
        StopScreenViewHolder.Callback {
    public static final String STATE_QUESTION_ID = "question_id";
    private static final Question RATE_US = new Question();
    private static final Question INVITE = new Question();
    private static final Question STOP_SCREEN = new Question();

    @BindView(R.id.page1) View page1;
    @BindView(R.id.page2) View page2;
    ProgressBar progress;
    @BindView(R.id.error) TextView error;
    @BindView(R.id.placeholders) FrameLayout placeholders;
    @BindView(R.id.counter) TextView counter;
    @BindView(R.id.root) RelativeLayout root;

    Unbinder unbinder;
    private QuestionViewHolder background;
    private QuestionViewHolder foreground;
    private Question question;
    private boolean requestSent;
    private boolean questionBindComplete;
    private View activePage;
    private boolean inviteComplete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_questions, container, false);
        unbinder = ButterKnife.bind(this, view);
        progress = placeholders.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        foreground = new QuestionViewHolder(page1, this);
        background = new QuestionViewHolder(page2, this);

        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);

        onNewQuestion(null);
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
        progress = null;
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

        if (showRateUs(activity, serviceState))
            return;

        int questionNumber = serviceState.questionNumber % prefs().config().questionsFrameSize;

        if (showInvite(activity, questionNumber))
            return;

        if (q == null || (q.variant1 == 0 && appService().getLastContactsSync() <= 0)) {
            if (question == null)
                showEmpty();

            if (q == null && !requestSent) {
                requestSent = true;
                appService().requestNextQuestion();
            }
            return;
        }

        if (q.equals(question))
            return;

        List<Contact> contacts = selectContacts(q);

        if (contacts.isEmpty()) {
            showNoContacts();
            return;
        }

        showQuestion(activity, q, serviceState, contacts, questionNumber);
    }

    private boolean showInvite(FragmentActivity activity, int questionNumber) {
        if (inviteComplete)
            return false;

        if (questionNumber != prefs().config().inviteTrigger)
            return false;

        if (question == INVITE)
            return true;

        int sent = data().contacts.countSentInvites();
        if (sent >= 5)
            return false;

        List<Contact> contacts = data().contacts.selectInviteVariants(0, 4).toList();
        if (contacts.size() < 4)
            return false;

        question = INVITE;
        InviteViewHolder inviteViewHolder = new InviteViewHolder(LayoutInflater.from(activity), root, this);

        inviteViewHolder.bind(contacts.get(0), contacts.get(1), contacts.get(2), contacts.get(3));
        statistics().questions().invite();
        showPage(inviteViewHolder);
        return true;
    }

    private void showQuestion(FragmentActivity activity, Question q, ServiceState serviceState, List<Contact> contacts, int questionNumber) {
        setupPlaceholders(false, null);

        Collections.sort(contacts, (c1, c2) -> c1.displayNameOrder.compareTo(c2.displayNameOrder));
        Contact contact1 = contacts.get(0);
        Contact contact2 = contacts.get(1 % contacts.size());
        Contact contact3 = contacts.get(2 % contacts.size());
        Contact contact4 = contacts.get(3 % contacts.size());

        bindVariants(q, contact1, contact2, contact3, contact4);

        requestSent = false;

        if (questionNumber == 0 &&
                dateTimeService().getServerTime() - serviceState.lastAnswerTime < prefs().config().deadTime) {
            if (question == STOP_SCREEN)
                return;
            question = STOP_SCREEN;

            statistics().questions().stopScreen();
            StopScreenViewHolder stopScreenViewHolder = new StopScreenViewHolder(LayoutInflater.from(activity), root, this);
            stopScreenViewHolder.bind();
            showPage(stopScreenViewHolder);
            updateCounter(false);
            return;
        }

        question = q;
        questionBindComplete = true;
        updateCounter(true);
        swapPages();
        foreground.bind(question, contact1, contact2, contact3, contact4);
        showPage(foreground);
    }

    private void bindVariants(Question q, Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        if (q.variant1 == 0) {
            q.variant1 = contact1._id;
            q.variant2 = contact2._id;
            q.variant3 = contact3._id;
            q.variant4 = contact4._id;
            ThreadPool.DB.execute(() -> data().questions.save(q));
        }
    }

    private void showNoContacts() {
        setupPlaceholders(false, "Похоже, что у вас нет контактов в телефоне. Добавьте 4-х друзей в адресную книгу и попробуйте еще разок \uD83D\uDE09");
        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);
        updateCounter(false);
    }

    @NonNull
    private List<Contact> selectContacts(Question q) {
        if (q.variant1 <= 0) {
            return data().contacts.selectRandom(4).toList();
        } else {
            return data().contacts.selectById(q.variant1, q.variant2, q.variant3, q.variant4).toList();
        }
    }

    private void showEmpty() {
        cleanupPage(page1);
        cleanupPage(page2);
        cleanupPage(activePage);
        updateCounter(false);
        String errorMessage;
        if (!networkObserver().isNetworkAvailable())
            errorMessage = "Проверьте подключение к интернету";
        else
            errorMessage = "Ждем следующий вопрос";
        setupPlaceholders(true, errorMessage);

    }

    private boolean showRateUs(FragmentActivity activity, ServiceState serviceState) {
        if (!serviceState.rateUsRequired)
            return false;
        if (question == RATE_US)
            return true;

        question = RATE_US;

        RateUsViewHolder rateUsViewHolder = new RateUsViewHolder(LayoutInflater.from(activity), root, this);
        rateUsViewHolder.bind();
        statistics().rateUs().start();
        showPage(rateUsViewHolder);
        return true;
    }


    @SuppressLint("SetTextI18n")
    private void updateCounter(boolean visible) {
        if (visible) {
            int i = prefs().serviceState().questionNumber;
            int n = prefs().config().questionsFrameSize;
            counter.setVisibility(View.VISIBLE);
            counter.setText("" + ((i % n) + 1) + "\u2009/\u2009" + n);
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
        Question q = this.question;
        if (q == null || q.variant1 == 0)
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
            notifications().onStopScreenIn();
        }

        if (!q.flags.getAndSet(Question.FLAG_ANSWERED, true)) {
            q.answer = a;
            ThreadPool.DB.execute(() -> {
                data().questions.save(q);
                appService().syncAnswers();
            });
        }
    }

    @Override
    public void onNextClick() {
        questionBindComplete = false;
        inviteComplete = question == INVITE;
        updateViews();
    }

    private void showPage(AbsPageViewHolder holder) {
        View next = holder.root;
        View prev = this.activePage;
        activePage = next;

        if (next == prev)
            return;

        if (root.indexOfChild(next) < 0) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) next.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.alignWithParent = true;
            root.addView(next);
        }

        if (prev == null) {
            next.setVisibility(View.VISIBLE);
            return;
        }

        holder.animateReveal();
        next.setVisibility(View.VISIBLE);
        for (int i = 0; i < root.getChildCount(); i++) {
            View view = root.getChildAt(i);
            if (view == next) {
                next.setAlpha(1);
                prev.animate()
                        .setStartDelay(0)
                        .setDuration(500)
                        .alpha(0)
                        .withEndAction(() -> cleanupPage(prev));
                break;
            } else if (view == prev) {
                next.setAlpha(0);
                next.animate()
                        .setStartDelay(0)
                        .setDuration(500)
                        .alpha(1)
                        .withEndAction(() -> cleanupPage(prev));
                break;
            }
        }

    }

    private void cleanupPage(View p) {
        if (p == page1 || p == page2 || p == placeholders) {
            p.setTranslationY(0);
            p.setVisibility(View.GONE);
        } else {
            root.removeView(p);
        }
    }

    @Override
    public void onAnswerSent(Question args) {
        appService().requestNextQuestion();
    }

    @Override
    public void onContactsSynchronizationComplete() {
        onNewQuestion(null);
    }

}
