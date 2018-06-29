package app.laiki.ui.main.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.model.types.Choice;
import app.laiki.service.AppService;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.prefs;
import static app.laiki.App.statistics;
import static app.laiki.toolkit.collections.Query.query;

public class InviteViewHolder extends AbsQuestionViewHolder {

    private final Callback callback;

    private final ContactViewHolder[] variants;
    private Contact[] contacts;

    public InviteViewHolder(LayoutInflater inflater, ViewGroup parent, Callback callback) {
        super(inflater.inflate(R.layout.fr_question_invite, parent, false));
        this.callback = callback;
        variants = new ContactViewHolder[]{
                new ContactViewHolder(variant1, variant1Text1, variant1Text2),
                new ContactViewHolder(variant2, variant2Text1, variant2Text2),
                new ContactViewHolder(variant3, variant3Text1, variant3Text2),
                new ContactViewHolder(variant4, variant4Text1, variant4Text2)};
    }

    public void bind(Contact contact1, Contact contact2, Contact contact3, Contact contact4) {
        root.setBackgroundColor(COLORS[(prefs().uniqueId() & 0xffff) % COLORS.length]);
        this.contacts = new Contact[]{contact1, contact2, contact3, contact4};
        for (int i = 0; i < variants.length; i++) {
            variants[i].bind(contacts[i]);
        }
        icon.setImageResource(R.drawable.ic_question_invite);

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
        ContactViewHolder v = variants[choice.ordinal()];
        v.root.setSelected(true);
        v.root.setEnabled(false);
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
                ContactViewHolder v = variants[choice.ordinal()];
                v.animateBind(c, new ContactViewHolder.AnimationCallback() {
                    @Override
                    public void onAnimationInComplete() {
                        v.root.setSelected(false);
                    }

                    @Override
                    public void onAnimationOutComplete() {
                        v.root.setEnabled(true);
                    }
                });
            });
            break;
        }

        if (query(contacts).first(c -> !c.flags.get(Contact.FLAG_INVITE_REQUESTED)) == null)
            callback.onNextClick();
    }


    public interface Callback {
        void onNextClick();
    }
}
