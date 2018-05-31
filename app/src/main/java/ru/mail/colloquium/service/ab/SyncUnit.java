package ru.mail.colloquium.service.ab;

import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.toolkit.phonenumbers.PhoneNumberUtils;

public class SyncUnit {

    private static final SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("--MM-dd", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            new SimpleDateFormat("dd MMM yyyy", new Locale("ru")),
            new SimpleDateFormat("dd MMM yyyy", new Locale("uk")),
            new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy-MM-dd", Locale.getDefault()),
            new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
            new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
            new SimpleDateFormat("dd.MM.", Locale.getDefault()),
            new SimpleDateFormat("MMM dd, yyyy", new Locale("ru")),
            new SimpleDateFormat("MMM dd, yyyy", new Locale("en"))
    };

    private boolean isPhoneNumberDirty;

    private static String normalizeNameString(String name) {
        if (name == null)
            return null;
        name = name.trim();
        if (name.isEmpty())
            return null;
        return name;
    }

    private boolean hasName;
    private boolean hasGoogleName;
    public Contact contact = new Contact();

    public SyncUnit(SyncDataUnit unit) {
        merge(unit);
        contact.abContactId = unit.contactId;
    }

    public void merge(SyncDataUnit ab) {
        boolean isGoogle = "com.google".equals(ab.accountType) ||
                "vnd.sec.contact.phone".equals(ab.accountType) ||
                "vnd.sec.contact.sim".equals(ab.accountType);

        switch (ab.mimeType) {
            case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                if (hasGoogleName)
                    break;
                contact.namePrefix = normalizeNameString(ab.prefix);
                contact.firstName = normalizeNameString(ab.givenName);
                contact.middleName = normalizeNameString(ab.middleName);
                contact.displayName = normalizeNameString(ab.displayName);
                contact.lastName = normalizeNameString(ab.familyName);
                hasName = !(contact.firstName == null && contact.lastName == null && contact.displayName == null);
                contact.contactLastUpdatedTimestamp = ab.contactLastUpdatedTimestamp;
                hasGoogleName = hasName && isGoogle;
                break;
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                if (contact.abPhoneId <= 0 || isPhoneNumberDirty) {
                    isPhoneNumberDirty = false;
                    tryPhoneNumber(ab.phoneId, ab.normalizedNumber);
                }
                if (contact.abPhoneId <= 0) {
                    isPhoneNumberDirty = true;
                    tryPhoneNumber(ab.phoneId, ab.number);
                }
                break;
        }
    }

    private void tryPhoneNumber(long phoneId, String n) {
        String number = PhoneNumberUtils.normalizePhoneNumber(n);
        if (number != null) {
            if (number.startsWith("89") && number.length() == 11) {
                contact.phone = "7" + number.substring(1);
                contact.abPhoneId = phoneId;
            } else if (number.startsWith("+79") && number.length() == 12) {
                contact.phone = number.substring(1);
                contact.abPhoneId = phoneId;
            }
        }
    }

}
