package app.laiki.ui.main.questions;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.ui.ContactsActivity;
import app.laiki.ui.views.VariantButtonBackgroundDrawable;
import app.laiki.utils.Utils;
import butterknife.BindView;
import butterknife.OnClick;

import static app.laiki.App.dateTimeService;
import static app.laiki.App.prefs;
import static app.laiki.App.statistics;

public class StopScreenViewHolder extends AbsPageViewHolder {
    private final Callback callback;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.contacts) FrameLayout contacts;

    public StopScreenViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        this(inflater.inflate(R.layout.fr_stopscreen, parent, false), callback);
    }

    public StopScreenViewHolder(View root, Callback callback) {
        super(root);
        this.callback = callback;
    }

    @OnClick(R.id.contacts)
    public void onViewClicked() {
        Activity activity = Utils.getActivity(root);
        if (activity != null) {
            statistics().contacts().start("StopScreen");
            activity.startActivity(new Intent(activity, ContactsActivity.class));
        }
    }

    public void bind() {
        if (title == null)
            return;
        long timeSpan = prefs().config().deadTime - (dateTimeService().getServerTime() - prefs().serviceState().lastAnswerTime);
        if (timeSpan > 0) {
            title.setText(title.getResources().getString(R.string.message_stopscreen, dateTimeService().formatTime(timeSpan, false)));
            title.postDelayed(this::bind, 1000);
        } else {
            callback.onNextClick();
        }
    }

    public interface Callback {
        void onNextClick();
    }
}
