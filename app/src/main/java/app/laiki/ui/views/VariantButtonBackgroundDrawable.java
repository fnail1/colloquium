package app.laiki.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import app.laiki.R;
import app.laiki.utils.Utils;

public class VariantButtonBackgroundDrawable extends Drawable {
    private ButtonState buttonState = ButtonState.DEFAULT;
    private float alpha;
    private final Paint layer1Paint;
    private final Paint layer2Paint;
    private final float radius;

    public VariantButtonBackgroundDrawable(Context context) {
        layer1Paint = new Paint();

        layer2Paint = new Paint();

        Resources resources = context.getResources();
        layer1Paint.setColor(Utils.getColor(context, R.color.buttonColor));
        layer2Paint.setColor(Utils.getColor(context, R.color.buttonColorSelected));
        radius = resources.getDimensionPixelOffset(R.dimen.button_corner_radius);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        switch (buttonState) {
            case DEFAULT:
                layer1Paint.setAlpha((int) (255 * (0.5f + alpha / 2)));
                canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
            case DISABLED:
                layer1Paint.setAlpha((int) (255 * ((0.5f + (1 - alpha) / 2))));
                canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
                break;
            case SELECTED:
                if (alpha < 1) {
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
                    canvas.save();
                    canvas.clipRect(bounds.left, bounds.top, bounds.left + bounds.width() * alpha, bounds.bottom);
                }
                canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer2Paint);
                if (alpha < 1) {
                    canvas.restore();
                }
                break;
        }

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setState(ButtonState buttonState, float alpha) {
        this.buttonState = buttonState;
        this.alpha = alpha;

        invalidateSelf();
    }

    public ButtonState getButtonState() {
        return buttonState;
    }

    public enum ButtonState {
        DEFAULT, DISABLED, SELECTED
    }
}
