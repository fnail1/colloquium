package app.laiki.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.model.entities.Contact;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.base.BaseActivity;
import app.laiki.ui.main.questions.QuestionViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.statistics;

public class AnswerActivity extends BaseActivity {
    public static final String EXTRA_ANSWER_ID = "answer_id";

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.root) RelativeLayout root;
    @BindView(R.id.back) ImageView back;
    @BindView(R.id.variant1) View variant1;
    @BindView(R.id.variant2) View variant2;
    @BindView(R.id.variant3) View variant3;
    @BindView(R.id.variant4) View variant4;
    @BindView(R.id.variant1text) TextView variant1text;
    @BindView(R.id.variant2text) TextView variant2text;
    @BindView(R.id.variant3text) TextView variant3text;
    @BindView(R.id.variant4text) TextView variant4text;
    @BindView(R.id.copyright) TextView copyright;
    private Answer answer;
    private boolean readSent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);

        long id = getIntent().getLongExtra(EXTRA_ANSWER_ID, 0);

        ThreadPool.DB.execute(() -> {
            answer = data().answers.selectById(id);
            HashMap<String, Contact> contacts = data().contacts.select(answer).toMap(c -> c.serverId);
            runOnUiThread(() -> bindData(contacts));
        });
    }

    private void bindData(HashMap<String, Contact> contacts) {
        if (!readSent) {
            readSent = true;
            statistics().answers().read();
            appService().answerRead(answer);
        }

        root.setBackground(QuestionViewHolder.randomBackground(this));
        switch (answer.gender) {
            case CAMEL:
                icon.setImageResource(R.drawable.ic_camel);
                copyright.setText("#ЧСН ❤️ chsn.app");
                break;
            case MALE:
                icon.setImageResource(R.drawable.ic_male);
                copyright.setText("#ЧСН ❤️ chsn.app");
                break;
            case FEMALE:
                icon.setImageResource(R.drawable.ic_female);
                copyright.setText("#ЧСН \uD83D\uDC99️ chsn.app");
                break;
        }

        author.setText(getString(answer.gender.nameResId) + ", " + getResources().getString(answer.age.nameResId));

        message.setText(answer.questionText);

        bindVariantText(variant1text, Choice.A, contacts.get(answer.variantA));
        bindVariantText(variant2text, Choice.B, contacts.get(answer.variantB));
        bindVariantText(variant3text, Choice.C, contacts.get(answer.variantC));
        bindVariantText(variant4text, Choice.D, contacts.get(answer.variantD));

        root.postDelayed(() -> {

            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float alpha, Transformation t) {
                    super.applyTransformation(alpha, t);

                    if (answer.answer != Choice.A) applyAnimation(variant1, alpha);
                    if (answer.answer != Choice.B) applyAnimation(variant2, alpha);
                    if (answer.answer != Choice.C) applyAnimation(variant3, alpha);
                    if (answer.answer != Choice.D) applyAnimation(variant4, alpha);
                }
            };
            a.setDuration(450);
            root.startAnimation(a);

        }, 500);
    }

    private void applyAnimation(View variantView, float alpha) {
        variantView.setAlpha(.5f + (1 - alpha) / 2);
        float elevation = 12 * (1 - alpha);
        if (elevation < 1f)
            elevation = 1f;
        variantView.setElevation(elevation);
    }

    private void bindVariantText(TextView viewText, Choice expected, Contact contact) {
        boolean selected = answer.answer == expected;
        if (selected && answer.answerName != null)
            viewText.setText(answer.answerName);
        else if (contact == null)
            viewText.setText(R.string.hidden);
        else
            viewText.setText(contact.displayName);

    }


    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
