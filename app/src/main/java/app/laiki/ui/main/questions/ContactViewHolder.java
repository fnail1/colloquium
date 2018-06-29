package app.laiki.ui.main.questions;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Contact;

public class ContactViewHolder extends VariantViewHolder {

    public static final int ANIMATION_DURATION = 500;

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
        bind(sb, contact.middleName);
    }

    public void bind(String name) {
        bind(name, null);
    }

    public void animateBind(Contact contact, @NonNull AnimationCallback callback) {

        text1.animate()
                .alpha(0)
//                .scaleX(0.85f)
//                .scaleY(0.85f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> {
                    bind(contact);
                    callback.onAnimationInComplete();
                    text1.animate()
                            .alpha(1)
//                            .scaleX(1)
//                            .scaleY(1)
                            .setDuration(ANIMATION_DURATION)
                            .withEndAction(callback::onAnimationOutComplete);
                });
        text2.animate()
                .alpha(0)
//                .scaleX(0.85f)
//                .scaleY(0.85f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> {
                    text2.animate()
                            .alpha(1)
//                            .scaleX(1)
//                            .scaleY(1)
                            .setDuration(ANIMATION_DURATION);
                });
        return;
    }


    private void append(StringBuilder sb, String s) {
        if (s == null)
            return;
        sb.append(s).append(' ');
    }

    public interface AnimationCallback {
        void onAnimationInComplete();

        void onAnimationOutComplete();
    }
}
