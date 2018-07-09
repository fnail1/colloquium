package app.laiki.ui.main.questions;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.ref.WeakReference;

import app.laiki.R;
import app.laiki.utils.GraphicUtils;
import app.laiki.utils.photomanager.adapters.CircleDrawable;
import butterknife.ButterKnife;

import static app.laiki.App.prefs;

public class AbsPageViewHolder {
    private static final ColorScheme[] COLOR_SCHEMES = {
            new ColorScheme(R.drawable.bg_q01, 0xFFFF5F79),
            new ColorScheme(R.drawable.bg_q02, 0xFFFFEE5F),
            new ColorScheme(R.drawable.bg_q03, 0xFFFF5F5F),
            new ColorScheme(R.drawable.bg_q04, 0xFF5FA5FF),
            new ColorScheme(R.drawable.bg_q05, 0xFFFFF75F),
            new ColorScheme(R.drawable.bg_q06, 0xFFA9D861),
            new ColorScheme(R.drawable.bg_q07, 0xFFFF733F),
            new ColorScheme(R.drawable.bg_q08, 0xFF6F3FFF),
            new ColorScheme(R.drawable.bg_q09, 0xFFFFCC2F),
            new ColorScheme(R.drawable.bg_q10, 0xFFFFBD2F),
            new ColorScheme(R.drawable.bg_q11, 0xFFFF6F6F),
    };

    public static ColorScheme randomColorScheme() {
        return randomColorScheme(prefs().uniqueId());
    }

    static ColorScheme randomColorScheme(int key) {
        int index = (key & 0xffff) % COLOR_SCHEMES.length;
        return COLOR_SCHEMES[index];
    }

    View root;

    public AbsPageViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    public void animateReveal() {

    }

    public static class ColorScheme {
        private final int background;
        private final int highlight;
        private WeakReference<Drawable> cacheBackground;
        private WeakReference<Drawable> cacheHighlight;

        public ColorScheme(int background, int highlight) {
            this.background = background;
            this.highlight = highlight;
        }

        public Drawable background(Context context) {
            Drawable drawable;
            if (cacheBackground != null) {
                drawable = cacheBackground.get();
                if (drawable != null)
                    return drawable;
            }
            drawable = GraphicUtils.getDrawable(context, background);
            cacheBackground = new WeakReference<>(drawable);

            return drawable;
        }

        public Drawable highlight(Context context) {
            Drawable drawable;
            if (cacheHighlight != null) {
                drawable = cacheHighlight.get();
                if (drawable != null)
                    return drawable;
            }
            drawable = new CircleDrawable(new ColorDrawable(highlight));
            cacheHighlight = new WeakReference<>(drawable);

            return drawable;
        }
    }
}
