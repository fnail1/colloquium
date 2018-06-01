package ru.mail.colloquium.model.types;

import com.google.gson.annotations.SerializedName;

public enum Gender {
    @SerializedName("x")
    CAMEL,
    @SerializedName("M")
    MALE,
    @SerializedName("F")
    FEMALE;

}
