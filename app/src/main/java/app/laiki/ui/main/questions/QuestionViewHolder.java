package app.laiki.ui.main.questions;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.laiki.R;
import app.laiki.diagnostics.Logger;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.ui.base.AnswerButtonHelper;
import butterknife.OnClick;

import static app.laiki.App.data;
import static app.laiki.App.photos;
import static app.laiki.App.screenMetrics;

@SuppressWarnings("ConstantConditions")
public class QuestionViewHolder extends QuestionOfContactsViewHolder {



    /**
     * @param callback
     * @param root     R.layout.fr_question
     */
    @SuppressWarnings("WeakerAccess")
    public QuestionViewHolder(View root, Callback callback) {
        super(root, callback);

        root.setOnClickListener(this::onViewClicked);

//        R.layout.fr_question
    }

    public void bind(Question question, List<Contact> contacts) {
        super.bind(question, contacts);
        rebind();
    }

    public void rebind() {
        if (question == null)
            return;

        ColorScheme colorScheme = randomColorScheme(question.uniqueId.hashCode());
        root.setBackground(colorScheme.background(root.getContext()));
        icon.setBackground(colorScheme.highlight(root.getContext()));
        photos().attach(icon, question.emojiUrl)
                .size(
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size),
                        icon.getResources().getDimensionPixelOffset(R.dimen.question_icon_size))
                .commit();
        setMessage(question.question);

        if (question.answer == null) {
            skip.setVisibility(View.VISIBLE);
            next.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        } else {
            skip.setVisibility(View.GONE);
            if (question.answer == Choice.E) {
                next.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            } else {
                if (progress.getVisibility() == View.GONE)
                    next.setVisibility(View.VISIBLE);
                else
                    next.setVisibility(View.GONE);

            }
        }

        Choice[] values = {Choice.A, Choice.B, Choice.C, Choice.D};
        for (int i = 0; i < values.length; i++) {
            AnswerButtonHelper.bindVariant(question.answer, values[i], variants[i], variantsTextViews[i], contacts[i], this);
        }

    }


    @OnClick({R.id.variant1, R.id.variant2, R.id.variant3, R.id.variant4, R.id.skip, R.id.next, R.id.shuffle})
    public void onViewClicked(View view) {
        if (!animatingViews.isEmpty())
            return;

        switch (view.getId()) {
            case R.id.variant1:
                callback.onQuestionAnswered(Choice.A);
                onAnswer();
                break;
            case R.id.variant2:
                callback.onQuestionAnswered(Choice.B);
                onAnswer();
                break;
            case R.id.variant3:
                callback.onQuestionAnswered(Choice.C);
                onAnswer();
                break;
            case R.id.variant4:
                callback.onQuestionAnswered(Choice.D);
                onAnswer();
                break;
            case R.id.shuffle:
                onShuffleClick();
                break;
            case R.id.skip:
                callback.onQuestionAnswered(Choice.E);
                //no break;
            case R.id.next:
            case R.id.root:
            case R.id.page1:
            case R.id.page2:
                if (question.answer != null) {
                    callback.onNextClick();
                    progress.setVisibility(View.VISIBLE);
                    rebind();
                }
                break;
        }
    }


    private void onAnswer() {
        rebind();
        next.setAlpha(0);
        next.setTranslationY(screenMetrics().screen.height - next.getY());
        next.animate()
                .setStartDelay(0)
                .setDuration(500)
                .alpha(1)
                .translationY(0);
    }


}



/*

In 0 start T:10
In 1 start T:225
In 2 start T:427
In 0 end T:525
Out 0 start T:544
In 3 start T:625
In 1 end T:726
In 2 end T:925
Out 1 start T:945
Out 0 end T:1043
In 3 end T:1125
Out 2 start T:1345
Out 1 end T:1444
Out 3 start T:1747
Out 2 end T:1842
Out 3 end T:2242

0000 v
0050 v
0100 v
0150 v
0200 vv
0250 vv
0300 vv
0350 vv
0400 vvv
0450 vvv
0500 Xvv
0550 ^vv
0600 ^vvv
0650 ^vvv
0700 ^vvv
0750 ^ vv
0800 ^ vv
0850 ^ vv
0900 ^^vv
0950 ^^ v
1000 ^^ v
1050  ^ v
1100  ^ v
1150  ^
1200  ^
1250  ^
1300  ^^
1350  ^^
1400  ^^
1450   ^
1500   ^
1550   ^
1600   ^
1650   ^
1700   ^^
1750   ^^
1800   ^^
1850    ^
1900    ^
1950






*/