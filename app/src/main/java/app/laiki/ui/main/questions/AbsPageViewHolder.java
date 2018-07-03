package app.laiki.ui.main.questions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.ref.WeakReference;

import app.laiki.R;
import app.laiki.utils.GraphicUtils;
import butterknife.ButterKnife;

import static app.laiki.App.prefs;

public class AbsPageViewHolder {
    private static final int[] BACKGROUND_RES_IDS = {
            R.drawable.bg_q01,
            R.drawable.bg_q02,
            R.drawable.bg_q03,
            R.drawable.bg_q04,
            R.drawable.bg_q05,
            R.drawable.bg_q06,
            R.drawable.bg_q07,
            R.drawable.bg_q08,
            R.drawable.bg_q09,
            R.drawable.bg_q10,
            R.drawable.bg_q11,
    };
    private static final WeakReference[] BACKGROUNDS = new WeakReference[BACKGROUND_RES_IDS.length];

    public static Drawable randomBackground(Context context) {
        return randomBackground(context, prefs().uniqueId());
    }

    static Drawable randomBackground(Context context, int key) {
        int index = (key & 0xffff) % BACKGROUNDS.length;
        WeakReference ref = BACKGROUNDS[index];
        Drawable d;
        if (ref != null) {
            d = (Drawable) ref.get();
            if (d != null)
                return d;
        }
        d = GraphicUtils.getDrawable(context, BACKGROUND_RES_IDS[index]);
        BACKGROUNDS[index] = new WeakReference<>(d);

        return d;
    }

    View root;

    public AbsPageViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

    public void animateReveal() {

    }
}
