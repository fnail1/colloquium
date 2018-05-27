package ru.mail.colloquium.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.colloquium.R;
import ru.mail.colloquium.model.types.ContactPhoneNumber;

public class PhoneViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.title) TextView title;
    @BindView(R.id.ab_id) TextView abId;

    public PhoneViewHolder(LayoutInflater inflater, ViewGroup parent) {
        this(inflater.inflate(R.layout.item_phone, parent, false));
    }

    public PhoneViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(ContactPhoneNumber number) {
        abId.setText(String.valueOf(number.link.abPhoneId));
        title.setText(number.normalized);
    }
}
