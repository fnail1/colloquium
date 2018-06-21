package app.laiki.model.queries;

import android.database.sqlite.SQLiteDatabase;

import app.laiki.api.model.GsonAnswers;
import app.laiki.model.entities.Answer;
import app.laiki.toolkit.data.CursorWrapper;
import app.laiki.toolkit.data.DbUtils;
import app.laiki.toolkit.data.SQLiteCommands;
import app.laiki.toolkit.data.SimpleCursorWrapper;

import static app.laiki.toolkit.collections.Query.query;

public class AnswersQueries extends SQLiteCommands<Answer> {
    public AnswersQueries(SQLiteDatabase db, Logger logger) {
        super(db, Answer.class, logger);
    }

    public CursorWrapper<Answer> select(GsonAnswers.GsonAnswer[] src) {
        String sql = selectAll + "\n" +
                "where serverId in (" + query(src).select(s -> s.id).toString() + ")";

        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Answer.class, null);
    }

    public CursorWrapper<Answer> select(int skip, int limit) {
        String sql = selectAll + "\n" +
                "order by (flags & " + Answer.FLAG_READ + ") asc, createdAt desc, serverId desc\n" +
                "limit " + limit + " offset " + skip;

        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Answer.class, null);
    }

    public int countUnread() {
        return DbUtils.count(db, "select count(*) from Answers where flags & " + Answer.FLAG_READ + " = 0", (String[]) null);
    }

    public CursorWrapper<Answer> selectToSync() {
        String sql = selectAll + "\n" +
                "where (flags & " + (Answer.FLAG_READ | Answer.FLAG_SENT) + ") = " + Answer.FLAG_READ + "\n";

        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Answer.class, null);
    }
}
