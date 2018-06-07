package ru.mail.colloquium.model.queries;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

import ru.mail.colloquium.model.entities.Answer;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.toolkit.data.CursorWrapper;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;
import ru.mail.colloquium.toolkit.data.SimpleCursorWrapper;

import static ru.mail.colloquium.model.entities.Question.FLAG_ANSWERED;
import static ru.mail.colloquium.model.entities.Question.FLAG_SENT;

public class ContactsQueries extends SQLiteCommands<Contact> {
    public ContactsQueries(SQLiteDatabase db, Logger logger) {
        super(db, Contact.class, logger);
    }

    public CursorWrapper<Contact> selectAb() {
        return new ContactsCursor(db.rawQuery(selectAll + "\norder by displayNameOrder", null));
    }

    public CursorWrapper<Contact> selectRandom(int count) {
        Random random = new Random();
        int m = 0xffff;
        int a = random.nextInt() & m;
        int k = random.nextInt() & m;
        String sql = selectAll + "\n" +
//                "order by (phone = '79991112233') desc, ((" + a + " + _id * " + k + ") & " + m + ")  asc\n" +
                "order by (" + a + " + _id * " + k + ") & " + m + "\n" +
                "limit " + count + " offset 0";

        return new ContactsCursor(db.rawQuery(sql, null));
    }

    public CursorWrapper<Contact> selectById(long... ids) {
        int c = ids.length;
        StringBuilder sb = new StringBuilder(c * 4);
        c--;
        for (int i = 0; ; i++) {
            sb.append(ids[i]);
            if (i >= c) {
                String sql = selectAll + "\n" +
                        "where _id in (" + sb + ")  \n";

                return new ContactsCursor(db.rawQuery(sql, null));
            }
            sb.append(", ");
        }
    }

    public CursorWrapper<Contact> selectQuestionsVariants() {
        String sql = "select c.* \n" +
                "from Contacts c \n" +
                "join Questions q on c._id in (q.variant1,q.variant2,q.variant3,q.variant4)\n" +
                "where q.flags & " + (FLAG_ANSWERED | FLAG_SENT) + " = " + FLAG_ANSWERED + "\n";

        return new ContactsCursor(db.rawQuery(sql, null));
    }

    public CursorWrapper<Contact> select(Answer answer) {
        String sql = "select c.* \n" +
                "from Contacts c \n" +
                "join Answers a on c.serverId in (" +
                "\'" + answer.variantA + "', " +
                "\'" + answer.variantB + "', " +
                "\'" + answer.variantC + "', " +
                "\'" + answer.variantD + "'" +
                ")\n";

        return new ContactsCursor(db.rawQuery(sql, null));
    }


    private static class ContactsCursor extends SimpleCursorWrapper<Contact> {

        public ContactsCursor(Cursor cursor) {
            super(cursor, Contact.class, null);
        }
    }
}
