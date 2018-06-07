package ru.mail.colloquium.api.model;

import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.model.types.Choice;
import ru.mail.colloquium.model.types.Gender;

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
        public boolean is_viewed;
        public Gender sex;
        public Age user_education;
        public String created_at;

        public GsonQuestionResponse.GsonQuestion question;
    }
}
