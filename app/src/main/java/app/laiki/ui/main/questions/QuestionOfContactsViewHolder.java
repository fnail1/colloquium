package app.laiki.ui.main.questions;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.laiki.diagnostics.Logger;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.base.AnswerButtonHelper;

import static app.laiki.App.data;

public class QuestionOfContactsViewHolder extends AbsQuestionViewHolder implements AnswerButtonHelper.Callback {
    private static final int MAX_SHUFFLES = 5;

    protected final Callback callback;
    protected final Contact[] contacts = new Contact[4];
    protected final Set<View> animatingViews = new HashSet<>();

    protected Question question;
    private long shuffleStartTime;


    public QuestionOfContactsViewHolder(View root, Callback callback) {
        super(root);
        this.callback = callback;
    }

    public void bind(Question question, List<Contact> contacts) {
        this.question = question;
        for (int i = 0; i < this.contacts.length; i++) {
            Contact contact = contacts.get(i % contacts.size());
            this.contacts[i] = contact;
            variantsTextViews[i].setText(contact.displayName);
        }

        shuffle.setVisibility(question.shuffles < MAX_SHUFFLES ? View.VISIBLE : View.GONE);
    }


    class ShuffleAnimationState {
        boolean pendingOut;
        boolean dbTaskComplete;

        private synchronized void onDbTaskComplete(List<Contact> newContacts) {

            for (int i = 0; i < contacts.length; i++) {
                contacts[i] = newContacts.get(i % newContacts.size());
            }

            dbTaskComplete = true;

            if (pendingOut) {
                for (int i = 0; i < variants.length; i++) {
                    startAnimationOut(this, i, "B");
                }
            }
        }

        private synchronized void onAnimationInComplete(int index) {
            if (contacts == null)
                pendingOut = true;

            if (pendingOut)
                return;

            startAnimationOut(this, index, "A");
        }

        public void onAnimationOutComplete(View button) {
            onAnimationEnd(button);
            if (question.shuffles >= MAX_SHUFFLES) {
                shuffle.animate()
                        .setDuration(200)
                        .alpha(0)
                        .withEndAction(() -> shuffle.setVisibility(View.GONE));
            }
        }
    }

    protected void onShuffleClick() {
        if (question.shuffles >= MAX_SHUFFLES)
            return;
        shuffleStartTime = SystemClock.elapsedRealtime();

        for (View variant : variants)
            onAnimationStart(variant);

        ShuffleAnimationState animationState = new ShuffleAnimationState();

        ThreadPool.DB.execute(() -> {
            List<Contact> contacts = data().contacts.selectRandom(4).toList();
            Collections.sort(contacts, (c1, c2) -> c1.displayNameOrder.compareTo(c2.displayNameOrder));
            Contact contact1 = contacts.get(0);
            Contact contact2 = contacts.get(1 % contacts.size());
            Contact contact3 = contacts.get(2 % contacts.size());
            Contact contact4 = contacts.get(3 % contacts.size());
            question.shuffles++;
            question.bindVariants(contact1, contact2, contact3, contact4);
            animationState.onDbTaskComplete(contacts);
        });

        for (int i = 0; i < contacts.length; i++)
            startAnimationIn(animationState, i);
    }

    private void startAnimationIn(ShuffleAnimationState animationState, int index) {
        variants[index].animate()
                .setStartDelay(0)
                .setDuration(VIEW_REVEAL_ANIMATION_DURATION)
                .alpha(0)
                .withStartAction(() -> {
                    Logger.logV("SHUFFLE", "startAnimationIn " + index + " start T:" + (SystemClock.elapsedRealtime() - shuffleStartTime));
                })
                .withEndAction(() -> {
                    Logger.logV("SHUFFLE", "startAnimationIn " + index + " end T:" + (SystemClock.elapsedRealtime() - shuffleStartTime));
                    animationState.onAnimationInComplete(index);
                });
    }

    private void startAnimationOut(ShuffleAnimationState animationState, int index, String plan) {
        Logger.logV("SHUFFLE", "startAnimationOut " + index + " post T:" + (SystemClock.elapsedRealtime() - shuffleStartTime) + " plan " + plan);

        View button = variants[index];
        TextView text = variantsTextViews[index];
        Contact contact = contacts[index];
        button.setTranslationY(animationOffsetY);
        button.animate()
                .setStartDelay(VIEW_REVEAL_ANIMATION_STEP * index)
                .setDuration(VIEW_REVEAL_ANIMATION_DURATION)
                .translationY(0)
                .alpha(1)
                .withStartAction(() -> {
                    text.setText(contact.displayName);
                    Logger.logV("SHUFFLE", "startAnimationOut " + index + " start T:" + (SystemClock.elapsedRealtime() - shuffleStartTime) + " plan " + plan);
                })
                .withEndAction(() -> {
                    Logger.logV("SHUFFLE", "startAnimationOut " + index + " end T:" + (SystemClock.elapsedRealtime() - shuffleStartTime) + " plan " + plan);
                    animationState.onAnimationOutComplete(button);
                });
    }

    @Override
    public void onAnimationStart(View button) {
        animatingViews.add(button);
    }

    @Override
    public void onAnimationEnd(View button) {
        animatingViews.remove(button);
    }

    public interface Callback {
        void onQuestionAnswered(Choice a);

        void onNextClick();
    }
}
