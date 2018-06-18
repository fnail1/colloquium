package ru.mail.colloquium.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;
import ru.mail.colloquium.ui.base.BaseActivity;
import ru.mail.colloquium.ui.main.questions.VariantViewHolder;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class AnswerActivity extends BaseActivity {
    public static final String EXTRA_ANSWER_ID = "answer_id";

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.answers) LinearLayout answers;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.root) RelativeLayout root;
    @BindView(R.id.back) ImageView back;
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
    private Answer answer;
    private VariantViewHolder v1;
    private VariantViewHolder v2;
    private VariantViewHolder v3;
    private VariantViewHolder v4;
    private volatile HashMap<String, Contact> contacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);
        v1 = new VariantViewHolder(variant1, variant1Text1, variant1Text2);
        v2 = new VariantViewHolder(variant2, variant2Text1, variant2Text2);
        v3 = new VariantViewHolder(variant3, variant3Text1, variant3Text2);
        v4 = new VariantViewHolder(variant4, variant4Text1, variant4Text2);

        long id = getIntent().getLongExtra(EXTRA_ANSWER_ID, 0);

        ThreadPool.DB.execute(() -> {
            answer = data().answers.selectById(id);
            contacts = data().contacts.select(answer).toMap(c -> c.serverId);
            runOnUiThread(this::bindData);
        });

        answers.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            answers.post(this::bindData);
        });

    }

    private void bindData() {
        if (contacts == null || answers.getWidth() == 0) {
            return;
        }

        switch (answer.gender) {
            case CAMEL:
                root.setBackgroundColor(0xffF5F5F5);
                getWindow().setStatusBarColor(0xffF5F5F5);
                icon.setImageResource(R.drawable.ic_camel);
                break;
            case MALE:
                root.setBackgroundColor(0xff2767A9);
                getWindow().setStatusBarColor(0xff2767A9);
                icon.setImageResource(R.drawable.ic_male);
                break;
            case FEMALE:
                root.setBackgroundColor(0xffED1C45);
                getWindow().setStatusBarColor(0xffED1C45);
                icon.setImageResource(R.drawable.ic_female);
                break;
        }

        author.setText(getString(answer.gender.nameResId) + ", " + getResources().getString(answer.age.nameResId));

        message.setText(answer.questionText);

        bindVariant(answer.variantA, Choice.A, variant1, v1);
        bindVariant(answer.variantB, Choice.B, variant2, v2);
        bindVariant(answer.variantC, Choice.C, variant3, v3);
        bindVariant(answer.variantD, Choice.D, variant4, v4);
    }

    private void bindVariant(String contactServerId, Choice expected, LinearLayout viewRoot, VariantViewHolder viewText) {
        Contact contact = contacts.get(contactServerId);
        if (answer.answer != expected) {
            viewRoot.setAlpha(.5f);
            viewText.bind(contact);
        } else {
            if (answer.answerName != null) {
                viewText.bind(answer.answerName);
            } else {
                viewText.bind(contact);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appService().answerViewed(answer);
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
