package app.laiki.ui.main.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;

import java.util.List;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;
import butterknife.BindView;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.screenMetrics;
import static app.laiki.App.statistics;
import static app.laiki.toolkit.collections.Query.query;

public class InviteViewHolder extends QuestionOfContactsViewHolder {
    public static final int ANIMATION_DURATION = 1500;

    @BindView(R.id.subtitle) TextView subtitle;
    private SelectedVariantAnimationState selectedVariantAnimationState = SelectedVariantAnimationState.IDLE;

    public InviteViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater.inflate(R.layout.fr_question_invite, parent, false), callback);
        shuffle.setVisibility(View.GONE);
    }

    @Override
    public void bind(Question question, List<Contact> contacts) {
        super.bind(question, contacts);

        ColorScheme colorScheme = randomColorScheme();
        root.setBackground(colorScheme.background(root.getContext()));
        icon.setBackground(colorScheme.highlight(root.getContext()));
        icon.setImageResource(R.drawable.ic_question_invite);

        setMessage("Party time!\n" +
                "Кого позовешь в ЧСН?");

    }

    @Override
    public void animateReveal() {
        if (shadowTextView.getWidth() == 0) {
            scheduledAnimatedRevealing = true;
            return;
        }
        int delay = 150;
        int step = 50;

        animateLayer(icon, animationOffsetY, delay);
        delay += step;

        if (title != null) {
            animateLayer(title, animationOffsetY, delay);
            delay += step;
        }

//        animateLayer(shadowTextView, animationOffsetY, delay);
//        delay += step;

        for (TextView textLine : textLines) {
            animateLayer(textLine, animationOffsetY, delay);
            delay += step;
        }

        animateLayer(variant1, animationOffsetY, delay);
        delay += step;

        animateLayer(variant2, animationOffsetY, delay);
        delay += step;

        animateLayer(variant3, animationOffsetY, delay);
        delay += step;

        animateLayer(variant4, animationOffsetY, delay);
        delay += step;

        animateLayer(subtitle, animationOffsetY, delay);
        delay += step;

        if (skip != null)
            animateLayer(skip, animationOffsetY, delay);
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.shuffle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                onContactsSelected(Choice.A);
                break;
            case R.id.variant2:
                onContactsSelected(Choice.B);
                break;
            case R.id.variant3:
                onContactsSelected(Choice.C);
                break;
            case R.id.variant4:
                onContactsSelected(Choice.D);
                break;
            case R.id.skip:
                callback.onNextClick();
                break;
            case R.id.shuffle:
                onShuffleClick();
                break;
        }
    }

    private void onContactsSelected(Choice choice) {
        statistics().contacts().inviteSent();
        Contact contact = contacts[choice.ordinal()];
        for (View variant : variants) {
            variant.setEnabled(false);
        }

        appService().contactUpdated.add(new AppService.ContactUpdatedEventHandler() {
            @Override
            public void onContactUpdated(Contact args) {
                if (args.equals(contact) && args.flags.get(Contact.FLAG_INVITE_REQUESTED)) {
                    appService().contactUpdated.remove(this);
                    onInviteRequested(args, choice);
                }
            }
        });
        appService().sendInvite(contact);
        animateSelectedVariant(choice);
    }

    private synchronized void onInviteRequested(Contact args, Choice choice) {
        List<Contact> candidates = data().contacts.selectInviteVariants(0, 5).toList();
        next:
        for (Contact c : candidates) {
            for (Contact existing : contacts) {
                if (c.equals(existing))
                    continue next;
            }

            root.post(() -> {
                contacts[choice.ordinal()] = c;
                bindContactWithAnimation(choice);

            });
            break;
        }

        if (query(contacts).first(c -> !c.flags.get(Contact.FLAG_INVITE_REQUESTED)) == null)
            root.post(callback::onNextClick);
    }

    private void animateSelectedVariant(Choice choice) {
        View view = variants[choice.ordinal()];

        selectedVariantAnimationState = SelectedVariantAnimationState.IN;
        final VariantButtonBackgroundDrawable background = (VariantButtonBackgroundDrawable) view.getBackground();

        Animation a = new Animation() {
            private boolean stateAnimationComplete;
            private float maxScaleAmplitude = (float) (screenMetrics().screen.width - view.getWidth()) / view.getWidth();
            VariantButtonBackgroundDrawable.ButtonState buttonState = VariantButtonBackgroundDrawable.ButtonState.SELECTED;

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

                float amplitude = maxScaleAmplitude * (1 - alpha) * (1 - alpha);
                float scale = 1 - (float) (amplitude * Math.sin(alpha * 8 * Math.PI));
                view.setScaleX(scale);
                view.setScaleY(scale);

            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setScaleX(1);
                view.setScaleY(1);
                selectedVariantAnimationState = SelectedVariantAnimationState.SELECTED;
                bindContactWithAnimation(choice);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        a.setInterpolator(new LinearInterpolator());
        a.setDuration(1500);
        view.startAnimation(a);
    }

    private void bindContactWithAnimation(Choice choice) {
        if (selectedVariantAnimationState != SelectedVariantAnimationState.SELECTED)
            return;
        Contact c = contacts[choice.ordinal()];

        if (c.flags.get(Contact.FLAG_INVITE_REQUESTED))
            return;

        selectedVariantAnimationState = SelectedVariantAnimationState.OUT_STAGE1;

        final VariantButtonBackgroundDrawable background = (VariantButtonBackgroundDrawable) variants[choice.ordinal()].getBackground();

        TextView v = variantsTextViews[choice.ordinal()];


        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float alpha, Transformation t) {
                super.applyTransformation(alpha, t);
                if (alpha < .5f) {
                    v.setAlpha(1 - 2 * alpha);
                    background.setState(VariantButtonBackgroundDrawable.ButtonState.DEFAULT, alpha * 2);
                    return;
                }

                if (selectedVariantAnimationState == SelectedVariantAnimationState.OUT_STAGE1) {
                    v.setAlpha(0);
                    v.setText(c.displayName);
                    background.setState(VariantButtonBackgroundDrawable.ButtonState.DEFAULT, 1);
                    selectedVariantAnimationState = SelectedVariantAnimationState.OUT_STAGE2;
                }

                if (selectedVariantAnimationState == SelectedVariantAnimationState.OUT_STAGE2) {
                    v.setAlpha(alpha * 2 - 1.0f);
                }
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setAlpha(1);
                for (View variant : variants) {
                    variant.setEnabled(true);
                }
                selectedVariantAnimationState = SelectedVariantAnimationState.IDLE;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        a.setDuration(ANIMATION_DURATION);
        v.startAnimation(a);

//        background.setState(VariantButtonBackgroundDrawable.ButtonState.DEFAULT, 1);
//        v.setText(c.displayName);
//        v.setAlpha(1);
//        for (View variant : variants) {
//            variant.setEnabled(true);
//        }
//        selectedVariantAnimationState = SelectedVariantAnimationState.IDLE;
    }

    private enum SelectedVariantAnimationState {
        IN, SELECTED, OUT_STAGE1, OUT_STAGE2, IDLE

    }
}
