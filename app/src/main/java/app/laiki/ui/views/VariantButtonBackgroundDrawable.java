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

import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;

public class VariantButtonBackgroundDrawable extends Drawable {
    private ButtonState buttonState = ButtonState.DEFAULT;
    private ButtonState previouseState = ButtonState.DEFAULT;
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
        trace("%d %s -> %s, %.3f", hashCode(), previouseState, buttonState, alpha);

        Rect bounds = getBounds();
        if (alpha == 1 || previouseState == buttonState) {
            switch (buttonState) {
                case DEFAULT:
                    layer1Paint.setAlpha(255);
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
                    break;
                case DISABLED:
                    layer1Paint.setAlpha(127);
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
                    break;
                case SELECTED:
                    layer2Paint.setAlpha(255);
                    canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer2Paint);
                    break;
            }
        } else if ((previouseState == ButtonState.DISABLED && buttonState == ButtonState.DEFAULT) ||
                (previouseState == ButtonState.DEFAULT && buttonState == ButtonState.DISABLED)) {
            layer1Paint.setAlpha((int) (255 * (0.5f + alpha / 2)));
            canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
        } else if (previouseState == ButtonState.DEFAULT && buttonState == ButtonState.SELECTED) {
            layer1Paint.setAlpha(255);
            canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
            canvas.save();
            layer2Paint.setAlpha(255);
            canvas.clipRect(bounds.left, bounds.top, bounds.left + bounds.width() * alpha, bounds.bottom);
            canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer2Paint);
            canvas.restore();
        } else if (previouseState == ButtonState.SELECTED && buttonState == ButtonState.DEFAULT) {
            layer1Paint.setAlpha(255);
            canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer1Paint);
            layer2Paint.setAlpha((int) (255 * (1 - alpha)));
            canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, layer2Paint);
        } else {
            safeThrow(new Exception("unsupportetd transitions: " + previouseState + " -> " + buttonState), true);
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
        if (buttonState != this.buttonState) {
            previouseState = this.buttonState;
        }
        this.buttonState = buttonState;


        this.alpha = alpha;

        if (alpha != 1 && previouseState != buttonState &&
                previouseState != ButtonState.DEFAULT &&
                buttonState != ButtonState.DEFAULT) {
            safeThrow(new Exception("unsupportetd transitions: " + previouseState + " -> " + buttonState), true);
        }

        invalidateSelf();
    }

    public ButtonState getButtonState() {
        return buttonState;
    }

    public enum ButtonState {
        DEFAULT, DISABLED, SELECTED
    }
}
