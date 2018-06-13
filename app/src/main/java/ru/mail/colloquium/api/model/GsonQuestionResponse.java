package ru.mail.colloquium.api.model;

public class GsonQuestionResponse {
    public GsonQuestion question;
    public int question_cycle;

    public class GsonQuestion {
        public String id;
        public String url;
        public String alt;
        public String question;
        public String created_at;
        public String updated_at;
    }
}
