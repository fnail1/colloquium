package app.laiki.ui.main.contacts;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import app.laiki.R;
import app.laiki.model.AppData;
import app.laiki.model.entities.Contact;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.utils.AntiDoubleClickLock;
import app.laiki.utils.AvatarBuilder;
import app.laiki.utils.photomanager.PhotoRequest;

import static app.laiki.App.data;
import static app.laiki.App.photos;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.invite) TextView invite;
    private Contact contact;

    public ContactViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_contact, parent, false));
    }

    public ContactViewHolder(View root) {
        super(root);
        ButterKnife.bind(this, root);
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
        invite.setVisibility(contact.inviteSent ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.invite)
    public void onViewClicked() {
        if (!AntiDoubleClickLock.onClick(this, R.id.invite))
            return;
        contact.inviteSent = true;
        Runnable runnable = () -> data().contacts.save(contact);
        ThreadPool.DB.execute(runnable);
        bind(contact);
    }
}
