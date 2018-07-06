package app.laiki.diagnostics.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import static app.laiki.diagnostics.Logger.trace;

public class DebugTextView extends AppCompatTextView {
    public DebugTextView(Context context) {
        super(context);
    }

    public DebugTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        trace("%.3f, // %s", alpha, getText());
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        trace("%.3f, // %s", translationY, getText());
    }
}
