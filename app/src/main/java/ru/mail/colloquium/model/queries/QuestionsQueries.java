package ru.mail.colloquium.model.queries;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.security.PublicKey;

import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.toolkit.data.CursorWrapper;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;
import ru.mail.colloquium.toolkit.data.SimpleCursorWrapper;

import static ru.mail.colloquium.model.entities.Question.FLAG_ANSWERED;
import static ru.mail.colloquium.model.entities.Question.FLAG_SENT;
import static ru.mail.colloquium.toolkit.collections.Query.query;

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
