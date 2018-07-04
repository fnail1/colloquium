package app.laiki.ui.main.questions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import app.laiki.R;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;
import butterknife.BindView;

import static app.laiki.utils.Utils.dpToPx;

public class AbsQuestionViewHolder extends AbsPageViewHolder {


    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView shadowTextView;
    protected final List<TextView> textLines = new ArrayList<>();
    @BindView(R.id.variant1) View variant1;
    @BindView(R.id.variant1text) TextView variant1Text;
    @BindView(R.id.variant2) View variant2;
    @BindView(R.id.variant2text) TextView variant2Text;
    @BindView(R.id.variant3) View variant3;
    @BindView(R.id.variant3text) TextView variant3Text;
    @BindView(R.id.variant4) View variant4;
    @BindView(R.id.variant4text) TextView variant4Text;
    @BindView(R.id.skip) TextView skip;
    @Nullable
    @BindView(R.id.next)
    TextView next;
    @Nullable
    @BindView(R.id.progress)
    ProgressBar progress;
    @Nullable
    @BindView(R.id.title)
    TextView title;
    protected float animationOffsetY;
    private String message;
    private boolean scheduledAnimatedRevealing;


    public AbsQuestionViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.fr_question, parent, false));
    }

    public AbsQuestionViewHolder(View root) {
        super(root);
        animationOffsetY = root.getResources().getDimensionPixelOffset(R.dimen.question_screen_item_reveal_offset);
        variant1.setBackground(new VariantButtonBackgroundDrawable(root.getContext()));
        variant3.setBackground(new VariantButtonBackgroundDrawable(root.getContext()));
        variant2.setBackground(new VariantButtonBackgroundDrawable(root.getContext()));
        variant4.setBackground(new VariantButtonBackgroundDrawable(root.getContext()));
        shadowTextView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom)
                return;
            layoutMessage();
            if (scheduledAnimatedRevealing) {
                scheduledAnimatedRevealing = false;
                animateReveal();
            }
        });
    }


    @Override
    public void animateReveal() {
        super.animateReveal();
        if (shadowTextView.getWidth() == 0) {
            scheduledAnimatedRevealing = true;
            return;
        }
        int delay = 200;
        int step = 100;

        animateLayer(icon, animationOffsetY, delay);
        delay += step;

        if (title != null) {
            animateLayer(title, animationOffsetY, delay);
            delay += step;
        }

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

        if (next != null)
            animateLayer(next, animationOffsetY, delay);
    }

    protected void animateLayer(@NonNull View view, float offset, int delay) {
        view.setTranslationY(offset);
        float alpha = view.getAlpha();
        view.setAlpha(0);

        view.animate()
                .setStartDelay(delay)
                .setDuration(300)
                .translationY(0)
                .alpha(alpha);

    }

    public void setMessage(String message) {
        this.message = message;
        layoutMessage();
    }

    private void layoutMessage() {
        int w = shadowTextView.getWidth();
        if (w == 0)
            return;

        for (TextView textLine : textLines) {
            ((ConstraintLayout) root).removeView(textLine);
        }

        textLines.clear();

        if (message == null)
            return;

        char[] chars = message.toCharArray();
        int lastWord = 0;
        int lastLine = 0;
        TextPaint paint = shadowTextView.getPaint();
        LayoutInflater inflater = LayoutInflater.from(shadowTextView.getContext());
        int anchor = getAnchor();


        for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
            char c = chars[i];
            if (!Character.isWhitespace(c))
                continue;

            if (paint.measureText(message, lastLine, i) > w) {
                if (lastWord <= lastLine)
                    lastWord = i;

                TextView tv = inflateTextView(inflater, anchor, chars, lastLine, lastWord);
                anchor = tv.getId();

                lastLine = lastWord;
            } else {
                lastWord = i;
            }

        }

        if (lastLine < message.length()) {
            if (lastWord > lastLine && paint.measureText(message, lastLine, message.length()) > w) {
                TextView tv = inflateTextView(inflater, anchor, chars, lastLine, lastWord);
                lastLine = lastWord;
                anchor = tv.getId();
            }
            TextView tv = inflateTextView(inflater, anchor, chars, lastLine, message.length());
        }

    }

    protected int getAnchor() {
        return R.id.anchor;
    }

    @NonNull
    protected TextView inflateTextView(LayoutInflater inflater, int anchor, char[] chars, int start, int end) {
        ConstraintLayout layout = (ConstraintLayout) this.root;

        ConstraintSet cset = new ConstraintSet();

        TextView tv = (TextView) inflater.inflate(R.layout.item_question_text, (ViewGroup) root, false);
        tv.setId(View.generateViewId());
        tv.setText(chars, start, end - start);

        layout.addView(tv);
        cset.clone(layout);

        if (textLines.isEmpty())
            cset.connect(tv.getId(), ConstraintSet.TOP, anchor, ConstraintSet.BOTTOM, (int) dpToPx(root.getContext(), 7));
        else
            cset.connect(tv.getId(), ConstraintSet.TOP, anchor, ConstraintSet.BOTTOM);
        cset.connect(tv.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cset.connect(tv.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cset.applyTo(layout);

        textLines.add(tv);
        return tv;
    }
}
