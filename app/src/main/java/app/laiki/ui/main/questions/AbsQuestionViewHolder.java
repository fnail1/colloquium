package app.laiki.ui.main.questions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import app.laiki.R;
import app.laiki.utils.GraphicUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

import static app.laiki.App.prefs;

public class AbsQuestionViewHolder {
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

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.variant1) View variant1;
    @BindView(R.id.variant1text) TextView variant1Text;
    @BindView(R.id.variant2) View variant2;
    @BindView(R.id.variant2text) TextView variant2Text;
    @BindView(R.id.variant3) View variant3;
    @BindView(R.id.variant3text) TextView variant3Text;
    @BindView(R.id.variant4) View variant4;
    @BindView(R.id.variant4text) TextView variant4Text;
    @BindView(R.id.skip) TextView skip;
    @Nullable
    @BindView(R.id.next)
    TextView next;
    @Nullable
    @BindView(R.id.progress)
    ProgressBar progress;
    View root;

    public AbsQuestionViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.fr_question, parent, false));
    }

    public AbsQuestionViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
//        variant2Text = (TextView) variant2;
//        variant3Text = (TextView) variant3;
//        variant4Text = (TextView) variant4;
    }

}
