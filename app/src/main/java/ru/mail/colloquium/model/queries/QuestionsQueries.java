package ru.mail.colloquium.model.queries;

import android.database.sqlite.SQLiteDatabase;

import ru.mail.colloquium.model.entities.Question;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;

public class QuestionsQueries extends SQLiteCommands<Question>{
    public QuestionsQueries(SQLiteDatabase db, Logger logger) {
        super(db, Question.class, logger);
    }
}
