package app.laiki.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.laiki.R;
import app.laiki.model.entities.Answer;
import app.laiki.ui.base.BaseActivity;
import app.laiki.ui.main.questions.AbsPageViewHolder;
import app.laiki.ui.main.questions.QuestionViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static app.laiki.App.appService;
import static app.laiki.App.data;
import static app.laiki.App.statistics;

public class AnswerActivity extends BaseActivity {
    public static final String EXTRA_ANSWER_ID = "answer_id";

    @BindView(R.id.icon) ImageView icon;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.root) View root;
    @BindView(R.id.back) ImageView back;
    private Answer answer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.bind(this);

        long id = getIntent().getLongExtra(EXTRA_ANSWER_ID, 0);
        answer = data().answers.selectById(id);
        bindData();
    }

    private void bindData() {
        if (!answer.flags.get(Answer.FLAG_READ)) {
            statistics().answers().read();
            appService().answerRead(answer);
        }

        AbsPageViewHolder.ColorScheme colorScheme = QuestionViewHolder.randomColorScheme();
        root.setBackground(colorScheme.background(this));
        icon.setBackground(colorScheme.highlight(this));
        switch (answer.gender) {
            case CAMEL:
                icon.setImageResource(R.drawable.ic_camel);
                break;
            case MALE:
                icon.setImageResource(R.drawable.ic_male);
                break;
            case FEMALE:
                icon.setImageResource(R.drawable.ic_female);
                break;
        }

        author.setText(formatAuthor());

        message.setText(answer.questionText);
    }

    private String formatAuthor() {
        switch (answer.gender) {
            case CAMEL:
                switch (answer.age) {
                    case GRADE6:
                        return "Некто из 6 класса";
                    case GRADE7:
                        return "Некто из 7 класса";
                    case GRADE8:
                        return "Некто из 8 класса";
                    case GRADE9:
                        return "Некто из 9 класса";
                    case GRADE10:
                        return "Некто из 10 класса";
                    case GRADE11:
                        return "Некто из 11 класса";
                    case YEAR1:
                        return "Некто с 1 курса";
                    case YEAR2:
                        return "Некто со 2 курса";
                    case YEAR3:
                        return "Некто с 3 курса";
                    case YEAR4:
                        return "Некто с 4 курса";
                    case YEAR5:
                        return "Некто с 5 курса";
                    case YEAR6:
                        return "Некто с 6 курса";
                    case SUPERSTAR:
                        break;
                }
                break;
            case MALE:
                switch (answer.age) {
                    case GRADE6:
                        return "От парня из 6 класса";
                    case GRADE7:
                        return "От парня из 7 класса";
                    case GRADE8:
                        return "От парня из 8 класса";
                    case GRADE9:
                        return "От парня из 9 класса";
                    case GRADE10:
                        return "От парня из 10 класса";
                    case GRADE11:
                        return "От парня из 11 класса";
                    case YEAR1:
                        return "От парня с 1 курса";
                    case YEAR2:
                        return "От парня со 2 курса";
                    case YEAR3:
                        return "От парня с 3 курса";
                    case YEAR4:
                        return "От парня с 4 курса";
                    case YEAR5:
                        return "От парня с 5 курса";
                    case YEAR6:
                        return "От парня с 6 курса";
                    case SUPERSTAR:
                        break;
                }
                break;
            case FEMALE:
                switch (answer.age) {
                    case GRADE6:
                        return "От девочки из 6 класса";
                    case GRADE7:
                        return "От девочки из 7 класса";
                    case GRADE8:
                        return "От девочки из 8 класса";
                    case GRADE9:
                        return "От девочки из 9 класса";
                    case GRADE10:
                        return "От девочки из 10 класса";
                    case GRADE11:
                        return "От девочки из 11 класса";
                    case YEAR1:
                        return "От девушки с 1 курса";
                    case YEAR2:
                        return "От девушки со 2 курса";
                    case YEAR3:
                        return "От девушки с 3 курса";
                    case YEAR4:
                        return "От девушки с 4 курса";
                    case YEAR5:
                        return "От девушки с 5 курса";
                    case YEAR6:
                        return "От девушки с 6 курса";
                    case SUPERSTAR:
                        break;
                }
                break;
        }
        return "От кого-то, пожелавшего остаться неизвестным";
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
