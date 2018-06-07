package ru.mail.colloquium.model.types;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import ru.mail.colloquium.R;

public enum Age {
    @SerializedName("GRADE6")
    GRADE6(R.string.age_grade6),
    @SerializedName("GRADE7")
    GRADE7(R.string.age_grade7),
    @SerializedName("GRADE8")
    GRADE8(R.string.age_grade8),
    @SerializedName("GRADE9")
    GRADE9(R.string.age_grade9),
    @SerializedName("GRADE10")
    GRADE10(R.string.age_grade10),
    @SerializedName("GRADE11")
    GRADE11(R.string.age_grade11),
    @SerializedName("COURSE1")
    YEAR1(R.string.age_year1),
    @SerializedName("COURSE2")
    YEAR2(R.string.age_year2),
    @SerializedName("COURSE3")
    YEAR3(R.string.age_year3),
    @SerializedName("COURSE4")
    YEAR4(R.string.age_year4),
    @SerializedName("COURSE5")
    YEAR5(R.string.age_year5),
    @SerializedName("COURSE6")
    YEAR6(R.string.age_year6),
    @SerializedName("")
    SUPERSTAR(R.string.age_superstar);

    private final int resx;

    Age(int resx) {
        this.resx = resx;
    }

    public String localName(Context context) {
        return context.getResources().getString(resx);
    }
}
