package app.laiki.ui.main.questions;

import android.view.View;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Contact;

public class ContactViewHolder extends VariantViewHolder {

    public ContactViewHolder(View root, TextView text1, TextView text2) {
        super(root, text1, text2);
    }

    public void bind(Contact contact) {
        if (contact == null) {
            bind(R.string.hidden, null);
            return;
        }

        StringBuilder sb = new StringBuilder();
        append(sb, contact.namePrefix);
        append(sb, contact.firstName);
        append(sb, contact.middleName);
        append(sb, contact.lastName);

        if (sb.length() == 0) {
            bind(contact.displayName, null);
            return;
        }

        float w = getPaint().measureText(sb, 0, sb.length() - 1);

        int width = root.getWidth() - root.getPaddingLeft() - root.getPaddingRight();
        if (w < width) {
            sb.delete(sb.length() - 1, sb.length());
            bind(sb, null);
            return;
        }

        if (contact.firstName != null && contact.lastName != null) {
            sb = new StringBuilder();
            append(sb, contact.namePrefix);
            append(sb, contact.firstName);
            append(sb, contact.middleName);
            sb.delete(sb.length() - 1, sb.length());
            bind(sb, contact.lastName);
            return;
        }

        if (contact.firstName == null) {
            sb = new StringBuilder();
            append(sb, contact.namePrefix);
            append(sb, contact.lastName);
            sb.delete(sb.length() - 1, sb.length());
            bind(sb, null);
            return;
        }

        sb = new StringBuilder();
        append(sb, contact.namePrefix);
        append(sb, contact.firstName);
        sb.delete(sb.length() - 1, sb.length());
        bind(sb,contact.middleName);
    }

    public void bind(String name) {
        bind(name,null);
    }

    private void append(StringBuilder sb, String s) {
        if (s == null)
            return;
        sb.append(s).append(' ');
    }
}
