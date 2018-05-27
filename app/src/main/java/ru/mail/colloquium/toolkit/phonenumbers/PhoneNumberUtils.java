package ru.mail.colloquium.toolkit.phonenumbers;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class PhoneNumberUtils {
    public static String normalizePhoneNumber(String number) {
        if (number == null)
            return null;
        int length = number.length();
        int i = 0;

        if (number.charAt(0) == '+' || Character.isDigit(number.charAt(0))) {
            i++;
            while (i < length) {
                char ch = number.charAt(i);
                if (!Character.isDigit(ch))
                    break;
                i++;
            }
            if (i == length)
                return number;
        }

        StringBuilder sb = new StringBuilder(length);
        sb.append(number, 0, i);

        for (; i < length; i++) {
            char ch = number.charAt(i);
            if (Character.isDigit(ch)) {
                sb.append(ch);
            } else if (ch == '+') {
                if (sb.length() > 0)
                    return null;
                else
                    sb.append("+");
            } else if (ch == '*' || ch == '#')
                return null;
        }

        return sb.length() >= 5 ? sb.toString() : null;
    }

    public static String formatPhone(String numberToParse) {
        return formatPhone(numberToParse, Locale.getDefault().getCountry());
    }

    public static String formatPhone(String numberToParse, String countryIso) {
        try {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber pn = util.parse(numberToParse, countryIso.toUpperCase());
            return util.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return numberToParse;
        }
    }

    @Nullable
    public static String digitsOnly(String number) {
        if (TextUtils.isEmpty(number))
            return null;


        int i;
        int length = number.length();

        for (i = 0; i < length; i++) {
            char ch = number.charAt(i);
            if (!Character.isDigit(ch))
                break;
        }
        if (i == length)
            return number;

        StringBuilder sb = null;

        if (i > 0) {
            sb = new StringBuilder(length);
            sb.append(number, 0, i);
        } else {
            for (; i < length; i++) {
                char ch = number.charAt(i);
                if (Character.isLetter(ch))
                    return null;

                if (Character.isDigit(ch)) {
                    sb = new StringBuilder(length - i);
                    break;
                }
            }

            if (i == length)
                return number;
        }


        for (; i < length; i++) {
            char ch = number.charAt(i);
            if (Character.isLetter(ch))
                return null;

            if (Character.isDigit(ch)) {
                //noinspection ConstantConditions
                sb.append(ch);
            }
        }

        return sb == null ? null : sb.toString();
    }
}
