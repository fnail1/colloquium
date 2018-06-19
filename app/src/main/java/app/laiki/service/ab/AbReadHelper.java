package app.laiki.service.ab;

import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;

import java.util.Iterator;

public class AbReadHelper implements Iterable<SyncDataUnit> {
    private final Cursor abCursor;

    public AbReadHelper(Cursor abCursor) {

        this.abCursor = abCursor;
    }

    @Override
    public Iterator<SyncDataUnit> iterator() {
        return new Iterator<SyncDataUnit>() {
            int columnPhoneId = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
            int columnContactId = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.CONTACT_ID);
            int columnMimeType = abCursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
            int columnPrefix = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX);
            int columnGivenName = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
            int columnFamilyName = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
            int columnMiddleName = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
            int columnDisplayName = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int columnNumber = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int columnNormalizedNumber = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
            int columnLastUpdatedTimestamp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? abCursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP) : -1;
            int columnPhotoUri = abCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
            int columnPhotoThumbUri = abCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
            int columnEventDate = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
            int columnEventType = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE);
            int columnAccountType = abCursor.getColumnIndex(ContactsContract.Settings.ACCOUNT_TYPE);
            int columnPhoneType = abCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            boolean hasNext = abCursor.moveToFirst();

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public SyncDataUnit next() {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < abCursor.getColumnCount(); i++) {
//                    try {
//                        sb.append(abCursor.getString(i)).append("\t");
//                    } catch (Exception e) {
//                    }
//                }
//                logSync(sb.toString());

                SyncDataUnit r = new SyncDataUnit();
                r.phoneId = abCursor.getLong(columnPhoneId);
                r.contactId = abCursor.getLong(columnContactId);
                r.mimeType = abCursor.getString(columnMimeType);
                r.prefix = abCursor.getString(columnPrefix);
                r.givenName = abCursor.getString(columnGivenName);
                r.familyName = abCursor.getString(columnFamilyName);
                r.middleName = abCursor.getString(columnMiddleName);
                r.displayName = abCursor.getString(columnDisplayName);
                r.number = abCursor.getString(columnNumber);
                r.normalizedNumber = abCursor.getString(columnNormalizedNumber);
//                r.normalizedNumber = Utils.normalizePhoneNumber(r.number);
                r.photoUri = abCursor.getString(columnPhotoUri);
                r.photoThumbUri = abCursor.getString(columnPhotoThumbUri);
                r.birthday = abCursor.getInt(columnEventType) == (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) ? abCursor.getString(columnEventDate) : null;
                r.contactLastUpdatedTimestamp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? abCursor.getLong(columnLastUpdatedTimestamp) : 0;
                r.accountType = abCursor.getString(columnAccountType);
                r.isMobile = abCursor.getInt(columnPhoneType) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

//                if (r.normalizedNumber != null && r.normalizedNumber.endsWith("01586")) {
//                    trace(r.number);
//                }
                hasNext = abCursor.moveToNext();

//                logSync("%s", r);
                return r;
            }
        };
    }
}
