package ru.mail.colloquium.ui.main.questions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.service.AppService;
import ru.mail.colloquium.ui.base.BaseFragment;
import ru.mail.colloquium.ui.main.MainActivity;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.App.screenMetrics;

public class QuestionsFragment extends BaseFragment implements AppService.NewQuestionEventHandler, QuestionViewHolder.QuestionAnsweredCallback, AppService.AnswerSentEventHandler, AppService.ContactsSynchronizationEventHandler {
    public static final String STATE_QUESTION_ID = "question_id";

    @BindView(R.id.page1) View page1;
    @BindView(R.id.page2) View page2;
    @BindView(R.id.progress) ProgressBar progress;
    Unbinder unbinder;
    @BindView(R.id.no_questions) TextView noQuestions;
    private QuestionViewHolder background;
    private QuestionViewHolder foreground;
    private Question question;

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
        foreground = new QuestionViewHolder(page1, this);
        background = new QuestionViewHolder(page2, this);

        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);

        question = data().questions.selectCurrent();

        if (question == null || prefs().serviceState().lastSync <= 0) {
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            if (question == null)
                appService().requestNextQuestion();
        } else {
            foreground.root.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> foreground.bind(question));
            foreground.root.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            noQuestions.setVisibility(View.GONE);
        }
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

        Question q = data().questions.selectCurrent();
        if (q == null || prefs().serviceState().lastSync <= 0)
            return;

        activity.runOnUiThread(() -> {
            noQuestions.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);

            if (question == null) {
                foreground.root.setVisibility(View.VISIBLE);
                foreground.bind(question = q);
            } else {
                QuestionViewHolder t = foreground;
                foreground = background;
                background = t;
                question = q;
                foreground.root.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> foreground.bind(question));
                animateSwap(background.root, foreground.root);
            }
        });
    }

    @Override
    public void onQuestionAnswered(Choice a) {
        appService().answer(question, a);
    }

    private void animateSwap(View prev, View next) {
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
        progress.setVisibility(View.GONE);
        appService().requestNextQuestion();
    }

    @Override
    public void onContactsSynchronizationComoplete() {
        onNewQuestion(null);
    }
}
