package app.laiki.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;


public class TextFormatUtils {

    @Nullable
    public static String normalizePhone(String phone) {
        if (phone == null)
            return null;

        StringBuilder sb = new StringBuilder(phone.length());

        for (int i = 0; i < phone.length(); i++) {
            char ch = phone.charAt(i);
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

    public static String formatPhone(String regionCode, String phone) {
        if (TextUtils.isEmpty(regionCode) || TextUtils.isEmpty(phone))
            return null;
        AsYouTypeFormatter formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode);
        return formatPhone(formatter, phone);
    }

    public static String formatPhone(AsYouTypeFormatter formatter, @NonNull String phone) {
        String formattedPhone = "";
        for (int i = 0; i < phone.length(); i++) {
            char currentChar = phone.charAt(i);
            if (Character.isDigit(currentChar) || currentChar == '+')
                formattedPhone = formatter.inputDigit(currentChar);
        }
        return formattedPhone;
    }

    public static Spanned convertHtml(String htmlSource) {
        if (htmlSource == null)
            htmlSource = "";
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? Html.fromHtml(htmlSource, 0)
                : Html.fromHtml(htmlSource);
    }
}
