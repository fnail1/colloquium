package ru.mail.colloquium.model.queries;

import android.database.sqlite.SQLiteDatabase;

import ru.mail.colloquium.api.model.GsonAnswers;
import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.toolkit.data.CursorWrapper;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;
import ru.mail.colloquium.toolkit.data.SimpleCursorWrapper;

import static ru.mail.colloquium.toolkit.collections.Query.query;

public class AnswersQueries extends SQLiteCommands<Answer> {
    public AnswersQueries(SQLiteDatabase db, Logger logger) {
        super(db, Answer.class, logger);
    }

    public CursorWrapper<Answer> select(GsonAnswers.GsonAnswer[] src) {
        String sql = selectAll + "\n" +
                "where serverId in (" + query(src).select(s -> s.id).toString() + ")";

        return new SimpleCursorWrapper<>(db.rawQuery(sql, null), Answer.class, null);
    }
}
