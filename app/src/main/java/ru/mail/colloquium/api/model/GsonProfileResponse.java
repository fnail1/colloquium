package ru.mail.colloquium.api.model;

import ru.mail.colloquium.model.types.Age;
import ru.mail.colloquium.model.types.Gender;

public class GsonProfileResponse {
    public GsonUser user;

    public class GsonUser {
        public String created_at;
        public String updated_at;
        public String phone;
        public Gender sex;
        public Age education;
    }
}
