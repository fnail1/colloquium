package ru.mail.colloquium.model.queries;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.model.entities.PhoneNumber;
import ru.mail.colloquium.model.types.ContactPhoneNumber;
import ru.mail.colloquium.toolkit.data.CursorWrapper;
import ru.mail.colloquium.toolkit.data.DbUtils;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;

public class PhoneNumbersQueries extends SQLiteCommands<PhoneNumber> {
    public PhoneNumbersQueries(SQLiteDatabase db, Logger logger) {
        super(db, PhoneNumber.class, logger);
    }

    public CursorWrapper<ContactPhoneNumber> selectToSync() {
        return new ContactPhoneNumbersCursor(db.rawQuery(ContactPhoneNumbersCursor.SELECT, null));
    }

    private static class ContactPhoneNumbersCursor extends CursorWrapper<ContactPhoneNumber> {

        public static final String COLUMNS;
        public static final String TABLES;
        public static final String SELECT;
        public static final String COUNT;

        static {
            StringBuilder sb = new StringBuilder(200);
            sb.append("select ");
            DbUtils.buildComplexColumnNames(PhoneNumber.class, "p", sb);
            sb.append(",\n");
            DbUtils.buildComplexColumnNames(ContactPhoneLink.class, "link", sb);

            COLUMNS = sb.toString();

            TABLES = "from PhoneNumbers p\n" +
                    "join ContactsPhonesLinks link on link.phone = p._id";

            SELECT = COLUMNS + "\n" + TABLES;
            COUNT = "select count(*) \n" + TABLES;

        }

        private final Field[] phoneNumberMap;
        private final Field[] linkMap;

        public ContactPhoneNumbersCursor(Cursor cursor) {
            super(cursor);
            phoneNumberMap = DbUtils.mapCursorForRawType(cursor, PhoneNumber.class, "p");
            linkMap = DbUtils.mapCursorForRawType(cursor, ContactPhoneLink.class, "link");

        }

        @NonNull
        @Override
        protected ContactPhoneNumber get(Cursor cursor) {
            ContactPhoneNumber phoneNumber = new ContactPhoneNumber();
            DbUtils.readObjectFromCursor(cursor, phoneNumber, phoneNumberMap);
            phoneNumber.link = new ContactPhoneLink();
            DbUtils.readObjectFromCursor(cursor, phoneNumber.link, linkMap);
            return phoneNumber;
        }
    }
}
