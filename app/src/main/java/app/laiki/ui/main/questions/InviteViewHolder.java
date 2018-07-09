package app.laiki.ui.main.questions;

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
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.statistics;
import static app.laiki.toolkit.collections.Query.query;

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
        ColorScheme colorScheme = randomColorScheme();
        root.setBackground(colorScheme.background(root.getContext()));
        icon.setBackground(colorScheme.highlight(root.getContext()));
        this.contacts = new Contact[]{contact1, contact2, contact3, contact4};
        for (int i = 0; i < variants.length; i++) {
            variants[i].setText(contacts[i].displayName);
        }
        icon.setImageResource(R.drawable.ic_question_invite);

        setMessage("Каких друзей ты бы позвал в ЧСН? Мы отправим им приглашения. Для тебя это бесплатно и анонимно.");

    }

    @Override
    protected int getAnchorViewId() {
        return R.id.title;
    }

    @Override
    protected int getQuestionTextItemLayoutId() {
        return R.layout.item_invite_text;
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
