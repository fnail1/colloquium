package app.laiki.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
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
import app.laiki.utils.GraphicUtils;
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
    private volatile HashMap<String, Contact> contacts;
    private boolean readSent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);

        long id = getIntent().getLongExtra(EXTRA_ANSWER_ID, 0);

        ThreadPool.DB.execute(() -> {
            answer = data().answers.selectById(id);
            contacts = data().contacts.select(answer).toMap(c -> c.serverId);
            runOnUiThread(this::bindData);
        });
    }

    private void bindData() {
        if (contacts == null) {
            return;
        }

        if (!readSent) {
            readSent = true;
            statistics().answers().read();
            appService().answerRead(answer);
        }

        switch (answer.gender) {
            case CAMEL:
                root.setBackground(GraphicUtils.getDrawable(this, R.drawable.bg_q11));
                icon.setImageResource(R.drawable.ic_camel);
                copyright.setText("#ЧСН ❤️ chsn.app");
                break;
            case MALE:
                root.setBackground(GraphicUtils.getDrawable(this, R.drawable.bg_q08));
                icon.setImageResource(R.drawable.ic_male);
                copyright.setText("#ЧСН ❤️ chsn.app");
                break;
            case FEMALE:
                root.setBackground(GraphicUtils.getDrawable(this, R.drawable.bg_q02));
                icon.setImageResource(R.drawable.ic_female);
                copyright.setText("#ЧСН \uD83D\uDC99️ chsn.app");
                break;
        }

        author.setText(getString(answer.gender.nameResId) + ", " + getResources().getString(answer.age.nameResId));

        message.setText(answer.questionText);

        bindVariant(answer.variantA, Choice.A, variant1, variant1text);
        bindVariant(answer.variantB, Choice.B, variant2, variant2text);
        bindVariant(answer.variantC, Choice.C, variant3, variant3text);
        bindVariant(answer.variantD, Choice.D, variant4, variant4text);
    }

    private void bindVariant(String contactServerId, Choice expected, View viewRoot, TextView viewText) {
        Contact contact = contacts.get(contactServerId);
        viewRoot.setEnabled(false);
        boolean selected = answer.answer == expected;
        viewRoot.setSelected(selected);
        if (!selected) {
            viewRoot.setAlpha(.5f);
            viewText.setText(contact.displayName);
        } else {
            if (answer.answerName != null) {
                viewText.setText(answer.answerName);
            } else {
                viewText.setText(contact.displayName);
            }
        }
    }


    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
