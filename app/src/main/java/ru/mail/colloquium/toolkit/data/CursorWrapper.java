package ru.mail.colloquium.toolkit.data;

import android.database.Cursor;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ru.mail.colloquium.toolkit.collections.Func;
import ru.mail.colloquium.toolkit.collections.IntegerSelector;
import ru.mail.colloquium.toolkit.collections.LongSelector;
import ru.mail.colloquium.toolkit.collections.Query;

public abstract class CursorWrapper<T> extends Query<T> implements Closeable {
    protected final Cursor cursor;

    public CursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public int count() {
        return cursor.getCount();
    }

    @NonNull
    protected abstract T get(Cursor cursor);

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            boolean hasNext = cursor.moveToFirst();

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                try {
                    return get(cursor);
                } finally {
                    hasNext = cursor.moveToNext();
                }
            }
        };
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    protected int approximateCount() {
        return count();
    }

    @Override
    public T first() {
        try {
            return cursor.moveToFirst() ? get(cursor) : null;
        } finally {
            close();
        }
    }

    @NonNull
    @Override
    public List<T> toList() {
        try {
            return super.toList();
        } finally {
            close();
        }
    }

    @NonNull
    @Override
    public <K> HashMap<K, T> toMap(Func<T, K> keySelector) {
        try {
            return super.toMap(keySelector);
        } finally {
            close();
        }
    }


    @NonNull
    @Override
    public SparseArrayCompat<T> toSparseArray(IntegerSelector<T> keySelector) {
        try {
            return super.toSparseArray(keySelector);
        } finally {
            close();
        }
    }

    @NonNull
    @Override
    public LongSparseArray<T> toLongSparseArray(LongSelector<T> keySelector) {
        try {
            return super.toLongSparseArray(keySelector);
        } finally {
            close();
        }
    }

    @NonNull
    @Override
    public LongSparseArray<ArrayList<T>> groupByLong(LongSelector<T> keySelector) {
        try {
            return super.groupByLong(keySelector);
        } finally {
            close();
        }
    }

    @Override
    public SparseArrayCompat<ArrayList<T>> groupByInt(IntegerSelector<T> keySelector) {
        try {
            return super.groupByInt(keySelector);
        } finally {
            close();
        }
    }

    @Override
    public <TKey> HashMap<TKey, ArrayList<T>> groupByObj(Func<T, TKey> keySelector) {
        try {
            return super.groupByObj(keySelector);
        } finally {
            close();
        }
    }

    public T random() {
        try {
            int size = count();
            if (size == 0)
                return null;
            int rnd = (int) ((SystemClock.elapsedRealtime() >> 6) % size);
            cursor.move(rnd);
            return get(cursor);
        } finally {
            cursor.close();
        }
    }

}
