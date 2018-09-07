package app.laiki.model.types;

import com.google.gson.annotations.SerializedName;

import app.laiki.R;

public enum Age {
    @SerializedName("GRADE6") GRADE6("GRADE6", R.string.age_grade6, R.string.age_profile_grade6),
    @SerializedName("GRADE7") GRADE7("GRADE7", R.string.age_grade7, R.string.age_profile_grade7),
    @SerializedName("GRADE8") GRADE8("GRADE8", R.string.age_grade8, R.string.age_profile_grade8),
    @SerializedName("GRADE9") GRADE9("GRADE9", R.string.age_grade9, R.string.age_profile_grade9),
    @SerializedName("GRADE10") GRADE10("GRADE10", R.string.age_grade10, R.string.age_profile_grade10),
    @SerializedName("GRADE11") GRADE11("GRADE11", R.string.age_grade11, R.string.age_profile_grade11),
    @SerializedName("COURSE1") YEAR1("COURSE1", R.string.age_year1, R.string.age_profile_year1),
    @SerializedName("COURSE2") YEAR2("COURSE2", R.string.age_year2, R.string.age_profile_year2),
    @SerializedName("COURSE3") YEAR3("COURSE3", R.string.age_year3, R.string.age_profile_year3),
    @SerializedName("COURSE4") YEAR4("COURSE4", R.string.age_year4, R.string.age_profile_year4),
    @SerializedName("COURSE5") YEAR5("COURSE5", R.string.age_year5, R.string.age_profile_year5),
    @SerializedName("COURSE6") YEAR6("COURSE6", R.string.age_year6, R.string.age_profile_year6),
    @SerializedName("") SUPERSTAR("", R.string.age_superstar, R.string.age_profile_superstar);

    public final int nameResId;
    public final int profileNameResId;
    public final String serverName;

    Age(String serverName, int nameResId, int profileNameResId) {
        this.serverName = serverName;
        this.nameResId = nameResId;
        this.profileNameResId = profileNameResId;
    }

}
