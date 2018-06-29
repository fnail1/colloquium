package app.laiki.ui.main.questions;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.ui.ContactsActivity;
import app.laiki.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.dateTimeService;
import static app.laiki.App.prefs;
import static app.laiki.App.statistics;

public class StopScreenViewHolder {
    public final View root;
    @BindView(R.id.timer) TextView timer;
    private final Callback callback;

    public StopScreenViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        this(inflater.inflate(R.layout.fr_stopscreen, parent, false), callback);
    }

    public StopScreenViewHolder(View root, Callback callback) {
        this.root = root;
        this.callback = callback;
        ButterKnife.bind(this, root);
    }

    @OnClick(R.id.contacts)
    public void onViewClicked() {
        Activity activity = Utils.getActivity(root);
        if (activity != null) {
            statistics().questions().contacts();
            activity.startActivity(new Intent(activity, ContactsActivity.class));
        }
    }

    public void bind() {
        if (timer == null)
            return;
        long timeSpan = prefs().config().deadTime - (dateTimeService().getServerTime() - prefs().serviceState().lastAnswerTime);
        if (timeSpan > 0) {
            timer.setText(dateTimeService().formatTime(timeSpan, false));
            timer.postDelayed(this::bind, 1000);
        } else {
            callback.onNextClick();
        }
    }

    public interface Callback {
        void onNextClick();
    }
}
