package app.laiki.ui.main.questions;

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
import app.laiki.R;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import app.laiki.ui.base.BaseFragment;
import app.laiki.ui.main.MainActivity;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.prefs;
import static app.laiki.App.screenMetrics;
import static app.laiki.App.statistics;

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
    private boolean requestSent;

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
        View.OnLayoutChangeListener onLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> onPageLayout();
        page1.addOnLayoutChangeListener(onLayoutChangeListener);
        page2.addOnLayoutChangeListener(onLayoutChangeListener);
        foreground = new QuestionViewHolder(page1, this);
        background = new QuestionViewHolder(page2, this);

        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);

        onNewQuestion(null);
    }

    private void onPageLayout() {
        foreground.bind(question);
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

        if (q != null && q.equals(question))
            return;

        activity.runOnUiThread(() -> {
            if (q == null || (q.variant1 == 0 && appService().getLastContactsSync() <= 0)) {
                page1.setVisibility(View.GONE);
                page2.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                if (q == null && !requestSent) {
                    requestSent = true;
                    appService().requestNextQuestion();
                }
                return;
            }

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
                foreground.bind(question);
                animateSwap(background.root, foreground.root);
            }
        });
    }

    @Override
    public void onQuestionAnswered(Choice a) {
        statistics().questions().answer(a);
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
    public void onContactsSynchronizationComplete() {
        onNewQuestion(null);
    }
}
