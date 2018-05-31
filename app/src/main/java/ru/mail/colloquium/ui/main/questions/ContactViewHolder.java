package ru.mail.colloquium.ui.main.questions;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.ui.views.ContactNameView;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.title) ContactNameView title;

    public ContactViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_contact, parent, false));
    }

    public ContactViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Contact contact) {
        title.setContact(contact);
    }
}
