package app.laiki.toolkit.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import app.laiki.BuildConfig;
import app.laiki.toolkit.concurrent.ThreadPool;


@SuppressWarnings("WeakerAccess")
public abstract class SQLiteCommands<T extends BaseRow> {

    private final ThreadLocal<SQLiteStatement> insert;
    private final ThreadLocal<SQLiteStatement> update;
    private final ThreadLocal<SQLiteStatement> delete;
    protected final String selectAll;
    private final String selectById;

    protected final SQLiteDatabase db;
    protected final Logger logger;
    public final String tableName;
    protected final Class<T> rawType;

    protected final String rawTypeSimpleName;

    public SQLiteCommands(SQLiteDatabase db, Class<T> rawType, Logger logger) {
        this.db = db;
        this.rawType = rawType;
        this.logger = logger;
        insert = new SQLiteStatementSimpleBuilder(db, DbUtils.buildInsert(rawType, ConflictAction.IGNORE));
        update = new SQLiteStatementSimpleBuilder(db, DbUtils.buildUpdate(rawType, ConflictAction.IGNORE));
        delete = new SQLiteStatementSimpleBuilder(db, DbUtils.buildDelete(rawType));
        selectAll = DbUtils.buildSelectAll(rawType);
        selectById = DbUtils.buildSelectById(rawType);
        rawTypeSimpleName = logger.enabled() ? rawType.getSimpleName() : "";
        tableName = DbUtils.getTableName(this.rawType);
    }

    public long insert(@NonNull T raw) {
        SQLiteStatement statement = insert.get();
        String[] args = DbUtils.buildInsertArgs(raw);
        DbUtils.bindAllArgsAsStrings(statement, args);
        long r = statement.executeInsert();
        logger.logDb("INSERT %s %s returns %d", rawTypeSimpleName, raw, r);
        return r;
    }

    public int update(@NonNull T raw) {
        SQLiteStatement statement = update.get();
        String[] args = DbUtils.buildUpdateArgs(raw);
        DbUtils.bindAllArgsAsStrings(statement, args);
        int r = statement.executeUpdateDelete();
        logger.logDb("UPDATE %s %s returns %d", rawTypeSimpleName, raw, r);
        return r;
    }

    public int delete(long id) {
        SQLiteStatement statement = delete.get();
        statement.bindLong(1, id);
        int r = statement.executeUpdateDelete();
        logger.logDb("DELETE %s %d returns %d", rawTypeSimpleName, id, r);
        return r;
    }

    public int delete(T row) {
        return delete(row._id);
    }

    public T selectById(long id) {
        String[] args = {String.valueOf(id)};
        return DbUtils.readSingle(db, rawType, selectById, args);
    }

    @NonNull
    public CursorWrapper<T> selectAll() {
        logger.logDb(selectAll);

        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(selectAll, null);
        return new SimpleCursorWrapper<>(cursor, rawType, null);
    }

    @NonNull
    public final CursorWrapper<T> select(@NonNull String sql, @Nullable String... args) {
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(sql, args);
        return new SimpleCursorWrapper<>(cursor, rawType, null);
    }

    public long save(@NonNull T obj) {
        if (BuildConfig.DEBUG && ThreadPool.isUiThread())
            throw new IllegalStateException("Do not lock UI-thread!");

        if (obj._id == 0) {
            return obj._id = insert(obj);
        } else {
            return update(obj) == 1 ? obj._id : 0;
        }
    }

    public void deleteAll() {
        logger.logDb("delete from %s", tableName);
        db.delete(tableName, null, null);
    }

    public long count() {
        return DbUtils.count(db, tableName);
    }

    public interface Logger {
        boolean enabled();

        void logDb(String message);

        void logDb(String message, Object arg);

        void logDb(String message, Object... args);

        void safeThrow(Exception e);
    }
}
