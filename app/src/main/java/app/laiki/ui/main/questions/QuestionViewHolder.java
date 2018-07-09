package app.laiki.ui.main.questions;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;
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

        ColorScheme colorScheme = randomColorScheme(question.uniqueId.hashCode());
        root.setBackground(colorScheme.background(root.getContext()));
        icon.setBackground(colorScheme.highlight(root.getContext()));
        photos().attach(icon, question.emojiUrl)
                .size(
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size),
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size))
                .commit();
        setMessage(question.question);

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
        final VariantButtonBackgroundDrawable background = (VariantButtonBackgroundDrawable) view.getBackground();
        if (answer == null) {
            background.setState(VariantButtonBackgroundDrawable.ButtonState.DEFAULT, 1);
            view.setEnabled(true);
            view.setSelected(false);
        } else {
            view.setEnabled(false);
            VariantButtonBackgroundDrawable.ButtonState buttonState;
            if (answer != expected) {
                buttonState = VariantButtonBackgroundDrawable.ButtonState.DISABLED;
                view.setSelected(false);
            } else {
                buttonState = VariantButtonBackgroundDrawable.ButtonState.SELECTED;
                view.setSelected(true);
            }

            if (background.getButtonState() != buttonState) {
                Animation a = new Animation() {
                    private boolean stateAnimationComplete;
                    private float maxScaleAmplitude = (float) (screenMetrics().screen.width - view.getWidth()) / view.getWidth();

                    @Override
                    protected void applyTransformation(float alpha, Transformation t) {
                        super.applyTransformation(alpha, t);
                        float alphaButtonState = (alpha - .0f) / .3f;
                        float amplitude = maxScaleAmplitude * (1 - alpha) * (1 - alpha);
                        float scale = 1 - (float) (amplitude * Math.sin(alpha * 8 * Math.PI));

//                        trace("A=%.3f, state=%.3f, scale=%.3f", alpha, alphaButtonState, scale);

                        if (.0 < alphaButtonState && alphaButtonState <= 1) {
                            background.setState(buttonState, alphaButtonState);
                        } else if (!stateAnimationComplete && alphaButtonState >= 1) {
                            background.setState(buttonState, 1);
                            stateAnimationComplete = true;
                        }
                        if (buttonState == VariantButtonBackgroundDrawable.ButtonState.SELECTED) {
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                    }
                };
                if (buttonState == VariantButtonBackgroundDrawable.ButtonState.SELECTED) {
                    a.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.setScaleX(1);
                            view.setScaleY(1);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                a.setInterpolator(new LinearInterpolator());
                a.setDuration(1500);
                view.startAnimation(a);
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
