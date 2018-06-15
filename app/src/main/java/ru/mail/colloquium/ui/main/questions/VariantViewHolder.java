package ru.mail.colloquium.ui.main.questions;

import android.view.View;
import android.widget.TextView;

import ru.mail.colloquium.R;
import ru.mail.colloquium.model.entities.Contact;

public class VariantViewHolder {
    private final View root;
    private final TextView text1;
    private final TextView text2;

    public VariantViewHolder(View root, TextView text1, TextView text2) {
        this.root = root;
        this.text1 = text1;
        this.text2 = text2;
    }

    public void bind(Contact contact) {
        if (contact == null) {
            text1.setText(R.string.hidden);
            text2.setVisibility(View.GONE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        append(sb, contact.namePrefix);
        append(sb, contact.firstName);
        append(sb, contact.middleName);
        append(sb, contact.lastName);
        float w = text1.getPaint().measureText(sb, 0, sb.length() - 1);

        int width = root.getWidth() - root.getPaddingLeft() - root.getPaddingRight();
        if (w < width) {
            sb.delete(sb.length() - 1, sb.length());
            text1.setText(sb);
            text2.setVisibility(View.GONE);
            return;
        }

        if (contact.firstName != null && contact.lastName != null) {
            sb = new StringBuilder();
            append(sb, contact.namePrefix);
            append(sb, contact.firstName);
            append(sb, contact.middleName);
            sb.delete(sb.length() - 1, sb.length());
            text1.setText(sb);

            text2.setVisibility(View.VISIBLE);
            text2.setText(contact.lastName);
            return;
        }

        if (contact.firstName == null) {
            sb = new StringBuilder();
            append(sb, contact.namePrefix);
            append(sb, contact.lastName);
            sb.delete(sb.length() - 1, sb.length());
            text1.setText(sb);
            text2.setVisibility(View.GONE);
            return;
        }

        sb = new StringBuilder();
        append(sb, contact.namePrefix);
        append(sb, contact.firstName);
        sb.delete(sb.length() - 1, sb.length());
        text1.setText(sb);

        text2.setVisibility(View.VISIBLE);
        text2.setText(contact.middleName);

    }

    public void bind(String name) {
        text1.setText(name);
        text1.setSingleLine(false);
        text1.setMaxLines(2);
        text2.setVisibility(View.GONE);
    }

    private void append(StringBuilder sb, String s) {
        if (s == null)
            return;
        sb.append(s).append(' ');
    }
}
