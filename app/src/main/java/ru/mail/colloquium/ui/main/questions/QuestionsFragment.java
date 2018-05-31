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
import ru.mail.colloquium.service.AppService;
import ru.mail.colloquium.ui.base.BaseFragment;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;

public class QuestionsFragment extends BaseFragment implements AppService.NewQuestionEventHandler, QuestionViewHolder.QuestionAnsweredCallback {
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

        if (savedInstanceState == null) {
            question = data().questions.selectCurrent();
        } else {
            long qid = savedInstanceState.getLong(STATE_QUESTION_ID);
            question = data().questions.selectById(qid);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_QUESTION_ID, question._id);
    }

    @Override
    public void onResume() {
        super.onResume();
        appService().newQuestionEvent.add(this);
        if (question == null) {
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            appService().requestNextQuestion();
        } else {
            foreground.bind(question);
            foreground.root.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            noQuestions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        appService().newQuestionEvent.remove(this);
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

        activity.runOnUiThread(() -> {
            noQuestions.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);

            if (question == null) {
                question = data().questions.selectCurrent();
                if (question != null) {
                    foreground.root.setVisibility(View.VISIBLE);
                    foreground.bind(question);
                } else {
                    noQuestions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onQuestionAnswered() {
        question = null;
        appService().requestNextQuestion();
        question = data().questions.selectCurrent();
        if (question != null) {
            QuestionViewHolder t = this.foreground;
            foreground = background;
            background = t;
            foreground.root.setVisibility(View.VISIBLE);
            foreground.bind(question);

            background.root.animate()
                    .setDuration(500)
                    .translationY(-background.root.getHeight())
                    .withEndAction(() -> {
                        background.root.setTranslationY(0);
                        background.root.setVisibility(View.GONE);
                    });

            foreground.root.setTranslationY(foreground.root.getHeight());
            foreground.root.animate()
                    .setDuration(500)
                    .translationY(0);
        }
    }
}
