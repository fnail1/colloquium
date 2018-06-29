package app.laiki.ui.main.contacts;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Contact;
import app.laiki.utils.AntiDoubleClickLock;
import app.laiki.utils.AvatarBuilder;
import app.laiki.utils.photomanager.PhotoRequest;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.photos;
import static app.laiki.App.statistics;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.invite) TextView invite;
    @BindView(R.id.progress) ProgressBar progress;
    private Contact contact;
    private RecyclerView.Adapter adapter;

    public ContactViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_contact, parent, false));
    }

    public ContactViewHolder(View root) {
        super(root);
        ButterKnife.bind(this, root);
        progress.setVisibility(View.GONE);
    }

    public void bind(Contact contact) {
        this.contact = contact;
        if (TextUtils.isEmpty(contact.avatar)) {
            avatar.setImageDrawable(AvatarBuilder.build(contact));
        } else {
            photos().attach(avatar, contact.avatar)
                    .circle()
                    .placeholder(new PhotoRequest.AbstractPlaceholder<ImageView>() {
                        @Override
                        protected void apply(PhotoRequest<ImageView> request) {
                            ImageView imageView = request.viewHolder.viewRef.get();
                            imageView.setImageDrawable(AvatarBuilder.build(contact));
                        }
                    }).commit();
        }
        name.setText(contact.displayName);


        boolean inviteSent = contact.flags.get(Contact.FLAG_INVITE_SENT);
        if (contact.flags.get(Contact.FLAG_INVITE_REQUESTED) && !inviteSent) {
            progress.setVisibility(View.VISIBLE);
            invite.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
            invite.setVisibility(View.VISIBLE);
            invite.setEnabled(!inviteSent);
            invite.setText(inviteSent ? "Отправили" : "Отправить");
        }
    }

    @OnClick(R.id.invite)
    public void onViewClicked() {
        if (!AntiDoubleClickLock.onClick(this, R.id.invite))
            return;
        statistics().contacts().invite();
        appService().sendInvite(contact);

        bind(contact);
    }
}
