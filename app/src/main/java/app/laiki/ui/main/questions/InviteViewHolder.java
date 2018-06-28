package app.laiki.ui.main.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import butterknife.BindView;
import butterknife.OnClick;

public class InviteViewHolder extends AbsQuestionViewHolder {

    private final Callback callback;

    private final ContactViewHolder v1;
    private final ContactViewHolder v2;
    private final ContactViewHolder v3;
    private final ContactViewHolder v4;
    @BindView(R.id.progress) ProgressBar progress;
    private Question question;
    private Contact contact1;
    private Contact contact2;
    private Contact contact3;
    private Contact contact4;

    public InviteViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater, parent);
        this.callback = callback;
        v1 = new ContactViewHolder(variant1, variant1Text1, variant1Text2);
        v2 = new ContactViewHolder(variant2, variant2Text1, variant2Text2);
        v3 = new ContactViewHolder(variant3, variant3Text1, variant3Text2);
        v4 = new ContactViewHolder(variant4, variant4Text1, variant4Text2);
    }

    public void bind(Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.contact3 = contact3;
        this.contact4 = contact4;

        v1.bind(contact1);
        v2.bind(contact2);
        v3.bind(contact3);
        v4.bind(contact4);
        icon.setImageResource(R.drawable.ic_question_invite);
        message.setText("Каких друзей ты бы позвал в ЧСН? Мы отправим им приглашения. Для тебя это бесплатно и анонимно.");
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next})
    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.variant1:
//                sendInviteAnd
//                break;
//            case R.id.variant2:
//                callback.onQuestionAnswered(Choice.B);
//                onAnswer();
//                break;
//            case R.id.variant3:
//                callback.onQuestionAnswered(Choice.C);
//                onAnswer();
//                break;
//            case R.id.variant4:
//                callback.onQuestionAnswered(Choice.D);
//                onAnswer();
//                break;
//            case R.id.skip:
//                callback.onQuestionAnswered(Choice.E);
//                //no break;
//            case R.id.next:
//            case R.id.root:
//            case R.id.page1:
//            case R.id.page2:
//                callback.onNextClick();
//                progress.setVisibility(View.VISIBLE);
//                rebind();
//                break;
//        }
    }

    public interface Callback {
        void onNextClick();
    }
}
