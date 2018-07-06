package app.laiki.ui.main.questions;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.laiki.R;
import app.laiki.model.types.Choice;
import app.laiki.service.ServiceState;
import app.laiki.utils.Utils;
import butterknife.OnClick;

import static app.laiki.App.prefs;
import static app.laiki.App.statistics;

public class RateUsViewHolder extends AbsQuestionViewHolder {

    private final Callback callback;

    public RateUsViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater, parent);
        this.callback = callback;
    }

    public void bind() {
        setMessage("Как тебе наша приложуха ЧСН?");
        this.icon.setImageResource(R.drawable.ic_rate_us);
        variant1Text.setText("Огонь \uD83D\uDD25");
        variant2Text.setText("Норм");
        variant3Text.setText("ХЗ");
        variant4Text.setText("Чет не оч");
        root.setBackground(randomBackground(root.getContext()));
        progress.setVisibility(View.GONE);
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                statistics().rateUs().answer(Choice.A);
                onPositive();
                break;
            case R.id.variant2:
                statistics().rateUs().answer(Choice.B);
                onPositive();
                break;
            case R.id.variant3:
                statistics().rateUs().answer(Choice.C);
                onNegative();
                break;
            case R.id.variant4:
                statistics().rateUs().answer(Choice.D);
                onNegative();
                break;
            case R.id.skip:
                statistics().rateUs().answer(Choice.E);
                onComplete(false);

                //no break;
            case R.id.next:
            case R.id.root:
            case R.id.page1:
            case R.id.page2:
                break;
        }
    }

    private void onPositive() {
        Context context = root.getContext();
        if (context == null)
            return;

        new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setMessage("А поставишь нам пятерочку? Ну плэзз...")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Utils.startGooglePlay(context);
                    statistics().rateUs().googlePlayStarted();
                    onComplete(true);
                }).setNegativeButton("Нет", (dialog, which) -> onComplete(true))
                .show();
    }

    private void onNegative() {
        onComplete(true);
    }

    private void onComplete(boolean complete) {
        ServiceState serviceState = prefs().serviceState();
        serviceState.rateUsComplete = complete;
        serviceState.rateUsRequired = false;
        prefs().save(serviceState);

        callback.onNextClick();
    }

    public interface Callback {
        void onNextClick();
    }
}
