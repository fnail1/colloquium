package app.laiki.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;

import app.laiki.model.entities.Contact;

import static app.laiki.diagnostics.Logger.trace;

public class AvatarBuilder {

    private static final int FONT_SIZE_MIN = 48;
    private static final int FONT_SIZE_MAX = 80;

    public static Drawable build(Contact contact) {
        return new ContactAvatarDrawable(contact);
    }

    private static int getBackgroundColor(int value) {
        int random = value & 0x000fffff;
        int r = 170 + (random * 33) % 80;
        int g = 170 + (random * 57) % 80;
        int b = 170 + (random * 79) % 80;

        int w = (random >> 2) % 6;
        switch (w) {
            case 0:
                r -= (random * 29) % 120;
                break;
            case 1:
                g -= (random * 29) % 120;
                break;
            case 2:
                b -= (random * 29) % 120;
                break;
            case 3:
                r -= (random * 29) % 120;
                g -= (random * 29) % 120;
                break;
            case 4:
                g -= (random * 29) % 120;
                b -= (random * 29) % 120;
                break;
            case 5:
                r -= (random * 29) % 120;
                b -= (random * 29) % 120;
                break;
        }
        return Color.argb(0xff, r, g, b);
    }


    private static class ContactAvatarDrawable extends Drawable {

        private int alpha;
        private final Contact contact;
        private final Paint background;
        private final TextPaint foreground;
        String text = "";
        private MeasureResult measures = new MeasureResult();
        private float textHeight;

        private ContactAvatarDrawable(Contact contact) {
            this.contact = contact;
            background = new Paint();
            background.setColor(getBackgroundColor(contact.serverId.hashCode()));

            foreground = new TextPaint();
            foreground.setColor(0xffffffff);

            if (!TextUtils.isEmpty(contact.firstName))
                text += Character.toUpperCase(contact.firstName.charAt(0));
            if (!TextUtils.isEmpty(contact.lastName))
                text += Character.toUpperCase(contact.lastName.charAt(0));
        }


        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), Math.min(bounds.width(), bounds.height()) / 2, background);
            canvas.drawText(text, (bounds.width() - measures.textSize) / 2, (bounds.height() + textHeight) / 2, foreground);
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
            background.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            super.setBounds(left, top, right, bottom);
            selectMaxTextWidth(foreground, text, right - left, FONT_SIZE_MIN, FONT_SIZE_MAX, measures);
            textHeight = foreground.getTextSize() + foreground.baselineShift - foreground.getFontMetrics().bottom;
//            trace("T = %s, F = %d, S = %f, H = %f", text, measures.fontSize, measures.textSize, textHeight);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }

        private void selectMaxTextWidth(TextPaint paint, String text, int maxWidth, int minFontSize, int maxFontSize, MeasureResult out) {
//        trace("" + minFontSize + "-" + maxFontSize);
            if (maxFontSize - minFontSize < 4) {
                if (out.textSize > maxWidth) {
                    out.fontSize -= 4;
                    paint.setTextSize(out.fontSize);
                    out.textSize = paint.measureText(text);
                }
                return;
            }

            out.fontSize = (maxFontSize + minFontSize) / 2;

            paint.setTextSize(out.fontSize);
            out.textSize = paint.measureText(text);
            if (out.textSize > maxWidth)
                selectMaxTextWidth(paint, text, maxWidth, minFontSize, out.fontSize, out);
            else if (out.textSize < maxWidth)
                selectMaxTextWidth(paint, text, maxWidth, out.fontSize, maxFontSize, out);

        }

        private static class MeasureResult {
            int fontSize;
            public float textSize;
        }
    }
}
