package app.laiki.model.types;

import com.google.gson.annotations.SerializedName;

import app.laiki.R;

public enum Gender {
    @SerializedName("x")
    CAMEL("x", R.string.gender_unknown, R.drawable.ic_camel, R.drawable.ic_camel_heart),
    @SerializedName("M")
    MALE("M", R.string.male, R.drawable.ic_male, R.drawable.ic_male_heart),
    @SerializedName("F")
    FEMALE("F", R.string.female, R.drawable.ic_female, R.drawable.ic_female_heart);

    public final int nameResId;
    public final int iconResId;
    public final int heartIconResId;
    public final String serverName;

    Gender(String serverName, int nameResId, int iconResId, int heartIconResId) {
        this.serverName = serverName;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.heartIconResId = heartIconResId;
    }


}
