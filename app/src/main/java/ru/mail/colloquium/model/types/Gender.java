package ru.mail.colloquium.model.types;

import com.google.gson.annotations.SerializedName;

public enum Gender {
    @SerializedName("unknown")
    CAMEL {
        @Override
        public String getName() {
            return null;
        }
    },
    @SerializedName("male")
    MALE {
        @Override
        public String getName() {
            return "male";
        }
    },
    @SerializedName("female")
    FEMALE {
        @Override
        public String getName() {
            return "female";
        }
    };

    public abstract String getName();
}
