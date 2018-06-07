package ru.mail.colloquium.service.ab;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.util.Iterator;

public class AbReader implements Iterable<SyncUnit>, Closeable {
    private static final String CONTACT_INFO_SELECTION =
            "(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " NOTNULL) " +
                    "AND (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " != '') " +
                    "AND (" + ContactsContract.CommonDataKinds.GroupMembership.IN_VISIBLE_GROUP + " != 0) " +
                    "AND (" +
                    "(" + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "')" +
                    " OR (" + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "')" +
                    " OR (" + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "')" +
                    ")";

    private static final String CONTACT_INFO_SORT_ORDER =
            ContactsContract.CommonDataKinds.Contactables.CONTACT_ID + " ASC, " +
                    ContactsContract.CommonDataKinds.Phone._ID + " ASC ";

    private static final String[] CONTACT_INFO_COLUMNS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            CONTACT_INFO_COLUMNS = new String[]{
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Contactables.CONTACT_ID,
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.PREFIX,
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Settings.ACCOUNT_TYPE,
            };
        } else {
            CONTACT_INFO_COLUMNS = new String[]{
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Contactables.CONTACT_ID,
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.PREFIX,
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Settings.ACCOUNT_TYPE
            };
        }
    }


    private final Cursor cursor;
    private final AbReadHelper helper;

    public AbReader(Context context) {
        cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, CONTACT_INFO_COLUMNS, CONTACT_INFO_SELECTION, null, CONTACT_INFO_SORT_ORDER);
//        cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, CONTACT_INFO_SELECTION, null, CONTACT_INFO_SORT_ORDER);
        helper = new AbReadHelper(cursor);
    }

    @NonNull
    @Override
    public ABIterator iterator() {
        return new ABIterator(helper.iterator(), cursor.getCount());
    }

    @Override
    public void close() {
        cursor.close();
    }

    public static class ABIterator implements Iterator<SyncUnit> {
        private final Iterator<SyncDataUnit> it;
        private SyncUnit aggr;

        ABIterator(Iterator<SyncDataUnit> it, int count) {
            this.it = it;
            if (it.hasNext()) {
                aggr = new SyncUnit(it.next());
            }
        }

        @Override
        public boolean hasNext() {
            return aggr != null;
        }

        @Override
        public SyncUnit next() {
            SyncUnit unit = aggr;
            SyncDataUnit next = null;

            while (it.hasNext() && (next = it.next()).contactId == unit.contact.abContactId) {
                unit.merge(next);
                next = null;
            }
            aggr = next == null ? null : new SyncUnit(next);

            return unit;
        }

    }
}
