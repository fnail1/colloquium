package ru.mail.colloquium.diagnostics;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Patterns;

import static ru.mail.colloquium.App.app;


public class Utils {

    private static String externalUserId;

    static String getExternalUserId() {

        if (externalUserId != null)
            return externalUserId;

        if (ActivityCompat.checkSelfPermission(app(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
//            if (prefs().getProfileContact().phoneNumber().normalized == null)
//                return "";
//            return externalUserId = encodePhoneNumber(prefs().getProfileContact().phoneNumber().normalized);
            return "";
        }

        Account[] accounts = AccountManager.get(app()).getAccounts();
        String google = null;
        String pattern = null;
        String checkBoth = null;
        for (Account account : accounts) {
            boolean checkGoogle = "com.google".equals(account.type);
            boolean checkPattern = Patterns.EMAIL_ADDRESS.matcher(account.name).matches();
            if (checkGoogle && checkPattern) {
                checkBoth = account.name;
                break;
            }
            if (checkGoogle)
                google = account.name;
            if (checkPattern)
                pattern = account.name;

        }

        if (!TextUtils.isEmpty(checkBoth))
            externalUserId = checkBoth;
        else if (!TextUtils.isEmpty(pattern))
            externalUserId = pattern;
        else if (!TextUtils.isEmpty(google))
            externalUserId = google;
        else
            externalUserId = "";//encodePhoneNumber(prefs().getProfileContact().phoneNumber().normalized);

        return externalUserId;
    }

    private static String encodePhoneNumber(String normalizedNumber) {
        if (normalizedNumber == null)
            return null;
        char[] chars = normalizedNumber.toCharArray();
        int r = 1;
        long num = 0;
        for (char c : chars) {
            if (Character.isDigit(c)) {
                num += r * (c - '0');
                r *= 10;
            }
        }

        for (int i = 0; i < 17; i++)
            num = (num * num) % 0xF12540BE400L;

        return Long.toString(num, Character.MAX_RADIX) + "@noemail";
    }
}
