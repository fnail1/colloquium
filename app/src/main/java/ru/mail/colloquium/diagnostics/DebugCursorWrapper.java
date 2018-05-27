package ru.mail.colloquium.diagnostics;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import static ru.mail.colloquium.diagnostics.Logger.trace;

public class DebugCursorWrapper implements Cursor {

    private final Cursor cursor;

    public DebugCursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        trace();
        return cursor.getCount();
    }

    @Override
    public int getPosition() {
        trace();
        return cursor.getPosition();
    }

    @Override
    public boolean move(int i) {
        trace();
        return cursor.move(i);
    }

    @Override
    public boolean moveToPosition(int i) {
        trace();
        return cursor.moveToPosition(i);
    }

    @Override
    public boolean moveToFirst() {
        trace();
        new Throwable().printStackTrace();
        return cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        trace();
        return cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        trace();
        return cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        trace();
        return cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        trace();
        return cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        trace();
        return cursor.isLast();
    }

    @Override
    public boolean isBeforeFirst() {
        trace();
        return cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        trace();
        return cursor.isAfterLast();
    }

    @Override
    public int getColumnIndex(String s) {
        trace();
        return cursor.getColumnIndex(s);
    }

    @Override
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        trace();
        return cursor.getColumnIndexOrThrow(s);
    }

    @Override
    public String getColumnName(int i) {
        trace();
        return cursor.getColumnName(i);
    }

    @Override
    public String[] getColumnNames() {
        trace();
        return cursor.getColumnNames();
    }

    @Override
    public int getColumnCount() {
        trace();
        return cursor.getColumnCount();
    }

    @Override
    public byte[] getBlob(int i) {
        trace();
        return cursor.getBlob(i);
    }

    @Override
    public String getString(int i) {
        trace();
        return cursor.getString(i);
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        trace();
        cursor.copyStringToBuffer(i, charArrayBuffer);
    }

    @Override
    public short getShort(int i) {
        trace();
        return cursor.getShort(i);
    }

    @Override
    public int getInt(int i) {
        trace();
        return cursor.getInt(i);
    }

    @Override
    public long getLong(int i) {
        trace();
        return cursor.getLong(i);
    }

    @Override
    public float getFloat(int i) {
        trace();
        return cursor.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        trace();
        return cursor.getDouble(i);
    }

    @Override
    public int getType(int i) {
        trace();
        return cursor.getType(i);
    }

    @Override
    public boolean isNull(int i) {
        trace();
        return cursor.isNull(i);
    }

    @Override
    public void deactivate() {
        trace();
        cursor.deactivate();
    }

    @Override
    public boolean requery() {
        trace();
        return cursor.requery();
    }

    @Override
    public void close() {
        trace();
        cursor.close();
    }

    @Override
    public boolean isClosed() {
        trace();
        return cursor.isClosed();
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {
        trace();
        cursor.registerContentObserver(contentObserver);
    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {
        trace();
        cursor.unregisterContentObserver(contentObserver);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        trace();
        cursor.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        trace();
        cursor.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        trace();
        cursor.setNotificationUri(contentResolver, uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Uri getNotificationUri() {
        trace();
        return cursor.getNotificationUri();
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        trace();
        return cursor.getWantsAllOnMoveCalls();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void setExtras(Bundle bundle) {
        trace();
        cursor.setExtras(bundle);
    }

    @Override
    public Bundle getExtras() {
        trace();
        return cursor.getExtras();
    }

    @Override
    public Bundle respond(Bundle bundle) {
        trace();
        return cursor.respond(bundle);
    }

}
