package app.laiki.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import app.laiki.App;
import app.laiki.diagnostics.Logger;
import app.laiki.model.queries.AnswersQueries;
import app.laiki.model.queries.ContactsQueries;
import app.laiki.model.queries.QuestionsQueries;
import app.laiki.toolkit.data.SQLiteCommands;
import app.laiki.toolkit.io.FileUtils;

import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.logDb;


@SuppressWarnings("WeakerAccess")
public class AppData {
    public static final String ANONYMOUS = "anonymous";

    public static final String TABLE_QUESTIONS = "Questions";
    public static final String TABLE_CONTACTS = "Contacts";
    public static final String TABLE_PHONE_NUMBERS = "PhoneNumbers";
    public static final String TABLE_CONTACTS_PHONES_LINKS = "ContactsPhonesLinks";
    public static final String TABLE_ANSWERS = "Answers";

    public static String normalizeDbName(String userId) {
        if (TextUtils.isEmpty(userId))
            return ANONYMOUS;

        if (ANONYMOUS.equals(userId))
            throw new RuntimeException("WTF?");

        try {
            return URLEncoder.encode(userId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return String.valueOf(userId.hashCode());
        }
    }

    public final SQLiteDatabase db;

    public final QuestionsQueries questions;
    public final AnswersQueries answers;
    public final ContactsQueries contacts;

    public AppData(Context context, String userId) {
        SQLiteOpenHelper helper = new AppDataSQLiteOpenHelper(context, normalizeDbName(userId));
        db = helper.getWritableDatabase();

        SQLiteCommands.Logger logger = Logger.createDbLogger();

        questions = new QuestionsQueries(db, logger);
        answers = new AnswersQueries(db, logger);
        contacts = new ContactsQueries(db, logger);
    }

    public String getDbPath() {
        return db.getPath();
    }

    public void close() {
        db.close();
    }

    public Transaction beginTransaction() {
        return new Transaction();
    }

    public void runInTx(Runnable runnable) {
        try (Transaction transaction = beginTransaction()) {
            runnable.run();
            transaction.commit();
        }
    }

    public boolean cloneForNewUser(App context, String userId) {
        String dbPath = getDbPath();
        if (!dbPath.endsWith(ANONYMOUS)) {
            safeThrow(new IllegalStateException("attempt to clone non anonymous database"));
            return false;
        }

        File dstPath = context.getDatabasePath(normalizeDbName(userId));

        return !dstPath.exists() && FileUtils.copyFile(new File(dbPath), dstPath);
    }


    @SuppressWarnings("UnusedReturnValue")
    public boolean copyFromAnonimous(Context context) {
        try {
            db.execSQL("attach \'" + context.getDatabasePath(ANONYMOUS).getAbsolutePath() + "\' as src");
            try {

                return true;
            } catch (Exception e) {
                safeThrow(e);
                return false;
            } finally {
                db.execSQL("detach database src");
            }
        } catch (Exception e) {
            safeThrow(e);
            return false;
        }
    }

    private static final AtomicInteger txCounter = new AtomicInteger();

    public boolean isOpen() {
        return db.isOpen();
    }

    public class Transaction implements Closeable {

        private final int id;

        Transaction() {
            id = txCounter.getAndIncrement();
            db.beginTransaction();
            logDb("TX begin %d", id);
        }

        public void commit() {
            logDb("TX commit %d", id);
            db.setTransactionSuccessful();
        }

        @Override
        public void close() {
//            new Throwable().printStackTrace();
            logDb("TX end %d", id);
            db.endTransaction();
        }
    }
}
