package app.laiki.ui.main.questions;

import android.view.View;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import butterknife.OnClick;

import static app.laiki.App.photos;
import static app.laiki.App.screenMetrics;

@SuppressWarnings("ConstantConditions")
public class QuestionViewHolder extends AbsQuestionViewHolder {

    private final QuestionAnsweredCallback callback;

    private Question question;
    private Contact contact1;
    private Contact contact2;
    private Contact contact3;
    private Contact contact4;

    /**
     * @param callback
     * @param root     R.layout.fr_question
     */
    @SuppressWarnings("WeakerAccess")
    public QuestionViewHolder(View root, QuestionAnsweredCallback callback) {
        super(root);
        this.callback = callback;
        root.setOnClickListener(this::onViewClicked);

//        R.layout.fr_question
    }

    public void bind(Question question, Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        this.question = question;
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.contact3 = contact3;
        this.contact4 = contact4;

        rebind();
    }

    public void rebind() {
        if (question == null)
            return;

        root.setBackground(randomBackground(root.getContext(), question.uniqueId.hashCode()));
        photos().attach(icon, question.emojiUrl)
                .size(
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size),
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size))
                .commit();
        message.setText(question.question);

        if (question.answer == null) {
            skip.setVisibility(View.VISIBLE);
            next.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        } else {
            skip.setVisibility(View.GONE);
            if (question.answer == Choice.E) {
                next.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            } else {
                if (progress.getVisibility() == View.GONE)
                    next.setVisibility(View.VISIBLE);
                else
                    next.setVisibility(View.GONE);

            }
        }

        bindVariant(question.answer, Choice.A, variant1, variant1Text, contact1);
        bindVariant(question.answer, Choice.B, variant2, variant2Text, contact2);
        bindVariant(question.answer, Choice.C, variant3, variant3Text, contact3);
        bindVariant(question.answer, Choice.D, variant4, variant4Text, contact4);
    }

    private void bindVariant(Choice answer, Choice expected, View view, TextView textView, Contact contact) {
        if (answer == null) {
            view.setAlpha(1f);
            view.setEnabled(true);
            view.setSelected(false);
        } else {
            view.setEnabled(false);
            if (answer != expected) {
                view.setAlpha(.5f);
                view.setSelected(false);
            } else {
                view.setAlpha(1f);
                view.setSelected(true);
            }
        }
        textView.setText(contact.displayName);
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                callback.onQuestionAnswered(Choice.A);
                onAnswer();
                break;
            case R.id.variant2:
                callback.onQuestionAnswered(Choice.B);
                onAnswer();
                break;
            case R.id.variant3:
                callback.onQuestionAnswered(Choice.C);
                onAnswer();
                break;
            case R.id.variant4:
                callback.onQuestionAnswered(Choice.D);
                onAnswer();
                break;
            case R.id.skip:
                callback.onQuestionAnswered(Choice.E);
                //no break;
            case R.id.next:
            case R.id.root:
            case R.id.page1:
            case R.id.page2:
                if (question.answer != null) {
                    callback.onNextClick();
                    progress.setVisibility(View.VISIBLE);
                    rebind();
                }
                break;
        }
    }

    private void onAnswer() {
        rebind();
        next.setAlpha(0);
        next.setTranslationY(screenMetrics().screen.height - next.getY());
        next.animate()
                .setStartDelay(0)
                .setDuration(500)
                .alpha(1)
                .translationY(0);
    }

    public interface QuestionAnsweredCallback {
        void onQuestionAnswered(Choice a);

        void onNextClick();
    }
}
