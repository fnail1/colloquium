package app.laiki.ui.base;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;

import app.laiki.model.entities.Contact;
import app.laiki.model.types.Choice;
import app.laiki.ui.main.questions.QuestionViewHolder;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;

import static app.laiki.App.screenMetrics;

public class AnswerButtonHelper {

    public static void bindVariant(Choice answer, Choice expected, View view, TextView textView, Contact contact, Callback callback) {
        final VariantButtonBackgroundDrawable background = (VariantButtonBackgroundDrawable) view.getBackground();
        textView.setText(contact.displayName);

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
                if (callback != null) {
                    callback.onAnimationStart(view);
                }
                Animation a = new Animation() {
                    private boolean stateAnimationComplete;
                    private float maxScaleAmplitude = (float) (screenMetrics().screen.width - view.getWidth()) / view.getWidth();

                    @Override
                    protected void applyTransformation(float alpha, Transformation t) {
                        super.applyTransformation(alpha, t);
                        float alphaButtonState = (alpha - .0f) / .3f;

                        if (.0 < alphaButtonState && alphaButtonState <= 1) {
                            background.setState(buttonState, alphaButtonState);
                        } else if (!stateAnimationComplete && alphaButtonState >= 1) {
                            background.setState(buttonState, 1);
                            stateAnimationComplete = true;
                        }
                        if (buttonState == VariantButtonBackgroundDrawable.ButtonState.SELECTED) {
                            float amplitude = maxScaleAmplitude * (1 - alpha) * (1 - alpha);
                            float scale = 1 - (float) (amplitude * Math.sin(alpha * 8 * Math.PI));
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
                            if (callback != null) {
                                callback.onAnimationEnd(view);
                            }
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
    }

    public interface Callback{
        void onAnimationStart(View button);
        void onAnimationEnd(View button);
    }
}
