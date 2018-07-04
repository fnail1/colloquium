package app.laiki.ui.main.questions;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.List;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import butterknife.BindView;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.statistics;
import static app.laiki.toolkit.collections.Query.query;
import static app.laiki.utils.Utils.dpToPx;

public class InviteViewHolder extends AbsQuestionViewHolder {
    public static final int ANIMATION_DURATION = 500;

    private final Callback callback;

    private final TextView[] variants;
    private Contact[] contacts;

    public InviteViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater.inflate(R.layout.fr_question_invite, parent, false));
        this.callback = callback;
        variants = new TextView[]{variant1Text, variant2Text, variant3Text, variant4Text};
    }

    public void bind(Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        root.setBackground(randomBackground(root.getContext()));
        this.contacts = new Contact[]{contact1, contact2, contact3, contact4};
        for (int i = 0; i < variants.length; i++) {
            variants[i].setText(contacts[i].displayName);
        }
        icon.setImageResource(R.drawable.ic_question_invite);

        setMessage("Каких друзей ты бы позвал в ЧСН? Мы отправим им приглашения. Для тебя это бесплатно и анонимно.");

    }

    @Override
    protected int getAnchor() {
        return R.id.title;
    }

    @NonNull
    @Override
    protected TextView inflateTextView(LayoutInflater inflater, int anchor, char[] chars, int start, int end) {
        ConstraintLayout layout = (ConstraintLayout) this.root;

        ConstraintSet cset = new ConstraintSet();

        TextView tv = (TextView) inflater.inflate(R.layout.item_invite_text, (ViewGroup) root, false);
        tv.setId(View.generateViewId());
        tv.setText(chars, start, end - start);

        layout.addView(tv);
        cset.clone(layout);

        if (textLines.isEmpty())
            cset.connect(tv.getId(), ConstraintSet.TOP, anchor, ConstraintSet.BOTTOM, (int) dpToPx(root.getContext(), 7));
        else
            cset.connect(tv.getId(), ConstraintSet.TOP, anchor, ConstraintSet.BOTTOM);
        cset.connect(tv.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cset.connect(tv.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cset.applyTo(layout);

        textLines.add(tv);
        return tv;
    }

    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.variant1:
                onContactsSelected(Choice.A);
                break;
            case R.id.variant2:
                onContactsSelected(Choice.B);
                break;
            case R.id.variant3:
                onContactsSelected(Choice.C);
                break;
            case R.id.variant4:
                onContactsSelected(Choice.D);
                break;
            case R.id.skip:
                callback.onNextClick();
                break;
        }
    }

    private void onContactsSelected(Choice choice) {
        statistics().contacts().inviteSent();
        Contact contact = contacts[choice.ordinal()];
        TextView v = variants[choice.ordinal()];
        v.setSelected(true);
        v.setEnabled(false);
        appService().contactUpdated.add(new AppService.ContactUpdatedEventHandler() {
            @Override
            public void onContactUpdated(Contact args) {
                if (args.equals(contact) && args.flags.get(Contact.FLAG_INVITE_REQUESTED)) {
                    appService().contactUpdated.remove(this);
                    onInviteRequested(args, choice);
                }
            }
        });
        appService().sendInvite(contact);
    }

    private synchronized void onInviteRequested(Contact args, Choice choice) {
        List<Contact> candidates = data().contacts.selectInviteVariants(0, 5).toList();
        next:
        for (Contact c : candidates) {
            for (Contact existing : contacts) {
                if (c.equals(existing))
                    continue next;
            }

            root.post(() -> {
                contacts[choice.ordinal()] = c;
                TextView v = variants[choice.ordinal()];
                bindContactWithAnimation(c, v);

            });
            break;
        }

        if (query(contacts).first(c -> !c.flags.get(Contact.FLAG_INVITE_REQUESTED)) == null)
            root.post(callback::onNextClick);
    }

    private void bindContactWithAnimation(Contact c, TextView v) {
        Animation out = new AlphaAnimation(0, 1);
        out.setDuration(ANIMATION_DURATION);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation in = new AlphaAnimation(1, 0);
        in.setDuration(ANIMATION_DURATION);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setText(c.displayName);
                v.setSelected(false);
                v.startAnimation(out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(in);
    }

    public interface Callback {
        void onNextClick();
    }
}
