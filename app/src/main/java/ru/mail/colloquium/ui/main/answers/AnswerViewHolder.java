package ru.mail.colloquium.ui.main.answers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.ui.AnswerActivity;
import ru.mail.colloquium.utils.AntiDoubleClickLock;

import static ru.mail.colloquium.App.dateTimeService;

public class AnswerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.time) TextView time;
    private Answer answer;

    public AnswerViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_answer, parent, false));
    }

    public AnswerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bind(Answer answer) {
        this.answer = answer;
        if (!answer.flags.get(Answer.FLAG_VIEWED)) {
            icon.setImageResource(answer.gender.heartIconResId);
        } else {
            icon.setImageResource(R.drawable.ic_camel_heart);
        }

        Resources resources = title.getResources();
        title.setText(resources.getString(answer.gender.iconResId) + ", " + resources.getString(answer.age.nameResId));


        String timeText = formatTerm(answer.createdAt);
        time.setText(timeText);
    }

    private String formatTerm(long timestamp) {
        long t = dateTimeService().getServerTime() - timestamp;
        if (t < 60 * 1000)
            return "Только что";
        if (t < 60 * 60 * 1000) {
            t /= 60 * 1000;
            return t + "м";
        }
        if (t < 24 * 60 * 60 * 1000) {
            t /= 60 * 60 * 1000;
            return t + "ч";
        }

        t /= 24 * 60 * 60 * 1000;
        return t + "д";
    }

    @Override
    public void onClick(View v) {
        Context context = itemView.getContext();
        if (context == null)
            return;

        if (!AntiDoubleClickLock.onClick(context, R.layout.item_answer))
            return;

        Intent intent = new Intent(context, AnswerActivity.class)
                .putExtra(AnswerActivity.EXTRA_ANSWER_ID, answer._id);
        context.startActivity(intent);
    }
}
