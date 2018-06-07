package ru.mail.colloquium.utils.photomanager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;

import java.io.IOException;

import ru.mail.colloquium.toolkit.collections.Func;
import ru.mail.colloquium.toolkit.http.ClientException;
import ru.mail.colloquium.ui.ScreenMetrics;
import ru.mail.colloquium.utils.photomanager.adapters.CircleDrawable;
import ru.mail.colloquium.utils.photomanager.adapters.RoundedRectDrawable;

import static ru.mail.colloquium.App.screenMetrics;


@SuppressWarnings({"unused", "WeakerAccess"})
public class PhotoRequestBuilder<TView> {
    private final PhotoManager photoManager;
    private PhotoRequest.Target<TView> imageView;
    private final String photo;
    private int width;
    private int height;
    private boolean circle;
    private Func<Drawable, Drawable> extraEffect;
    private PhotoRequest.AbstractPlaceholder<TView> placeholder;
    boolean committed;
    private long doNotAnimateBefore;

    public PhotoRequestBuilder(PhotoManager photoManager, PhotoRequest.Target<TView> imageView, String photo) {
        this.photoManager = photoManager;
        this.imageView = imageView;
        this.photo = photo;
    }

    public PhotoRequestBuilder<TView> size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PhotoRequestBuilder<TView> size(ScreenMetrics.Size size) {
        this.width = size.width;
        this.height = size.height;
        return this;
    }

    public PhotoRequestBuilder<TView> circle() {
        extraEffect = CircleDrawable::new;
        return this;
    }

    public PhotoRequestBuilder<TView> round(float rx, float ry) {
        extraEffect = (d) -> new RoundedRectDrawable.RoundedRectDrawable1(d, rx, ry);
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    public PhotoRequestBuilder<TView> round(float leftTopX, float leftTopY, float rightTopX, float rightTopY, float rightBottomX, float rightBottomY, float leftBottomX, float leftBottomY) {
        extraEffect = (d) -> new RoundedRectDrawable.RoundedRectDrawable2(d, leftTopX, leftTopY, rightTopX, rightTopY, rightBottomX, rightBottomY, leftBottomX, leftBottomY);
        return this;
    }

    public PhotoRequestBuilder<TView> placeholder(@DrawableRes int resId) {
        placeholder = PhotoRequest.AbstractPlaceholder.wrap(resId);
        return this;
    }

    public PhotoRequestBuilder<TView> placeholder(Drawable drawable) {
        placeholder = PhotoRequest.AbstractPlaceholder.wrap(drawable);
        return this;
    }

    public PhotoRequestBuilder<TView> placeholder(PhotoRequest.AbstractPlaceholder<TView> placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    private void resolveSize() {
        if (width != 0 && height != 0)
            return;

        ScreenMetrics.Size screen = screenMetrics().screen;
        width = screen.width;
        height = screen.height;
    }

    public void fixTransitionConflict(int transitionDuration) {
        doNotAnimateBefore = SystemClock.elapsedRealtime() + transitionDuration;
    }

    public void commit() {
        committed = true;
        if (photo == null)
            return;

        resolveSize();
        PhotoRequest<TView> request = new PhotoRequest<>(photoManager, imageView, photo, width, height, extraEffect, placeholder);
        request.doNotAnimateBefore = doNotAnimateBefore;
        photoManager.attach(request);
    }

    public Bitmap loadBitmapSync() throws IOException, ClientException {
        PhotoRequest request = new PhotoRequest<Void>(photoManager, null, photo, width, height, extraEffect, null);

        if (!request.load()) {
            if (request.download())
                request.load();
        }


        return request.bitmap;
    }
}
