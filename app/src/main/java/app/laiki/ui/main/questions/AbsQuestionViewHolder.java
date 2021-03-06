package app.laiki.ui.main.questions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.laiki.R;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;
import butterknife.BindView;

import static app.laiki.utils.Utils.dpToPx;

public abstract class AbsQuestionViewHolder extends AbsPageViewHolder {


    public static final int VIEW_REVEAL_ANIMATION_DURATION = 300;
    static final int VIEW_REVEAL_ANIMATION_STEP = 50;
    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView shadowTextView;
    @BindView(R.id.header) ViewGroup header;
    @BindView(R.id.variant1) View variant1;
    @BindView(R.id.variant1text) TextView variant1Text;
    @BindView(R.id.variant2) View variant2;
    @BindView(R.id.variant2text) TextView variant2Text;
    @BindView(R.id.variant3) View variant3;
    @BindView(R.id.variant3text) TextView variant3Text;
    @BindView(R.id.variant4) View variant4;
    @BindView(R.id.variant4text) TextView variant4Text;
    @BindView(R.id.skip) View skip;
    @Nullable
    @BindView(R.id.next)
    TextView next;
    @Nullable
    @BindView(R.id.progress)
    ProgressBar progress;
    @Nullable
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.shuffle) View shuffle;

    protected float animationOffsetY;
    protected String message;
    protected boolean scheduledAnimatedRevealing;
    protected final List<TextView> textLines = new ArrayList<>();
    protected final View[] variants;
    protected final TextView[] variantsTextViews;


    public AbsQuestionViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.fr_question, parent, false));
    }

    public AbsQuestionViewHolder(View root) {
        super(root);
        animationOffsetY = root.getResources().getDimensionPixelOffset(R.dimen.question_screen_item_reveal_offset);
        variants = new View[]{variant1, variant2, variant3, variant4};
        variantsTextViews = new TextView[]{variant1Text, variant2Text, variant3Text, variant4Text};
        for (View variant : variants) {
            variant.setBackground(new VariantButtonBackgroundDrawable(root.getContext()));
        }

        shadowTextView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left == oldLeft && right == oldRight)
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
        int delay = 150;

        animateLayer(icon, animationOffsetY, delay);
        delay += VIEW_REVEAL_ANIMATION_STEP;

        if (title != null) {
            animateLayer(title, animationOffsetY, delay);
            delay += VIEW_REVEAL_ANIMATION_STEP;
        }


        for (TextView textLine : textLines) {
            animateLayer(textLine, animationOffsetY, delay);
            delay += VIEW_REVEAL_ANIMATION_STEP;
        }

        for (View variant : variants) {
            animateLayer(variant, animationOffsetY, delay);
            delay += VIEW_REVEAL_ANIMATION_STEP;
        }

        if (skip != null)
            animateLayer(skip, animationOffsetY, delay);
    }

    protected void animateLayer(@NonNull View view, float offset, int delay) {
        view.setTranslationY(offset);
        float targetAlpha = view.getAlpha();
        view.setAlpha(0);

        view.animate()
                .setStartDelay(delay)
                .setDuration(VIEW_REVEAL_ANIMATION_DURATION)
                .translationY(0)
                .alpha(targetAlpha);

    }

    public void setMessage(String message) {
        this.message = message;
        shadowTextView.setText(message);
        layoutMessage();
    }

    protected void layoutMessage() {
        int w = shadowTextView.getWidth();
        if (w == 0)
            return;

        for (TextView textLine : textLines) {
            header.removeView(textLine);
        }

        textLines.clear();

        if (message == null)
            return;

        char[] chars = message.toCharArray();
        int lastWord = 0;
        int lastLine = 0;
        TextPaint paint = shadowTextView.getPaint();
        LayoutInflater inflater = LayoutInflater.from(shadowTextView.getContext());
        int anchor = getAnchorViewId();


        for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
            char c = chars[i];
            if (!Character.isWhitespace(c))
                continue;

            if (paint.measureText(message, lastLine, i) > w) {
                if (lastWord <= lastLine)
                    lastWord = i + 1;

                TextView tv = inflateTextView(inflater, anchor, chars, lastLine, lastWord);
                anchor = tv.getId();

                lastLine = lastWord;
            } else if (c == '\n') {
                TextView tv = inflateTextView(inflater, anchor, chars, lastLine, i + 1);
                anchor = tv.getId();
                lastWord = i + 1;
                lastLine = lastWord;
            } else {
                lastWord = i + 1;
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

    protected int getAnchorViewId() {
        return R.id.icon;
    }

    protected int getQuestionTextItemLayoutId() {
        return R.layout.item_question_text;
    }

    @NonNull
    protected TextView inflateTextView(LayoutInflater inflater, int anchor, char[] chars,
                                       int start, int end) {
        TextView tv = (TextView) inflater.inflate(getQuestionTextItemLayoutId(), header, false);
        tv.setId(View.generateViewId());
        tv.setText(chars, start, end - start);

        header.addView(tv);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
        lp.topMargin = textLines.isEmpty() ? (int) dpToPx(root.getContext(), 4) : -(int) dpToPx(root.getContext(), 2);
        tv.setLayoutParams(lp);

        textLines.add(tv);
        return tv;
    }
}
