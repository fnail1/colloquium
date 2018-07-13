package app.laiki.diagnostics.statistics;

import android.content.Context;
import android.util.ArrayMap;

import com.flurry.android.FlurryAgent;

import app.laiki.BuildConfig;
import app.laiki.model.types.Choice;

import static app.laiki.diagnostics.Logger.logStat;

public class Statistics {

    private LoginWorkflow loginWorkflow;
    private QuestionsStatistics questions;
    private AnswersStatistics answers;
    private ProfileStatistics profile;
    private RateUsStatistics rateUs;

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

    public RateUsStatistics rateUs() {
        if (rateUs == null)
            rateUs = new RateUsStatistics();

        return rateUs;
    }

    public class LoginWorkflow {
        public void start() {
            logEvent("Login.Start");
        }

        public void login() {
            logEvent("Login.PhoneEntered");
        }

        public void auth() {
            logEvent("Login.CodeEntered");
        }

        public void permission() {
            logEvent("Login.PermissionRequested");
        }
    }

    private void logEvent(String event) {
        if (BuildConfig.DEBUG) {
            logStat("logEvent", event, null);
            return;
        }
        FlurryAgent.logEvent(event);
    }

    private void logEvent(String event, ArrayMap<String, String> map) {
        if (BuildConfig.DEBUG) {
            logStat("logEvent", event, map);
            return;
        }
        FlurryAgent.logEvent(event, map);
    }

    public class QuestionsStatistics {

        public void answer(Choice a) {
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("Answer", a.name());
            logEvent("Question.Answered", map);
        }

        public void stopScreen() {
            logEvent("Question.StopScreen");
        }

        public void invite() {
            logEvent("Question.Invite");
        }

        public void shuffle() {
            logEvent("Question.Shuffle");
        }
    }

    public class AnswersStatistics {
        public void recieved() {
            logEvent("Answer.Recieved");
        }

        public void read() {
            logEvent("Answer.Read");
        }
    }

    public class ContactsStatistics {
        public void start(String parent) {
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("Parent", parent);
            logEvent("Contacts.Start", map);

        }

        public void inviteSent() {
            logEvent("Contacts.InviteSent");
        }
    }

    public class ProfileStatistics {
        public void settings() {
            logEvent("Profile.Settings");
        }

        public void support() {
            logEvent("Profile.Support");
        }

        public void vk() {
            logEvent("Profile.Vk");
        }
    }

    public class RateUsStatistics {
        public void start() {
            logEvent("Rating.Request");
        }

        public void answer(Choice a) {
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("Answer", a.name());
            logEvent("Rating.Set", map);
        }

        public void googlePlayStarted() {
            logEvent("Rating.Store");
        }
    }
}
