package app.laiki.ui.main.questions;

import android.support.annotation.StringRes;
import android.text.TextPaint;
import android.view.View;
import android.widget.TextView;

public class VariantViewHolder {
    protected final View root;
    protected final TextView text1;
    protected final TextView text2;

    public VariantViewHolder(View root, TextView text1, TextView text2) {
        this.root = root;
        this.text1 = text1;
        this.text2 = text2;
    }

    public void bind(CharSequence line1, CharSequence line2) {
        text1.setText(line1);
        if (line2 == null) {
            text2.setVisibility(View.GONE);
            text1.setSingleLine(false);
            text1.setMaxLines(2);
        } else {
            text2.setVisibility(View.VISIBLE);
            text2.setText(line2);
            text1.setSingleLine(true);
            text1.setMaxLines(1);
        }
    }

    public void bind(@StringRes int line1, CharSequence line2) {
        text1.setText(line1);
        if (line2 == null) {
            text2.setVisibility(View.GONE);
            text1.setSingleLine(false);
            text1.setMaxLines(2);
        } else {
            text2.setVisibility(View.VISIBLE);
            text2.setText(line2);
            text1.setSingleLine(true);
            text1.setMaxLines(1);
        }
    }


    public void bind(@StringRes int line1, @StringRes int line2) {
        text1.setText(line1);
        if (line2 == 0) {
            text2.setVisibility(View.GONE);
            text1.setSingleLine(false);
            text1.setMaxLines(2);
        } else {
            text2.setVisibility(View.VISIBLE);
            text2.setText(line2);
            text1.setSingleLine(true);
            text1.setMaxLines(1);
        }
    }


    protected TextPaint getPaint() {
        return text1.getPaint();
    }

}
