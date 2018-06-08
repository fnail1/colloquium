package ru.mail.colloquium.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
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
import ru.mail.colloquium.ui.base.BaseActivity;

import static ru.mail.colloquium.App.appService;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.photos;
import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class AnswerActivity extends BaseActivity {
    public static final String EXTRA_ANSWER_ID = "answer_id";

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.variant1) TextView variant1;
    @BindView(R.id.variant2) TextView variant2;
    @BindView(R.id.variant3) TextView variant3;
    @BindView(R.id.variant4) TextView variant4;
    @BindView(R.id.answers) LinearLayout answers;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.root) RelativeLayout root;
    @BindView(R.id.back) ImageView back;
    private Answer answer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);

        long id = getIntent().getLongExtra(EXTRA_ANSWER_ID, 0);
        answer = data().answers.selectById(id);
        if (answer == null) {
            safeThrow(new IllegalArgumentException("id = " + id));
            finish();
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

        author.setText(answer.gender.localName(this) + ", " + answer.age.localName(this));

        message.setText(answer.questionText);

        HashMap<String, Contact> contacts = data().contacts.select(answer).toMap(c -> c.serverId);

        Contact a = contacts.get(answer.variantA);
        variant1.setText(a != null ? a.displayName : "?");

        Contact b = contacts.get(answer.variantB);
        variant2.setText(b != null ? b.displayName : "?");

        Contact c = contacts.get(answer.variantC);
        variant3.setText(c != null ? c.displayName : "?");

        Contact d = contacts.get(answer.variantD);
        variant4.setText(d != null ? d.displayName : "?");

        switch (answer.answer) {
            case A:
                variant1.setBackgroundResource(R.drawable.bg_white_button_selected);
                break;
            case B:
                variant2.setBackgroundResource(R.drawable.bg_white_button_selected);
                break;
            case C:
                variant3.setBackgroundResource(R.drawable.bg_white_button_selected);
                break;
            case D:
                variant4.setBackgroundResource(R.drawable.bg_white_button_selected);
                break;
            case E:
                safeThrow(new Exception("No answer: " + answer.serverId));
                break;
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
