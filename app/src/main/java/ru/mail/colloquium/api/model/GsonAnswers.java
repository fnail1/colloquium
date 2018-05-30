package ru.mail.colloquium.api.model;

import ru.mail.colloquium.model.types.Gender;

public class GsonAnswers {
    public GsonAnswer[] answers;

    public class GsonAnswer {
        public String id;
        public String question_id;
        public String selected_phone;
        public String all_phones;
        public boolean is_viewed;
        public Gender sex;
        public String created_at;

        public GsonQuestionResponse.GsonQuestion question;
    }
}
