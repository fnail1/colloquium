package app.laiki.ui.main.questions;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;

import static app.laiki.App.photos;

public class QuestionViewHolder {

    public static final int[] COLORS = {
            0xFF1A1334, 0xFF26294A, 0xFF02545A, 0xFF0C7351,
            0xFFAAD962, 0xFFFBBF45, 0xFFEF6A32, 0xFFED1C45,
            0xFFA12A5E, 0xFFA12A5E, 0xFF2767A9, 0xFF14407F,
            0xFF14407F};

    private final QuestionAnsweredCallback callback;
    public final View root;
    @BindView(R.id.icon) ImageView icon;

    @BindView(R.id.answers) LinearLayout answers;
    @BindView(R.id.skip) TextView skip;
    @BindView(R.id.message) TextView message;
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
    private final VariantViewHolder v1;
    private final VariantViewHolder v2;
    private final VariantViewHolder v3;
    private final VariantViewHolder v4;
    private Question question;
    private Contact contact1;
    private Contact contact2;
    private Contact contact3;
    private Contact contact4;

    /**
     * @param callback
     * @param root     R.layout.fr_question
     */
    public QuestionViewHolder(View root, QuestionAnsweredCallback callback) {
        this.callback = callback;
        this.root = root;
        ButterKnife.bind(this, root);
        v1 = new VariantViewHolder(variant1, variant1Text1, variant1Text2);
        v2 = new VariantViewHolder(variant2, variant2Text1, variant2Text2);
        v3 = new VariantViewHolder(variant3, variant3Text1, variant3Text2);
        v4 = new VariantViewHolder(variant4, variant4Text1, variant4Text2);
//        R.layout.fr_question
    }

    public void bind(Question question, Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        this.question = question;
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.contact3 = contact3;
        this.contact4 = contact4;
        root.setBackgroundColor(COLORS[(question.uniqueId.hashCode() & 0xffff) % COLORS.length]);
        photos().attach(icon, question.emojiUrl)
                .size(
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size),
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size))
                .commit();
        message.setText(question.question);

        v1.bind(contact1);
        v2.bind(contact2);
        v3.bind(contact3);
        v4.bind(contact4);
    }

    public void rebind() {
        if (question == null)
            return;
        bind(question, contact1, contact2, contact3, contact4);
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                callback.onQuestionAnswered(Choice.A);
                break;
            case R.id.variant2:
                callback.onQuestionAnswered(Choice.B);
                break;
            case R.id.variant3:
                callback.onQuestionAnswered(Choice.C);
                break;
            case R.id.variant4:
                callback.onQuestionAnswered(Choice.D);
                break;
            case R.id.skip:
                callback.onQuestionAnswered(Choice.E);
                break;
        }
    }

    public interface QuestionAnsweredCallback {
        void onQuestionAnswered(Choice a);
    }
}
