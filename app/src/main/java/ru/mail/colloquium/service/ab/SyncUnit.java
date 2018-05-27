package ru.mail.colloquium.service.ab;

import android.provider.ContactsContract;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.model.entities.PhoneNumber;
import ru.mail.colloquium.model.types.ContactPhoneNumber;
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

    private static int lastDateFormatIndex = 0;

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
    public List<ContactPhoneNumber> phones = new ArrayList<>();
    public int birthdayYear;
    public byte birthdayMonth = -1; // local starts with 0
    public byte birthdayDay;

    public SyncUnit(SyncDataUnit unit) {
        merge(unit);
        contact.abContactId = unit.contactId;
    }

    SyncUnit(Contact contact) {
        merge(contact);
    }

    void merge(Contact contact) {
        this.contact = contact;
        phones.add(contact.joinedPhone);
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
                contact.syncTs = ab.contactLastUpdatedTimestamp;
                hasGoogleName = hasName && isGoogle;
                break;
            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                if (contact.avatarUrl == null || isGoogle)
                    contact.avatarUrl = ab.photoUri;
                break;
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                ContactPhoneNumber abPhone = new ContactPhoneNumber();
                abPhone.link = new ContactPhoneLink();
                abPhone.link.abPhoneId = ab.phoneId;
                abPhone.link.origin = ab.number;
                abPhone.normalized = ab.normalizedNumber;

                abPhone.relevance = ab.isMobile ? PhoneNumber.PhoneRelevance.MOBILE : PhoneNumber.PhoneRelevance.UNKNOWN;

                if (TextUtils.isEmpty(abPhone.normalized))
                    abPhone.normalized = PhoneNumberUtils.normalizePhoneNumber(abPhone.link.origin);
                if (abPhone.normalized != null) {
                    if (!hasName && !TextUtils.isEmpty(ab.displayName)) {
                        contact.displayName = ab.displayName;
                        hasName = true;
                    }
                    phones.add(abPhone);
                }
                break;
            case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
                Date birthDate = null;
                String birthdayString = ab.birthday;
                if (birthdayString != null) {
                    birthDate = parseDate(birthdayString);
                }
                if (birthDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(birthDate);
                    birthdayDay = (byte) calendar.get(Calendar.DAY_OF_MONTH);
                    birthdayMonth = (byte) calendar.get(Calendar.MONTH);
                    birthdayYear = calendar.get(Calendar.YEAR);
                    boolean isYearUnknown = "--MM-dd".equals(dateFormats[lastDateFormatIndex].toPattern()) ||
                            "dd.MM.".equals(dateFormats[lastDateFormatIndex].toPattern());
                    if (isYearUnknown || birthdayYear == Calendar.getInstance().get(Calendar.YEAR) || birthdayYear < 1900)
                        birthdayYear = 0;
                }
                break;
        }
    }

    static Date parseDate(String dateString) {
        SimpleDateFormat lastFormat = dateFormats[lastDateFormatIndex];
        Date date = parseDate(dateString, lastFormat);
        if (date == null) {
            for (int i = 0; i < dateFormats.length; i++) {
                if (i != lastDateFormatIndex) {
                    date = parseDate(dateString, dateFormats[i]);
                    if (date != null) {
                        lastDateFormatIndex = i;
                        break;
                    }
                }
            }
        }
        return date;
    }

    private static Date parseDate(String dateString, SimpleDateFormat format) {
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
