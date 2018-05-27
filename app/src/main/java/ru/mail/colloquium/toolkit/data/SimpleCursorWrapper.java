package ru.mail.colloquium.toolkit.data;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

public class SimpleCursorWrapper<T> extends CursorWrapper<T> {
    private final Class<T> rowType;
    private final Field[] map;

    public SimpleCursorWrapper(Cursor cursor, Class<T> rowType, String tableAlias) {
        super(cursor);
        this.rowType = rowType;
        map = DbUtils.mapCursorForRawType(cursor, rowType, tableAlias);
    }

    @NonNull
    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    protected T get(Cursor cursor) {
        try {
            return DbUtils.readObjectFromCursor(cursor, rowType.newInstance(), map);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
