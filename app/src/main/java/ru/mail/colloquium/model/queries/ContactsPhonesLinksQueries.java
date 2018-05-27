package ru.mail.colloquium.model.queries;

import android.database.sqlite.SQLiteDatabase;

import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;

public class ContactsPhonesLinksQueries extends SQLiteCommands<ContactPhoneLink> {
    public ContactsPhonesLinksQueries(SQLiteDatabase db, Logger logger) {
        super(db, ContactPhoneLink.class, logger);
    }
}
