package app.laiki.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.prefs;

public class LoginPage1IntroViewHolder implements LoginActivity.LoginPageViewHolder {

    private final View root;
    @BindView(R.id.button) View button;
    @BindView(R.id.policy) TextView policy;

    public LoginPage1IntroViewHolder(View root) {
        this.root = root;
        ButterKnife.bind(this, root);
        policy.setText(buildLicenceText());
        policy.setFocusable(true);
        policy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        LoginActivity activity = (LoginActivity) Utils.getActivity(root);
        activity.onContinue();
    }

    private SpannableStringBuilder buildLicenceText() {
        String t1 = "Продолжая, вы соглашаетсясь с ";
        String t2 = "политикой конфиденциальности";
        SpannableStringBuilder message = new SpannableStringBuilder(t1 + t2);

        ClickableSpan licence = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Activity activity = Utils.getActivity(policy);
                if (activity == null)
                    return;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(prefs().getApiSet().webUrl + "/privacy"));
                activity.startActivity(intent);
            }
        };

        message.setSpan(licence, t1.length(), t1.length() + t2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        return message;
    }
}
