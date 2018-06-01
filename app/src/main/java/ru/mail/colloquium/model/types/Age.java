package ru.mail.colloquium.model.types;

import com.google.gson.annotations.SerializedName;

public enum Age {
    @SerializedName("GRADE6")
    GRADE6,
    @SerializedName("GRADE7")
    GRADE7,
    @SerializedName("GRADE8")
    GRADE8,
    @SerializedName("GRADE9")
    GRADE9,
    @SerializedName("GRADE10")
    GRADE10,
    @SerializedName("GRADE11")
    GRADE11,
    @SerializedName("COURSE1")
    YEAR1,
    @SerializedName("COURSE2")
    YEAR2,
    @SerializedName("COURSE3")
    YEAR3,
    @SerializedName("COURSE4")
    YEAR4,
    @SerializedName("COURSE5")
    YEAR5,
    @SerializedName("COURSE6")
    YEAR6,
    @SerializedName("")
    SUPERSTAR,
}
