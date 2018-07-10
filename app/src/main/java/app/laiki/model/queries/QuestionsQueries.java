package app.laiki.model.queries;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import app.laiki.api.model.GsonAnswers;
import app.laiki.model.entities.Question;
import app.laiki.toolkit.data.CursorWrapper;
import app.laiki.toolkit.data.SQLiteCommands;
import app.laiki.toolkit.data.SimpleCursorWrapper;

import static app.laiki.model.entities.Question.FLAG_ANSWERED;
import static app.laiki.model.entities.Question.FLAG_SENT;
import static app.laiki.toolkit.collections.Query.query;

public class QuestionsQueries extends SQLiteCommands<Question> {
    public QuestionsQueries(SQLiteDatabase db, Logger logger) {
        super(db, Question.class, logger);
    }

    public CursorWrapper<Question> select(GsonAnswers.GsonAnswer[] src) {
        String sql = selectAll + "\n" +
                "where serverId in (" + query(src).select(a -> a.question_id) + ")";
        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Question.class, null);
    }

    public CursorWrapper<Question> selectToSend() {
        String sql = selectAll + "\n" +
                "where flags & " + (FLAG_ANSWERED | FLAG_SENT) + " = " + FLAG_ANSWERED + "\n";
        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Question.class, null);
    }

    @Nullable
    public Question selectCurrent() {
        String sql = selectAll + "\n" +
                "where flags & " + Question.FLAG_ANSWERED + " = 0 \n" +
                "order by _id desc\n" +
                "limit 1 offset 0";
        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Question.class, null).first();
    }

}
