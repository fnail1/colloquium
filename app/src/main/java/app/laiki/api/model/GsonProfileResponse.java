package app.laiki.api.model;

import app.laiki.model.types.Age;
import app.laiki.model.types.Gender;

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
