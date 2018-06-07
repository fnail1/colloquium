package ru.mail.colloquium.utils.photomanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLHandshakeException;

import ru.mail.colloquium.BuildConfig;
import ru.mail.colloquium.diagnostics.Logger;
import ru.mail.colloquium.toolkit.collections.Func;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.toolkit.http.ClientException;
import ru.mail.colloquium.toolkit.http.HttpConnection;
import ru.mail.colloquium.toolkit.http.LoginRequiredException;
import ru.mail.colloquium.toolkit.http.ServerException;
import ru.mail.colloquium.toolkit.io.FileOpException;
import ru.mail.colloquium.toolkit.io.FileUtils;
import ru.mail.colloquium.utils.GraphicUtils;
import ru.mail.colloquium.utils.photomanager.adapters.AutoScaledDrawable;
import ru.mail.colloquium.utils.photomanager.adapters.DebugDrawable;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.networkObserver;
import static ru.mail.colloquium.App.prefs;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;
import static ru.mail.colloquium.diagnostics.Logger.logV;


public final class PhotoRequest<TView> implements Runnable {
    private final static AtomicInteger counter = new AtomicInteger();
    private final static HashSet<String> history = new HashSet<>();

    public final Target<TView> viewHolder;
    private final Func<Drawable, Drawable> extraEffect;
    private final AbstractPlaceholder<TView> placeholder;
    private final int targetWidth;
    private final int targetHeight;
    private final File file;
    private final String cacheKey;
    private final boolean isContentUri;
    private String photo;
    private final PhotoManager photoManager;
    Bitmap bitmap;
    private final long startTs;
    private final int name;
    private boolean loadPerformed;
    private boolean downloadPerformed;
    private boolean placeholderAttached;
    public long doNotAnimateBefore;

    public PhotoRequest(PhotoManager pm,
                        @Nullable Target<TView> viewHolder,
                        @NonNull String photo,
                        int targetWidth,
                        int targetHeight,
                        @Nullable Func<Drawable, Drawable> extraEffect,
                        @Nullable AbstractPlaceholder<TView> placeholder) {
        String fileName = null;
        isContentUri = photo.startsWith("content://");
        if (isContentUri) {
            fileName = photo;
//            file = photo;
        } else {
            try {
                fileName = URLEncoder.encode(photo, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                safeThrow(e);
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = "noimage";
        }
        cacheKey = fileName;
        file = new File(pm.cacheDir, fileName);
        startTs = SystemClock.elapsedRealtime();
        name = counter.incrementAndGet();

        photoManager = pm;
        this.photo = photo;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.extraEffect = extraEffect;
        this.placeholder = placeholder;
        this.viewHolder = viewHolder;
    }


    @UiThread
    public void start() {
        log("start");

        if (photo == null) {
            apply();
            return;
        }

//        if (photo.cachedWidth > 0)
        bitmap = photoManager.cache.get(cacheKey);
        apply();

        boolean b1 = bitmap == null;
        boolean b2 = shouldDownload();
        if (b1 || b2) {
//            log("scheduled for " + (b1 ? "load, " : "") + (b2 ? "download " : ""));
            ThreadPool.EXECUTORS.getExecutor(ThreadPool.Priority.LOWEST).execute(this);
        }
    }

    public void startSync() {
        log("startSync");

        if (photo == null) {
            apply();
            return;
        }

        bitmap = photoManager.cache.get(cacheKey);
        apply();

        if (shouldDownload()) {
            try {
                download();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                safeThrow(e);
            }
        }

        if (bitmap == null)
            load();

        apply();
    }

    @Override
    public void run() {
        if (!checkContext()) {
            log("cancel");
            return;
        }

        if (ThreadPool.isUiThread()) {
            log("UI");
            apply();
            return;
        }

        bitmap = photoManager.cache.get(cacheKey);
        if (bitmap != null) {
            ThreadPool.UI.post(this);
            return;
        }

        boolean loadSuccess = false;

        if (!loadPerformed) {
            log("load existing");
            loadPerformed = true;
            if ((loadSuccess = load()) || !placeholderAttached) {
                long dt = doNotAnimateBefore > 0 ? doNotAnimateBefore - SystemClock.elapsedRealtime() : 0;
                if (dt > 0)
                    ThreadPool.UI.postDelayed(this, dt);
                else
                    ThreadPool.UI.post(this);
            }
        }

        if (!downloadPerformed && (!loadSuccess || shouldDownload())) {
            try {
                log("start download");

                if (download())
                    load();

                if (Logger.LOG_GLIDE) {
                    synchronized (history) {
                        if (!history.add(photo)) {
                            log("download twice!!!!!!!!!");
                        }
                    }
                    log("complete download");
                }
                loadPerformed = true;
                downloadPerformed = true;
                long dt = doNotAnimateBefore > 0 ? doNotAnimateBefore - SystemClock.elapsedRealtime() : 0;
                if (dt > 0)
                    ThreadPool.UI.postDelayed(this, dt);
                else
                    ThreadPool.UI.post(this);

            } catch (UnknownHostException | ConnectException | SSLHandshakeException e) {
                e.printStackTrace();
                networkObserver().onNetworkError();
                networkObserver().scheduleTask(String.valueOf(this.hashCode()), this);
            } catch (LoginRequiredException e) {
                if (BuildConfig.DEBUG)
                    e.printStackTrace();
                else
                    safeThrow(e);
            } catch (IOException e) {
                e.printStackTrace();
                if (!appService().pingApi())
                    networkObserver().onNetworkError();
                networkObserver().scheduleTask(String.valueOf(this.hashCode()), this);
            } catch (Exception e) {
                safeThrow(e, true);
            }
        }

        if (!loadPerformed && !downloadPerformed && !placeholderAttached)
            ThreadPool.UI.post(this);
    }

    private boolean isNewBitmapBetter(Bitmap old) {
        if (bitmap == null)
            return false;

        if (old == null)
            return true;

        return (old.getWidth() < targetWidth && old.getWidth() < bitmap.getWidth()) ||
                (old.getHeight() < targetHeight && old.getHeight() < bitmap.getHeight());
    }

    private boolean shouldDownload() {
        return !file.exists();
    }

    private boolean checkContext() {
        TView imageView = viewHolder.viewRef.get();
        return imageView != null && cacheKey.equals(viewHolder.getTag(imageView));
    }

    boolean bind() {
        TView imageView = viewHolder.viewRef.get();
        if (imageView == null || cacheKey.equals(viewHolder.getTag(imageView)))
            return false;

        viewHolder.setTag(imageView, cacheKey);
        return true;
    }

    public boolean load() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
//            _history.add("load {" + (SystemClock.elapsedRealtime() - _t0) + "}; ");
            if (isContentUri)
                bitmap = MediaStore.Images.Media.getBitmap(app().getContentResolver(), Uri.parse(photo));
            else
                bitmap = GraphicUtils.decodeUri(file.getPath(), targetWidth, targetHeight);
            if (bitmap != null) {
                photoManager.cache.update(cacheKey, bitmap);
//                trace("2 complete %s %d %d", requestId, bitmap.getWidth(), bitmap.getHeight());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        trace("2 fail %s", requestId);
        return false;
    }


    public boolean download() throws IOException, ClientException {

//        if (BuildConfig.DEBUG) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        String url = photo;

//        trace("4 downloading %s", requestId);
//        _history.add("download {t:" + (SystemClock.elapsedRealtime() - _t0) + ", q:" + photoManager.downloadQueue.inQueue.size() + "}; ");

        try {
            try {
                HttpConnection.downloadImage(url, file, true);
            } catch (FileNotFoundException e) {
                File dir = file.getParentFile();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (dir.mkdirs() || dir.exists()) {
                    HttpConnection.downloadImage(url, file, true);
                } else {
                    if (FileUtils.calcDiskUsageRate(dir) > .2)
                        safeThrow(new FileOpException(FileOpException.FileOp.MKDIR, dir, e));
                    return false;
                }
            }
        } catch (FileOpException | IllegalStateException e) {
            if (FileUtils.calcDiskUsageRate(file.getParentFile()) > .2)
                safeThrow(e);
            return false;
        } catch (ServerException e) {
            safeThrow(e);

            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options);

        return true;
    }

    @UiThread
    private void apply() {
        boolean animate = downloadPerformed || SystemClock.elapsedRealtime() - startTs > 500;
        boolean usePlaceholder = viewHolder.forcePlaceholder() || animate;

        TView imageView = viewHolder.viewRef.get();
        if (imageView == null)
            return;

        if (bitmap == null) {
            if (usePlaceholder && placeholder != null) {
                log("3 set");
                placeholderAttached = true;
                placeholder.apply(this);
            } else {
                log("4 skip");
                viewHolder.apply(this, imageView, null, false);
            }
            return;
        }


        Drawable d = new BitmapDrawable(viewHolder.getContext(imageView).getResources(), bitmap);

        d = viewHolder.staticEffect(this, imageView, d);

        if (extraEffect != null) {
            d = extraEffect.invoke(d);
        }

        if (Logger.LOG_GLIDE) {
            d = new DebugDrawable(photo, d);
        }

//        log(animate ? "apply animation" : "apply static");

        viewHolder.apply(this, imageView, d, animate);

//        _history.add("apply {t:" + (SystemClock.elapsedRealtime() - _t0) + ", q:" + photoManager.downloadQueue.inQueue.size() + "}; ");
//
//        StringBuilder sb = new StringBuilder(_history.size() * 20);
//        for (String s : _history) {
//            sb.append(s);
//        }
//
//        trace("%s, %d, %s", requestId, SystemClock.elapsedRealtime() - _t0, sb);
    }

    private void log(String message) {
        logV(Logger.LOG_GLIDE, Logger.TAG_GLIDE, "%d %s %s %s -> (%dx%d)",
                name, message, cacheKey,
                photo,
                targetWidth, targetHeight);
    }

    public abstract static class AbstractPlaceholder<TView> {

        static <TView> AbstractPlaceholder<TView> wrap(@DrawableRes int resId) {
            return new ResourcePlaceholder<TView>(resId);
        }

        static <TView> AbstractPlaceholder<TView> wrap(Drawable drawable) {
            return new DrawablePlaceholder<TView>(drawable);
        }

        protected AbstractPlaceholder() {
        }

        protected abstract void apply(PhotoRequest<TView> request);

        protected void apply(PhotoRequest<TView> request, TView imageView, Drawable drawable) {
            if (request.viewHolder == null)
                return;

            Drawable d = request.viewHolder.staticEffect(request, imageView, drawable);
            if (Logger.LOG_GLIDE) {
                d = new DebugDrawable(request.photo, d);
            }

            request.viewHolder.apply(request, imageView, d, false);

        }


    }

    private static class ResourcePlaceholder<TView> extends AbstractPlaceholder<TView> {
        private final int resId;

        ResourcePlaceholder(@DrawableRes int resId) {
            this.resId = resId;
        }

        @Override
        protected void apply(PhotoRequest<TView> request) {
            if (request.viewHolder == null)
                return;

            TView imageView = request.viewHolder.viewRef.get();
            if (imageView == null)
                return;

            Context context = request.viewHolder.getContext(imageView);
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resId, context.getTheme());
            if (drawable != null) {
//                    drawable = new AutoScaledDrawable(drawable, request.targetWidth, request.targetHeight);
                apply(request, imageView, drawable);
            }
        }

    }

    private static class DrawablePlaceholder<TView> extends AbstractPlaceholder<TView> {
        private final Drawable drawable;

        DrawablePlaceholder(Drawable drawable) {
            this.drawable = drawable;
        }

        @Override
        protected void apply(PhotoRequest<TView> request) {
            if (request.viewHolder == null)
                return;

            TView imageView = request.viewHolder.viewRef.get();
            if (imageView == null)
                return;

            apply(request, imageView, drawable);
        }

    }

    public static abstract class Target<TView> {

        public final WeakReference<TView> viewRef;

        protected Target(TView target) {
            this.viewRef = new WeakReference<>(target);
        }

        public Drawable staticEffect(PhotoRequest<TView> request, TView view, Drawable drawable) {
            return drawable;
        }

        public abstract void apply(PhotoRequest<TView> request, TView view, @Nullable Drawable d, boolean animate);

        public abstract boolean forcePlaceholder();

        public abstract Object getTag(TView imageView);

        public abstract Context getContext(TView imageView);

        public abstract void setTag(TView imageView, Object tag);

    }

    public static abstract class ViewTarget<TView extends View> extends Target<TView> {

        ViewTarget(TView target) {
            super(target);
        }

        @Override
        public Object getTag(TView imageView) {
            return imageView.getTag();
        }

        @Override
        public Context getContext(TView imageView) {
            return imageView.getContext();
        }

        @Override
        public void setTag(TView imageView, Object tag) {
            imageView.setTag(tag);
        }

    }

    public static class ImageViewTarget extends ViewTarget<ImageView> {

        public ImageViewTarget(ImageView target) {
            super(target);
        }

        @Override
        public Drawable staticEffect(PhotoRequest<ImageView> request, ImageView view, @Nullable Drawable drawable) {
            if (drawable != null) {
                switch (view.getScaleType()) {
                    case CENTER_CROP:
//                    case FIT_CENTER:
                        return new AutoScaledDrawable(drawable, request.targetWidth, request.targetHeight);
                }
            }
            return drawable;
        }

        @Override
        public void apply(PhotoRequest<ImageView> request, ImageView view, @Nullable Drawable d, boolean animate) {
            if (animate && d != null) {
                request.log("apply animated");
                GraphicUtils.setImageBitmapAnimated(view, d, PhotoManager.CROSS_FADE_REVEAL_DURATION);
            } else {
                request.log("apply static");
                view.setImageDrawable(d);
            }
        }

        @Override
        public boolean forcePlaceholder() {
            return false;
        }

    }

    public static class TextViewStartTarget extends ViewTarget<TextView> {

        public TextViewStartTarget(TextView target) {
            super(target);
        }

        @Override
        public Drawable staticEffect(PhotoRequest<TextView> request, TextView view, Drawable drawable) {
            drawable.setBounds(0, 0, request.targetWidth, request.targetHeight);
            return drawable;
        }

        @Override
        public void apply(PhotoRequest<TextView> request, TextView view, @Nullable Drawable d, boolean animate) {
            view.setCompoundDrawablesRelative(d, null, null, null);
        }

        @Override
        public boolean forcePlaceholder() {
            return true;
        }

    }
}
