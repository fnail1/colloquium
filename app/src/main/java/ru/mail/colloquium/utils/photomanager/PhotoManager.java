package ru.mail.colloquium.utils.photomanager;

import android.content.Context;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import ru.mail.colloquium.BuildConfig;
import ru.mail.colloquium.service.AppStateObserver;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.io.FileOpException;


import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class PhotoManager {
    private static final String CACHE_DIR_NAME = "photos_cache";
    static final int CROSS_FADE_REVEAL_DURATION = 500;
    final PhotoMemoryCache cache;

    final File cacheDir;

    public PhotoManager(Context context, AppStateObserver stateObserver) {
        cacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs())
            safeThrow(new FileOpException(FileOpException.FileOp.MKDIR, cacheDir));
        cache = new PhotoMemoryCache(stateObserver);
    }

    public PhotoRequestBuilder<ImageView> attach(ImageView imageView, String photo) {
        return attach(new PhotoRequest.ImageViewTarget(imageView), photo);
    }

    public <TView extends View> PhotoRequestBuilder<TView> attach(PhotoRequest.Target<TView> imageView, String photo) {
        PhotoRequestBuilder<TView> builder = new PhotoRequestBuilder<>(this, imageView, photo);

        if (BuildConfig.DEBUG) {
            IllegalStateException th = new IllegalStateException("commit() not called!");
            ThreadPool.UI.post(() -> {
                if (!builder.committed)
                    throw th;
            });
        }


        return builder;
    }


    @UiThread
    void attach(PhotoRequest photoRequest) {
        if (photoRequest.bind())
            photoRequest.start();
    }

    @UiThread
    public void clean(ImageView imageView) {
        imageView.setTag(null);
    }

}
