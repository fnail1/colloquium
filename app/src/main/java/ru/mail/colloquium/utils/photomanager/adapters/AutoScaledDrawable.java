package ru.mail.colloquium.utils.photomanager.adapters;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import ru.mail.colloquium.utils.GraphicUtils;


public class AutoScaledDrawable extends MyDrawableWrapper {

    private final int width;
    private final int height;
    protected final Rect rect;

    public AutoScaledDrawable(@NonNull Drawable src, int width, int height) {
        super(src);
        this.width = width;
        this.height = height;
        rect = GraphicUtils.project(
                src.getIntrinsicWidth(), src.getIntrinsicHeight(),
                width, height);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        src.setBounds(rect);
        src.draw(canvas);
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }
}
