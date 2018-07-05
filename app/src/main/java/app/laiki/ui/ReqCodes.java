package app.laiki.ui;

public enum ReqCodes {
    BROKEN_VALUE,
    CONTACTS_PERMISSIONS,
    STORAGE_PERMISSION,
    STOP_SCREEN_OUT;

    public static final int BASE = 2200;
    public static final ReqCodes[] VALUES = values();


    public int code() {
        return BASE + ordinal();
    }

    public static ReqCodes byCode(int requestCode) {
        requestCode -= BASE;
        if (0 <= requestCode && requestCode < VALUES.length)
            return VALUES[requestCode];
        return BROKEN_VALUE;
    }
}
