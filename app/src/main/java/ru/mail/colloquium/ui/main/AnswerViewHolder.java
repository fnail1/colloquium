package ru.mail.colloquium.ui.main;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Answer;

import static ru.mail.colloquium.App.appState;

public class AnswerViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.root) RelativeLayout root;
    private Answer answer;

    public AnswerViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_answer, parent, false));
    }

    public AnswerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Answer answer) {
        this.answer = answer;
        switch (answer.gender) {
            case CAMEL:
                break;
            case MALE:
                icon.setImageResource(R.drawable.ic_favorite_blue);
                root.setBackgroundColor(0xffa0a0f0);
                break;
            case FEMALE:
                icon.setImageResource(R.drawable.ic_favorite_pink);
                root.setBackgroundColor(0xfff0a0a0);
                break;
        }


        title.setText(formatTerm(title.getResources(), answer.created));
    }

    private String formatTerm(Resources resources, long timestamp) {
        long t = appState().getServerTime() - timestamp;
        if (t < 60 * 1000)
            return resources.getString(R.string.just_now);
        if (t < 24 * 60 * 60 * 1000) {
            t /= 60 * 60 * 1000;
            return resources.getQuantityString(R.plurals.hours, (int) t, t);
        }
        t /= 24 * 60 * 60 * 1000;
        return resources.getQuantityString(R.plurals.days, (int) t, t);
    }
}
