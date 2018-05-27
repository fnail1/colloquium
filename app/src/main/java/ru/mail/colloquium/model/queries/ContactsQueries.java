package ru.mail.colloquium.model.queries;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.model.entities.PhoneNumber;
import ru.mail.colloquium.model.types.ContactPhoneNumber;
import ru.mail.colloquium.toolkit.data.CursorWrapper;
import ru.mail.colloquium.toolkit.data.DbUtils;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;

public class ContactsQueries extends SQLiteCommands<Contact> {
    public ContactsQueries(SQLiteDatabase db, Logger logger) {
        super(db, Contact.class, logger);
    }

    public CursorWrapper<Contact> selectAb() {
        return new ContactsCursor(db.rawQuery(ContactsCursor.SELECT + "\norder by c.displayNameOrder", null));
    }


    private static class ContactsCursor extends CursorWrapper<Contact> {

        public static final String COLUMNS;
        public static final String TABLES;
        public static final String SELECT;
        public static final String COUNT;

        static {
            StringBuilder sb = new StringBuilder(200);
            sb.append("select ");
            DbUtils.buildComplexColumnNames(Contact.class, "c", sb);
            sb.append(",\n");
            DbUtils.buildComplexColumnNames(PhoneNumber.class, "p", sb);
            sb.append(",\n");
            DbUtils.buildComplexColumnNames(ContactPhoneLink.class, "link", sb);

            COLUMNS = sb.toString();

            TABLES = "from Contacts c\n" +
                    "left join ContactsPhonesLinks link on link.contact = c._id and link.phone=c.displayPhone\n" +
                    "left join PhoneNumbers p on p._id = link.phone\n";

            SELECT = COLUMNS + "\n" + TABLES;
            COUNT = "select count(*) \n" + TABLES;

        }

        private final Field[] contactMap;
        private final Field[] phoneMap;
        private final Field[] linkMap;

        public ContactsCursor(Cursor cursor) {
            super(cursor);
            contactMap = DbUtils.mapCursorForRawType(cursor, Contact.class, "c");
            phoneMap = DbUtils.mapCursorForRawType(cursor, PhoneNumber.class, "p");
            linkMap = DbUtils.mapCursorForRawType(cursor, ContactPhoneLink.class, "link");
        }

        @NonNull
        @Override
        protected Contact get(Cursor cursor) {
            Contact contact = new Contact();
            DbUtils.readObjectFromCursor(cursor, contact, contactMap);
            contact.joinedPhone = new ContactPhoneNumber();
            DbUtils.readObjectFromCursor(cursor, contact.joinedPhone, phoneMap);
            contact.joinedPhone.link = new ContactPhoneLink();
            DbUtils.readObjectFromCursor(cursor, contact.joinedPhone.link, linkMap);

            return contact;
        }
    }
}
