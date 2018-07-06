package app.laiki.ui.main.questions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.types.Choice;
import app.laiki.service.ServiceState;
import app.laiki.utils.Utils;
import butterknife.OnClick;

import static app.laiki.App.prefs;
import static app.laiki.App.statistics;
import static app.laiki.utils.Utils.dpToPx;

public class RateUsViewHolder extends AbsQuestionViewHolder {

    private final Callback callback;

    public RateUsViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater, parent);
        this.callback = callback;
    }

    public void bind() {
        setMessage("Как тебе наша приложуха ЧСН?");
        this.icon.setImageResource(R.drawable.ic_rate_us);
        variant1Text.setText("Огонь \uD83D\uDD25");
        variant2Text.setText("Норм");
        variant3Text.setText("ХЗ");
        variant4Text.setText("Чет не оч");
        root.setBackground(randomBackground(root.getContext()));
        progress.setVisibility(View.GONE);
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                statistics().rateUs().answer(Choice.A);
                onPositive();
                break;
            case R.id.variant2:
                statistics().rateUs().answer(Choice.B);
                onPositive();
                break;
            case R.id.variant3:
                statistics().rateUs().answer(Choice.C);
                onNegative();
                break;
            case R.id.variant4:
                statistics().rateUs().answer(Choice.D);
                onNegative();
                break;
            case R.id.skip:
                statistics().rateUs().answer(Choice.E);
                onNegative();

                //no break;
            case R.id.next:
            case R.id.root:
            case R.id.page1:
            case R.id.page2:
                break;
        }
    }

    private void onPositive() {
        Context context = root.getContext();
        if (context == null)
            return;

        new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setMessage("А поставишь нам пятерочку? Ну плэзз...")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Utils.startGooglePlay(context);
                    statistics().rateUs().googlePlayStarted();
                    onComplete();
                }).setNegativeButton("Нет", (dialog, which) -> onComplete())
                .show();
    }

    private void onNegative() {
        onComplete();
    }

    private void onComplete() {
        ServiceState serviceState = prefs().serviceState();
        serviceState.rateUsComplete = true;
        serviceState.rateUsRequired = false;
        prefs().save(serviceState);

        callback.onNextClick();
    }

    @Override
    protected void layoutMessage() {
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
        int anchor = R.id.icon;


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

    @NonNull
    protected TextView inflateTextView(LayoutInflater inflater, int anchor, char[] chars, int start, int end) {
        ConstraintLayout layout = (ConstraintLayout) this.root;

        ConstraintSet cset = new ConstraintSet();

        TextView tv = (TextView) inflater.inflate(R.layout.item_question_text, (ViewGroup) root, false);
        tv.setId(View.generateViewId());
        tv.setText(chars, start, end - start);

        layout.addView(tv);

        if (textLines.isEmpty()) {
            int margin = (int) dpToPx(root.getContext(), 7);
            cset.clone(layout);
            cset.connect(tv.getId(), ConstraintSet.TOP, anchor, ConstraintSet.BOTTOM, margin);
        } else {
            View space = new View(inflater.getContext());
            space.setId(View.generateViewId());
            layout.addView(space);
            cset.clone(layout);
            int margin = (int) dpToPx(root.getContext(), 2);
//            cset.constrainWidth(space.getId(), 1);
            cset.constrainHeight(space.getId(), margin);
            cset.connect(space.getId(), ConstraintSet.BOTTOM, anchor, ConstraintSet.BOTTOM, margin);
            cset.connect(tv.getId(), ConstraintSet.TOP, space.getId(), ConstraintSet.TOP);
        }

        cset.connect(tv.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cset.connect(tv.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cset.applyTo(layout);

        textLines.add(tv);
        return tv;
    }

    public interface Callback {
        void onNextClick();
    }
}
