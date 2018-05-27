package ru.mail.colloquium.toolkit.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class SQLiteStatementSimpleBuilder extends ThreadLocal<SQLiteStatement> {

    private final SQLiteDatabase db;
    public final String sql;

    public SQLiteStatementSimpleBuilder(SQLiteDatabase db, String sql) {
        this.db = db;
        this.sql = sql;
    }

    @Override
    protected SQLiteStatement initialValue() {
        return db.compileStatement(sql);
    }
}
