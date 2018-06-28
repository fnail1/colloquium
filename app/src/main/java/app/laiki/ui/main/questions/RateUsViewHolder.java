package app.laiki.ui.main.questions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
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

    private final VariantViewHolder v1;
    private final VariantViewHolder v2;
    private final VariantViewHolder v3;
    private final VariantViewHolder v4;
    private final Callback callback;

    public RateUsViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater, parent);
        this.callback = callback;
        v1 = new VariantViewHolder(variant1, variant1Text1, variant1Text2);
        v2 = new VariantViewHolder(variant2, variant2Text1, variant2Text2);
        v3 = new VariantViewHolder(variant3, variant3Text1, variant3Text2);
        v4 = new VariantViewHolder(variant4, variant4Text1, variant4Text2);
    }

    public void bind() {
        message.setText("Как тебе наша приложуха ЧСН?");
        this.icon.setImageResource(R.drawable.ic_rate_us);
        v1.bind("Норм", null);
        v2.bind("Огонь \uD83D\uDD25", null);
        v3.bind("ХЗ", null);
        v4.bind("Чет не оч", null);
        root.setBackgroundColor(COLORS[(prefs().uniqueId() & 0xffff) % COLORS.length]);
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
                onNegative();

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
                    onComplete();
                }).setNegativeButton("Нет", (dialog, which) -> onComplete())
                .show();
    }

    private void onNegative() {
        onComplete();
    }

    private void onComplete() {
        ServiceState serviceState = prefs().serviceState();
        serviceState.rateUsComplete = true;
        serviceState.rateUsRequired = false;
        prefs().save(serviceState);

        callback.onNextClick();
    }

    public interface Callback {
        void onNextClick();
    }
}
