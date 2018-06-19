package app.laiki.api.model;

import app.laiki.model.types.Age;
import app.laiki.model.types.Choice;
import app.laiki.model.types.Gender;

public class GsonAnswers {
    public GsonAnswer[] answers;

    public class GsonAnswer {
        public String id;
        public String question_id;
        public String variantA;
        public String variantB;
        public String variantC;
        public String variantD;
        public Choice selected_variant;
        public String selected_name;
        public boolean is_viewed;
        public Gender sex;
        public Age user_education;
        public String created_at;

        public GsonQuestionResponse.GsonQuestion question;
    }
}
