package ru.mail.colloquium.model.types;

import com.google.gson.annotations.SerializedName;

import ru.mail.colloquium.R;

public enum Gender {
    @SerializedName("x")
    CAMEL(R.string.gender_unknown, R.drawable.ic_camel, R.drawable.ic_camel_heart),
    @SerializedName("M")
    MALE(R.string.male, R.drawable.ic_male, R.drawable.ic_male_heart),
    @SerializedName("F")
    FEMALE(R.string.female, R.drawable.ic_female, R.drawable.ic_female_heart);

    public final int nameResId;
    public final int iconResId;
    public final int heartIconResId;

    Gender(int nameResId, int iconResId, int heartIconResId) {
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.heartIconResId = heartIconResId;
    }


}
