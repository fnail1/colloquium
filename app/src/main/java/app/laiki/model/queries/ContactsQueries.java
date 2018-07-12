package app.laiki.model.queries;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

import app.laiki.BuildConfig;
import app.laiki.model.entities.Answer;
import app.laiki.model.entities.Contact;
import app.laiki.toolkit.data.CursorWrapper;
import app.laiki.toolkit.data.DbUtils;
import app.laiki.toolkit.data.SQLiteCommands;
import app.laiki.toolkit.data.SimpleCursorWrapper;

import static app.laiki.App.prefs;
import static app.laiki.model.entities.Contact.FLAG_INVITE_REQUESTED;
import static app.laiki.model.entities.Contact.FLAG_INVITE_SENT;
import static app.laiki.model.entities.Question.FLAG_ANSWERED;
import static app.laiki.model.entities.Question.FLAG_SENT;

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
        String orderBy;
//        if (BuildConfig.DEBUG) {
//            switch (prefs().getApiSet()) {
//                case PROD:
//                    orderBy = "(phone like '" + prefs().profile().phone + "') desc, ((" + a + " + _id * " + k + ") & " + m + ")  asc\n";
//                    break;
//                default:
//                    orderBy = "(phone like '7999111223%') desc, ((" + a + " + _id * " + k + ") & " + m + ")  asc\n";
//                    break;
//            }
//        } else {
            orderBy = " ((" + a + " + _id * " + k + ") & " + m + ")  asc\n";
//        }
        String sql = selectAll + "\n" +
                "order by " + orderBy + "" +
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

    public int countSentInvites() {
        return DbUtils.count(db, "select count(*) from Contacts where flags & " + FLAG_INVITE_REQUESTED + " != 0 ", (String[]) null);
    }

    public CursorWrapper<Contact> selectInviteVariants(int skip, int limit) {
        String sql = "select c.*, count(q._id) q\n" +
                "from Contacts c\n" +
                "left join Questions q on \n" +
                "    (q.answer=0 and q.variant1=c._id) or\n" +
                "    (q.answer=1 and q.variant2=c._id) or\n" +
                "    (q.answer=2 and q.variant3=c._id) or\n" +
                "    (q.answer=3 and q.variant4=c._id) \n" +
                "where c.flags & " + (FLAG_INVITE_REQUESTED | FLAG_INVITE_SENT) + " = 0\n" +
                "group by c._id\n" +
                "order by q desc\n" +
                "limit " + limit + " offset " + skip;
        return new ContactsCursor(db.rawQuery(sql, null));
    }

    public CursorWrapper<Contact> selectPendingInvites() {
        String sql = "select c.* \n" +
                "from Contacts c \n" +
                "where c.flags & " + (FLAG_INVITE_REQUESTED | FLAG_INVITE_SENT) + " = " + FLAG_INVITE_REQUESTED + "\n";

        return new ContactsCursor(db.rawQuery(sql, null));
    }


    private static class ContactsCursor extends SimpleCursorWrapper<Contact> {

        public ContactsCursor(Cursor cursor) {
            super(cursor, Contact.class, null);
        }
    }
}
