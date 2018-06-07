package ru.mail.colloquium.model.types;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import ru.mail.colloquium.R;

public enum Gender {
    @SerializedName("x")
    CAMEL(R.string.gender_unknown),
    @SerializedName("M")
    MALE(R.string.male),
    @SerializedName("F")
    FEMALE(R.string.female);

    private final int resx;

    Gender(int resx) {
        this.resx = resx;
    }

    public String localName(Context context) {
        return context.getResources().getString(resx);
    }


}
