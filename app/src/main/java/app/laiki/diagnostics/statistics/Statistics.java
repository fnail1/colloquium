package app.laiki.diagnostics.statistics;

import android.content.Context;
import android.util.ArrayMap;

import com.flurry.android.FlurryAgent;

import app.laiki.model.types.Choice;

public class Statistics {

    private LoginWorkflow loginWorkflow;
    private QuestionsStatistics questions;
    private AnswersStatistics answers;

    public Statistics(Context context) {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(context, "SR7M5GNN8BVW5DYTJZ2N");
    }

    public LoginWorkflow login() {
        if (loginWorkflow == null) {
            loginWorkflow = new LoginWorkflow();
        }
        return loginWorkflow;
    }

    public QuestionsStatistics questions() {
        if (questions == null)
            questions = new QuestionsStatistics();
        return questions;
    }

    public AnswersStatistics answers() {
        if (answers == null)
            answers = new AnswersStatistics();

        return answers;
    }

    public class LoginWorkflow {
        public void start() {
            FlurryAgent.logEvent("Login.Start");
        }

        public void login() {
            FlurryAgent.logEvent("Login.PhoneEntered");
        }

        public void auth() {
            FlurryAgent.logEvent("Login.CodeEntered");
        }

        public void permission() {
            FlurryAgent.logEvent("Login.PermissionRequested");
        }
    }

    public class QuestionsStatistics {
        public void answer(Choice a) {
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("Answer", a.name());
            FlurryAgent.logEvent("Question.Answered", map);
        }
    }

    public class AnswersStatistics {
        public void recieved() {
            FlurryAgent.logEvent("Answer.Recieved");
        }

        public void read() {
            FlurryAgent.logEvent("Answer.Read");
        }
    }
}
