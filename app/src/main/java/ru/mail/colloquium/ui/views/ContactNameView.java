package ru.mail.colloquium.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import ru.mail.colloquium.model.entities.Contact;

public class ContactNameView extends View {
    private static final int FONT_SIZE_MIN = 30;
    private static final int FONT_SIZE_TWO_LINE = 60;
    private static final int FONT_SIZE_MAX = 80;

    private final TextPaint paint1 = new TextPaint();
    private final TextPaint paint2 = new TextPaint();
    private int maxFontSize;
    private Contact contact;
    private String line1;
    private String line2;
    private float line1X;
    private float line2X;
    private float line1Shift;
    private float line2Shoft;

    public ContactNameView(Context context) {
        super(context);
        initPaints();
    }

    public ContactNameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public ContactNameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ContactNameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaints();
    }

    private void initPaints() {
        Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        paint1.setTypeface(typeface);
        paint2.setTypeface(typeface);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (line1 == null) {
            prepareToDraw();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (line1 != null) {
            if (line2 != null) {

                float h1 = paint1.getTextSize() + paint1.baselineShift;
                float h2 = h1 + paint2.getTextSize() + paint2.baselineShift;
                int height = getHeight();
                float y = (height - h2) / 2;
                canvas.drawText(line1, line1X, y + h1 - line1Shift, paint1);
                canvas.drawText(line2, line2X, y + h2 - line2Shoft, paint2);

//                canvas.drawLine(line1X, y + h1, line1X, y, paint1);
//                canvas.drawLine(line2X + 20, y + h2, line2X, y + h1, paint2);
            } else {
                float h1 = paint1.getTextSize() + paint1.getFontMetrics().bottom;
                int height = getHeight();
                float y = (height - h1) / 2;
                canvas.drawText(line1, line1X, y + h1 - line1Shift, paint1);
//                canvas.drawLine(line1X, y + h1, line1X, y, paint1);
            }
        }
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        line1 = null;
        maxFontSize = 0;
        prepareToDraw();
        invalidate();
    }


    private void prepareToDraw() {
        int maxWidth = getWidth();
        if (maxWidth == 0)
            return;

        MeasureResult m = new MeasureResult();
        selectMaxTextHeight(paint1, 1, getHeight(), FONT_SIZE_MIN, FONT_SIZE_MAX, m);
        maxFontSize = m.fontSize;


        if (!TextUtils.isEmpty(contact.firstName) && !TextUtils.isEmpty(contact.lastName)) {
            String fullName = contact.firstName + " " + contact.lastName;
            selectMaxTextWidth(paint1, fullName, maxWidth, FONT_SIZE_TWO_LINE - 2, maxFontSize, m);
            if (m.fontSize >= FONT_SIZE_TWO_LINE) {
                line1 = fullName;
                line1X = (maxWidth - m.textSize) / 2;
                line2 = null;
                return;
            }


            selectMaxTextHeight(paint1, 2, getHeight(), FONT_SIZE_MIN, FONT_SIZE_MAX, m);
            maxFontSize = m.fontSize;

            selectMaxTextWidth(paint1, contact.firstName, maxWidth, FONT_SIZE_MIN, maxFontSize, m);
            line1X = (maxWidth - m.textSize) / 2;
            line1 = contact.firstName;

            selectMaxTextWidth(paint2, contact.lastName, maxWidth, FONT_SIZE_MIN, maxFontSize, m);
            line2X = (maxWidth - m.textSize) / 2;
            line2 = contact.lastName;
        } else if (TextUtils.isEmpty(contact.firstName)) {
            if (TextUtils.isEmpty(contact.lastName)) {
                line1 = contact.displayName;
            } else {
                line1 = contact.lastName;
            }
            line2 = null;
            selectMaxTextWidth(paint1, line1, maxWidth, FONT_SIZE_MIN, maxFontSize, m);
            line1X = (maxWidth - m.textSize) / 2;
        } else {
            line1 = contact.firstName;
            line2 = null;
            selectMaxTextWidth(paint1, line1, maxWidth, FONT_SIZE_MIN, maxFontSize, m);
            line1X = (maxWidth - m.textSize) / 2;
        }

        line1Shift = paint1.getFontMetrics().bottom;
        line2Shoft = paint2.getFontMetrics().bottom;
    }

    private void selectMaxTextHeight(TextPaint paint, int lines, int maxHeight, int minFontSize, int maxFontSize, MeasureResult out) {
        out.fontSize = (maxFontSize + minFontSize) / 2;
        if (maxFontSize - minFontSize < 2)
            return;

        paint.setTextSize(out.fontSize);
        out.textSize = lines * (paint.getTextSize() + paint.baselineShift);
        if (out.textSize > maxHeight)
            selectMaxTextHeight(paint, lines, maxHeight, minFontSize, out.fontSize, out);
        if (out.textSize < maxHeight)
            selectMaxTextHeight(paint, lines, maxHeight, out.fontSize, maxFontSize, out);

    }

    private void selectMaxTextWidth(TextPaint paint, String text, int maxWidth, int minFontSize, int maxFontSize, MeasureResult out) {
        out.fontSize = (maxFontSize + minFontSize) / 2;
        if (maxFontSize - minFontSize < 2)
            return;

        paint.setTextSize(out.fontSize);
        out.textSize = paint.measureText(text);
        if (out.textSize > maxWidth)
            selectMaxTextWidth(paint, text, maxWidth, minFontSize, out.fontSize, out);
        if (out.textSize < maxWidth)
            selectMaxTextWidth(paint, text, maxWidth, out.fontSize, maxFontSize, out);

    }

    private static class MeasureResult {
        int fontSize;
        public float textSize;
    }
}
