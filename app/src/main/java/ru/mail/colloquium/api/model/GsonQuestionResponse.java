package ru.mail.colloquium.api.model;

public class GsonQuestionResponse {
    public GsonQuestion question;

    public class GsonQuestion {
        public String id;
        public String emoji;
        public String question;
        public String created_at;
        public String updated_at;
    }
}