package app.laiki.ui.main.questions;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.laiki.R;
import app.laiki.diagnostics.Logger;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.base.AnswerButtonHelper;
import butterknife.OnClick;

import static app.laiki.App.data;
import static app.laiki.App.photos;
import static app.laiki.App.screenMetrics;

@SuppressWarnings("ConstantConditions")
public class QuestionViewHolder extends AbsQuestionViewHolder implements AnswerButtonHelper.Callback {

    private static final int SHUFFLE_ANIMATION_STEP = 150;
    public static final int SHUFFLE_ANIMATION_DURATION = 350;
    private static final int MAX_SHUFFLES = 5;
    private final QuestionAnsweredCallback callback;

    private Question question;
    private final Contact[] contacts = new Contact[4];
    private final Set<View> animatingViews = new HashSet<>();
    private long shuffleStartTime;

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

    public void bind(Question question, List<Contact> contacts) {
        this.question = question;
        for (int i = 0; i < this.contacts.length; i++) {
            this.contacts[i] = contacts.get(i % contacts.size());
        }
        shuffle.setVisibility(question.shuffles < MAX_SHUFFLES ? View.VISIBLE : View.GONE);
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

        Choice[] values = {Choice.A, Choice.B, Choice.C, Choice.D};
        for (int i = 0; i < values.length; i++) {
            AnswerButtonHelper.bindVariant(question.answer, values[i], variants[i], variantsTextViews[i], contacts[i], this);
        }

    }


    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next, R.id.shuffle})
    public void onViewClicked(View view) {
        if (!animatingViews.isEmpty())
            return;

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
            case R.id.shuffle:
                onShuffleClick();
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


    class ShuffleAnimationState {
        boolean pendingOut;
        boolean dbTaskComplete;

        private synchronized void onDbTaskComplete(List<Contact> contacts) {

            for (int i = 0; i < QuestionViewHolder.this.contacts.length; i++) {
                QuestionViewHolder.this.contacts[i] = contacts.get(i % contacts.size());
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

    private void onShuffleClick() {
        if (question.shuffles >= MAX_SHUFFLES)
            return;
        shuffleStartTime = SystemClock.elapsedRealtime();

        onAnimationStart(variant1);
        onAnimationStart(variant2);
        onAnimationStart(variant3);
        onAnimationStart(variant4);

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
                .setStartDelay(SHUFFLE_ANIMATION_STEP * index)
                .setDuration(SHUFFLE_ANIMATION_DURATION)
                .scaleX(0)
                .scaleY(0)
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
        button.animate()
                .setStartDelay(0)
                .setDuration(SHUFFLE_ANIMATION_DURATION)
                .scaleX(1)
                .scaleY(1)
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

    @Override
    public void onAnimationStart(View button) {
        animatingViews.add(button);
    }

    @Override
    public void onAnimationEnd(View button) {
        animatingViews.remove(button);
    }

    public interface QuestionAnsweredCallback {
        void onQuestionAnswered(Choice a);

        void onNextClick();
    }
}



/*

In 0 start T:10
In 1 start T:225
In 2 start T:427
In 0 end T:525
Out 0 start T:544
In 3 start T:625
In 1 end T:726
In 2 end T:925
Out 1 start T:945
Out 0 end T:1043
In 3 end T:1125
Out 2 start T:1345
Out 1 end T:1444
Out 3 start T:1747
Out 2 end T:1842
Out 3 end T:2242

0000 v
0050 v
0100 v
0150 v
0200 vv
0250 vv
0300 vv
0350 vv
0400 vvv
0450 vvv
0500 Xvv
0550 ^vv
0600 ^vvv
0650 ^vvv
0700 ^vvv
0750 ^ vv
0800 ^ vv
0850 ^ vv
0900 ^^vv
0950 ^^ v
1000 ^^ v
1050  ^ v
1100  ^ v
1150  ^
1200  ^
1250  ^
1300  ^^
1350  ^^
1400  ^^
1450   ^
1500   ^
1550   ^
1600   ^
1650   ^
1700   ^^
1750   ^^
1800   ^^
1850    ^
1900    ^
1950






*/