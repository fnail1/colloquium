package ru.mail.colloquium.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import ru.mail.colloquium.toolkit.phonenumbers.PhoneNumberUtils;

import static ru.mail.colloquium.diagnostics.Logger.trace;

public class PhoneEditListener implements TextWatcher {
    public static final String DEFAULT_COUNTRY_ISO_CODE = "RU";
    public static final String DEFAULT_TELEPHONE_CODE = "+7";
    private final EditText editText;
    private AsYouTypeFormatter formatter;
    private boolean recursiveProtectionLock = false;

    public PhoneEditListener(EditText editText) {
        this.editText = editText;
        formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(DEFAULT_COUNTRY_ISO_CODE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        trace("%s", s);
        if (recursiveProtectionLock)
            return;
        recursiveProtectionLock = true;
        try {
            if (s.length() < 3)
                return;
            formatter.clear();
            String p = PhoneNumberUtils.formatPhone(formatter, DEFAULT_TELEPHONE_CODE + s.toString());
            if (p != null) {
                int pos = savePosition(s);
                int i = DEFAULT_TELEPHONE_CODE.length();
                while (i < p.length() && !Character.isDigit(p.charAt(i)))
                    i++;
                s.replace(0, s.length(), p, i, p.length());
                restorePosition(s, pos);
            }
        } finally {
            recursiveProtectionLock = false;
        }
    }

    private void restorePosition(Editable s, int dpos) {
        int chpos = 0;
        while (dpos > 0)
            if (Character.isDigit(s.charAt(chpos++)))
                dpos--;
        editText.setSelection(chpos);
    }

    private int savePosition(Editable s) {
        int dpos = 0, chpos = 0;
        int selStart = editText.getSelectionStart();
        while (chpos < selStart)
            if (Character.isDigit(s.charAt(chpos++)))
                dpos++;
        return dpos;
    }
}
