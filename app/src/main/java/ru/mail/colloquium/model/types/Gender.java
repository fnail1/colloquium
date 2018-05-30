package ru.mail.colloquium.model.types;

import com.google.gson.annotations.SerializedName;

public enum Gender {
    @SerializedName("x")
    CAMEL,
    @SerializedName("m")
    MALE,
    @SerializedName("f")
    FEMALE;

}
