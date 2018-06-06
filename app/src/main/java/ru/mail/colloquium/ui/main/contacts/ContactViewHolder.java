package ru.mail.colloquium.ui.main.contacts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.utils.photomanager.PhotoRequest;

import static ru.mail.colloquium.App.networkObserver;
import static ru.mail.colloquium.App.photos;

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
        photos().attach(avatar,contact.avatar)
                .circle()
                .placeholder(new PhotoRequest.AbstractPlaceholder<ImageView>() {
                    @Override
                    protected void apply(PhotoRequest<ImageView> request) {
                        ImageView imageView = request.viewHolder.viewRef.get();
//                        imageView.setImageDrawable();
                    }
                }).commit();
        name.setText(contact.displayName);
    }
}
