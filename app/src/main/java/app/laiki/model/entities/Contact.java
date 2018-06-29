package app.laiki.model.entities;

import android.text.TextUtils;
import android.util.SparseIntArray;

import java.util.Locale;

import app.laiki.diagnostics.Logger;
import app.laiki.model.AppData;
import app.laiki.toolkit.Flags32;
import app.laiki.toolkit.data.BaseRow;
import app.laiki.toolkit.data.DbColumn;
import app.laiki.toolkit.data.DbTable;
import app.laiki.toolkit.phonenumbers.PhoneNumberUtils;

@DbTable(name = AppData.TABLE_CONTACTS)
public class Contact extends BaseRow {

    public static final int FLAG_INVITE_REQUESTED = 1;
    public static final int FLAG_INVITE_SENT = 2;

    @DbColumn(unique = true)
    public long abContactId;

    public String displayName;
    public String namePrefix;
    public String firstName;
    public String lastName;
    public String middleName;

    public long abPhoneId;
    public String phone;
    public String serverId;

    public String displayNameOrder;
    public long contactLastUpdatedTimestamp;
    public String avatar;

    public final Flags32 flags = new Flags32();

    public void onUpdateName() {
        if (namePrefix != null) {
            namePrefix = namePrefix.trim();
            if (namePrefix.isEmpty())
                namePrefix = null;
        }

        if (firstName != null) {
            firstName = firstName.trim();
            if (firstName.isEmpty())
                firstName = null;
        }

        if (lastName != null) {
            lastName = lastName.trim();
            if (lastName.isEmpty())
                lastName = null;
        }

        if (middleName != null) {
            middleName = middleName.trim();
            if (middleName.isEmpty())
                middleName = null;
        }


        StringBuilder firstDisplayNameBuilder = new StringBuilder();
        if (namePrefix != null)
            firstDisplayNameBuilder.append(namePrefix);

        if (firstName != null) {
            if (firstDisplayNameBuilder.length() > 0)
                firstDisplayNameBuilder.append(' ');
            firstDisplayNameBuilder.append(firstName);
        }

        if (middleName != null) {
            if (firstDisplayNameBuilder.length() > 0)
                firstDisplayNameBuilder.append(' ');
            firstDisplayNameBuilder.append(middleName);
        }

        if (lastName != null) {
            if (firstDisplayNameBuilder.length() > 0)
                firstDisplayNameBuilder.append(' ');
            firstDisplayNameBuilder.append(lastName);
        }

        if (TextUtils.isEmpty(displayName)) {
            displayName = firstDisplayNameBuilder.toString();

            if (TextUtils.isEmpty(displayName))
                displayName = PhoneNumberUtils.formatPhone(phone, Locale.getDefault().getCountry());
        }

        displayNameOrder = nameOrderKey(displayName);

    }

    public static String nameOrderKey(String s) {
        char[] chars = s.toUpperCase(Locale.getDefault()).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = charOrderKey(chars[i]);
        }
        return new String(chars);
    }

    public static char charOrderKey(char ch) {
        return LETTERS_NATIVE_ORDER[_CHARSET.get(ch, LETTERS_NATIVE_ORDER.length - 1)];
    }

    private static final char[] LETTERS_NATIVE_ORDER = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            'Ё', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И',
            'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т',
            'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь',
            'Э', 'Ю', 'Я',
            'ю', 'я', 'ё'
    };

    private static final char[] LETTERS_ORDER = {
            'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И',
            'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т',
            'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь',
            'Э', 'Ю', 'Я',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final SparseIntArray _CHARSET = buildOrder();

    public static SparseIntArray buildOrder() {
        SparseIntArray array = new SparseIntArray();
        for (int i = 0; i < LETTERS_ORDER.length; i++) {
            char c = LETTERS_ORDER[i];
            array.put(c, i);
        }

        if (Logger.LOG_SYNC) {
            StringBuilder sb = new StringBuilder(LETTERS_ORDER.length * 20);
            for (int i = 0; i < array.size(); i++)
                sb.append("_X(").append(array.keyAt(i)).append(", ").append(array.valueAt(i)).append("), ");

            Logger.logD("LETTERS_ORDER", sb.toString());
        }

        return array;
    }

    public boolean addressBookDataChanged(Contact abData) {
        if (abContactId != abData.abContactId) return true;
        if (contactLastUpdatedTimestamp != abData.contactLastUpdatedTimestamp) return true;
        if (namePrefix != null ? !namePrefix.equals(abData.namePrefix) : abData.namePrefix != null)
            return true;
        if (displayName != null ? !displayName.equals(abData.displayName) : abData.displayName != null)
            return true;
        if (firstName != null ? !firstName.equals(abData.firstName) : abData.firstName != null)
            return true;
        if (lastName != null ? !lastName.equals(abData.lastName) : abData.lastName != null)
            return true;
        if (middleName != null ? !middleName.equals(abData.middleName) : abData.middleName != null)
            return true;
        if (abPhoneId != abData.abPhoneId || !TextUtils.equals(phone, abData.phone))
            return true;

        return false;
    }
}
