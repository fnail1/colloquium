package app.laiki.ui.main.questions;

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
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.base.BaseFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.screenMetrics;
import static app.laiki.App.statistics;

public class QuestionsFragment extends BaseFragment implements AppService.NewQuestionEventHandler, QuestionViewHolder.QuestionAnsweredCallback, AppService.AnswerSentEventHandler, AppService.ContactsSynchronizationEventHandler {
    public static final String STATE_QUESTION_ID = "question_id";

    @BindView(R.id.page1) View page1;
    @BindView(R.id.page2) View page2;
    Unbinder unbinder;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.error) TextView error;
    @BindView(R.id.placeholders) FrameLayout placeholders;
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

        Question q = data().questions.selectCurrent();

        if (q != null && q.equals(question))
            return;

        activity.runOnUiThread(() -> {
            if (q == null || (q.variant1 == 0 && appService().getLastContactsSync() <= 0)) {
                page1.setVisibility(View.GONE);
                page2.setVisibility(View.GONE);
                setupPlaceholders(true, null);
                if (q == null && !requestSent) {
                    requestSent = true;
                    appService().requestNextQuestion();
                }
                return;
            }

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
            boolean animate = question != null;
            question = q;

            if (!animate) {
                foreground.root.setVisibility(View.VISIBLE);
                foreground.bind(question, contact1, contact2, contact3, contact4);
            } else {
                QuestionViewHolder t = foreground;
                foreground = background;
                background = t;
                foreground.bind(question, contact1, contact2, contact3, contact4);
                animateSwap(background.root, foreground.root);
            }
        });
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
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
            });
        }
        appService().requestNextQuestion();
    }

    @Override
    public void onContactsSynchronizationComplete() {
        onNewQuestion(null);
    }
}
