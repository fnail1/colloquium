package ru.mail.colloquium.ui.main.questions;

import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.toolkit.concurrent.ThreadPool;

import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.App.photos;

public class QuestionViewHolder {

    public static final int[] COLORS = {
            0xFF1A1334, 0xFF26294A, 0xFF02545A, 0xFF0C7351,
            0xFFAAD962, 0xFFFBBF45, 0xFFEF6A32, 0xFFED1C45,
            0xFFA12A5E, 0xFFA12A5E, 0xFF2767A9, 0xFF14407F,
            0xFF14407F};

    private final QuestionAnsweredCallback callback;
    public final View root;
    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.variant1) TextView variant1;
    @BindView(R.id.variant2) TextView variant2;
    @BindView(R.id.variant3) TextView variant3;
    @BindView(R.id.variant4) TextView variant4;
    @BindView(R.id.answers) LinearLayout answers;
    @BindView(R.id.skip) TextView skip;
    @BindView(R.id.message) TextView message;

    /**
     * @param callback
     * @param root     R.layout.fr_question
     */
    public QuestionViewHolder(View root, QuestionAnsweredCallback callback) {
        this.callback = callback;
        this.root = root;
        ButterKnife.bind(this, root);
    }

    public void bind(Question question) {
        root.setBackgroundColor(COLORS[(question.serverId.hashCode() & 0xff) % COLORS.length]);
        photos().attach(icon, question.emojiUrl)
                .size(
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size),
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size))
                .commit();
        message.setText(question.question);
        List<Contact> contacts;
        if (question.variant1 <= 0) {
            contacts = data().contacts.selectRandom(4).toList();
        } else {
            contacts = data().contacts.selectById(question.variant1, question.variant2, question.variant3, question.variant4).toList();
        }
        Collections.sort(contacts, (c1, c2) -> c1.displayNameOrder.compareTo(c2.displayNameOrder));

        Contact contact1 = contacts.get(0);
        Contact contact2 = contacts.get(1 % contacts.size());
        Contact contact3 = contacts.get(2 % contacts.size());
        Contact contact4 = contacts.get(3 % contacts.size());

        if (question.variant1 == 0) {
            question.variant1 = contact1._id;
            question.variant2 = contact2._id;
            question.variant3 = contact3._id;
            question.variant4 = contact4._id;
            ThreadPool.DB.execute(() -> {
                data().questions.save(question);
            });
        }
        variant1.setText(contact1.displayName);
        variant2.setText(contact2.displayName);
        variant3.setText(contact3.displayName);
        variant4.setText(contact4.displayName);
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
