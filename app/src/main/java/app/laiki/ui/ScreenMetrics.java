package app.laiki.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;


import app.laiki.R;

import static app.laiki.App.app;

public class ScreenMetrics {

    public final Size screen;
    public final Size avatarSize;
    public final Size notificationIcon;
    public final Size icon;
    public final int indent;

    public ScreenMetrics(Context context) {
        Resources resources = context.getResources();

        indent = resources.getDimensionPixelOffset(R.dimen.indent);

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        screen = new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);


        int avatarSizePixels = resources.getDimensionPixelSize(R.dimen.avatar_size);
        avatarSize = new Size(avatarSizePixels, avatarSizePixels);


        int notificationIconSize = resources.getDimensionPixelSize(R.dimen.notification_image_size);
        notificationIcon = new Size(notificationIconSize, notificationIconSize);

        int iconSize = resources.getDimensionPixelSize(R.dimen.icon_size);
        icon = new Size(iconSize, iconSize);
    }

    public static boolean isPortrait() {
        return app().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


    public static final class Size {
        public final int width;
        public final int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
