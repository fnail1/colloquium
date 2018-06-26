package app.laiki.diagnostics.statistics;

import android.content.Context;
import android.util.ArrayMap;

import com.flurry.android.FlurryAgent;

import app.laiki.BuildConfig;
import app.laiki.model.types.Choice;

public class Statistics {

    private LoginWorkflow loginWorkflow;
    private QuestionsStatistics questions;
    private AnswersStatistics answers;
    private ProfileStatistics profile;

    public Statistics(Context context) {
        new FlurryAgent.Builder()
                .withLogEnabled(BuildConfig.DEBUG)
                .build(context, "SR7M5GNN8BVW5DYTJZ2N");
    }

    public LoginWorkflow login() {
        if (loginWorkflow == null)
            loginWorkflow = new LoginWorkflow();

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

    public ContactsStatistics contacts() {
        return new ContactsStatistics();
    }

    public ProfileStatistics profile() {
        if (profile == null)
            profile = new ProfileStatistics();

        return profile;
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

        public void stopScreen() {
            FlurryAgent.logEvent("Question.StopScreen");
        }

        public void contacts() {
            FlurryAgent.logEvent("Question.Contacts");
        }
    }

    public class AnswersStatistics {
        public void recieved() {
            FlurryAgent.logEvent("Answer.Recieved");
        }

        public void read() {
            FlurryAgent.logEvent("Answer.Read");
        }

        public void contacts() {
            FlurryAgent.logEvent("Answers.Contacts");
        }
    }

    public class ContactsStatistics {
        public void invite() {
            FlurryAgent.logEvent("Contacts.InviteSent");
        }
    }

    public class ProfileStatistics {
        public void contacts() {
            FlurryAgent.logEvent("Profile.Contacts");
        }

        public void settings() {
            FlurryAgent.logEvent("Profile.Settings");
        }

        public void support() {
            FlurryAgent.logEvent("Profile.Support");
        }

        public void vk() {
            FlurryAgent.logEvent("Profile.Vk");
        }
    }
}
