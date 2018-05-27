package ru.mail.colloquium.diagnostics.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import static ru.mail.colloquium.diagnostics.Logger.trace;

@SuppressLint("AppCompatCustomView")
public class DebugImageView extends AppCompatImageView {

    private String key;

    public DebugImageView(Context context) {
        super(context);
    }

    public DebugImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageResource(int resId) {
        trace("%s %s %s %s", hashCode(), key, getTag(), resId);
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        trace("%s %s %s %s", hashCode(), key, getTag(), drawable);
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        trace("%s %s %s %s", hashCode(), key, getTag(), bm);

        super.setImageBitmap(bm);
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        trace("%s %s %s %s", hashCode(), key, getTag(), uri);
        super.setImageURI(uri);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        trace("%s %s %s ", hashCode(), key, getTag());
        super.onDraw(canvas);
    }

    public void setKey(String key) {
        this.key = key;
    }
}
