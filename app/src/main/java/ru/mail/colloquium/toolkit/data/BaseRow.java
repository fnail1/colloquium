package ru.mail.colloquium.toolkit.data;

import ru.mail.colloquium.BuildConfig;

public class BaseRow {
    @DbColumn(primaryKey = true)
    public long _id;

    public static long getCheckedId(BaseRow obj) {
        if (obj == null)
            return 0;
        if (BuildConfig.DEBUG && obj._id <= 0)
            throw new AssertionError(obj.toString());
        return obj._id;
    }
}
