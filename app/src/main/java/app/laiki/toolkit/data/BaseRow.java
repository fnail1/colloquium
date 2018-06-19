package app.laiki.toolkit.data;

import java.util.Objects;

import app.laiki.BuildConfig;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseRow)) return false;
        BaseRow baseRow = (BaseRow) o;
        return _id == baseRow._id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(_id);
    }
}
