package app.laiki.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.laiki.BuildConfig;
import app.laiki.diagnostics.Logger;

import app.laiki.model.entities.Answer;
import app.laiki.model.entities.Contact;
import app.laiki.model.entities.Question;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.data.DbUtils;

import static app.laiki.diagnostics.Logger.logDb;

public class AppDataSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 3;

    AppDataSQLiteOpenHelper(Context context, String dbName) {
        super(context, dbName, getCursorFactory(), VERSION);
    }

    @Nullable
    private static SQLiteDatabase.CursorFactory getCursorFactory() {
        if (!BuildConfig.DEBUG || !Logger.LOG_DB)
            return null;

        return (db, masterQuery, editTable, query) -> {
            String sql = query.toString();
            logDb(sql);

            SQLiteCursor cursor = new SQLiteCursor(masterQuery, editTable, query);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ThreadPool.SCHEDULER.schedule(() -> {
                    if (!cursor.isClosed() && !Debug.isDebuggerConnected())
                        ThreadPool.UI.post(() -> {
                            throw new RuntimeException("Cursor leak detected! \'" + sql + '\'');
                        });
                }, 30, TimeUnit.SECONDS);
            }

            return cursor;
        };
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        sql = DbUtils.buildCreateScript(Contact.class);
        db.execSQL(sql);

        sql = DbUtils.buildCreateScript(Question.class);
        db.execSQL(sql);

        sql = DbUtils.buildCreateScript(Answer.class);
        db.execSQL(sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("alter table Contacts add column flags integer");
                db.execSQL("update Contacts set flags=(inviteSent * 3)");
                //break;
            case 2:
                db.execSQL("alter table Questions add column shuffles integer");
                //break;
        }
    }


    private void cleanAll(SQLiteDatabase db) {
        for (String obj : getDbObjectNames(db, "index")) {
            //noinspection SpellCheckingInspection
            if (obj.startsWith("sqlite_autoindex_"))
                continue;

            db.execSQL("drop index " + obj);
        }

        for (String obj : getDbObjectNames(db, "view")) {
            db.execSQL("drop view " + obj);
        }

        for (String obj : getDbObjectNames(db, "table")) {
            switch (obj) {
                case "android_metadata":
                case "sqlite_sequence":
                    continue;
            }

            db.execSQL("drop table " + obj);
        }

        onCreate(db);
    }

    @NonNull
    private static List<String> getDbObjectNames(SQLiteDatabase db, String type) {
        String[] args = {type};

        try (Cursor cursor = db.rawQuery("select name\n" +
                "from sqlite_master\n" +
                "where type = ?", args)) {
            if (cursor.moveToFirst()) {
                List<String> objects = new ArrayList<>(cursor.getCount());
                do {
                    objects.add(cursor.getString(0));
                } while (cursor.moveToNext());
                return objects;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
