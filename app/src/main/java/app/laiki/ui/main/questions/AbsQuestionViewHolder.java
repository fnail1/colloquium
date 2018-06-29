package app.laiki.ui.main.questions;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.laiki.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AbsQuestionViewHolder {
    public static final int[] COLORS = {
            0xFF1A1334, 0xFF26294A, 0xFF02545A, 0xFF0C7351,
            0xFFAAD962, 0xFFFBBF45, 0xFFEF6A32, 0xFFED1C45,
            0xFFA12A5E, 0xFFA12A5E, 0xFF2767A9, 0xFF14407F,
            0xFF14407F};

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.variant1Text1) TextView variant1Text1;
    @BindView(R.id.variant1Text2) TextView variant1Text2;
    @BindView(R.id.variant1) LinearLayout variant1;
    @BindView(R.id.variant2Text1) TextView variant2Text1;
    @BindView(R.id.variant2Text2) TextView variant2Text2;
    @BindView(R.id.variant2) LinearLayout variant2;
    @BindView(R.id.variant3Text1) TextView variant3Text1;
    @BindView(R.id.variant3Text2) TextView variant3Text2;
    @BindView(R.id.variant3) LinearLayout variant3;
    @BindView(R.id.variant4Text1) TextView variant4Text1;
    @BindView(R.id.variant4Text2) TextView variant4Text2;
    @BindView(R.id.variant4) LinearLayout variant4;
    @BindView(R.id.answers) LinearLayout answers;
    @BindView(R.id.skip) TextView skip;
    @Nullable
    @BindView(R.id.next) TextView next;
    @Nullable
    @BindView(R.id.progress) ProgressBar progress;
    View root;

    public AbsQuestionViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.fr_question, parent, false));
    }

    public AbsQuestionViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
    }

}
