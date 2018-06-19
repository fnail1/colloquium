package app.laiki.ui;

public enum ReqCodes {
    BROKEN_VALUE,
    SELECT_COUNTRY,
    IMPORT_DATA,
    CAMERA_CAPTURE_PERMISSIONS,
    ADD_ADDRESS,
    ADD_ADDRESS_AND_BUY,
    BUY_PRODUCT_SINGLE,
    BUY_PRODUCT_GROUP,
    CONFIRM_ORDER,
    LOGIN,
    LOGIN_AND_BUY_SINGLE,
    LOGIN_AND_BUY_GROUP,
    MAKE_PAYMENT,
    ADD_CARD,
    CHOOSE_GROUP,
    CHOOSE_PHOTO,

    WRITE_APP_FEEDBACK,
    WRITE_MAIL_TO_SUPPORT,

    LEAVE_FEEDBACK,
    CONTACTS_PERMISSIONS, STORAGE_PERMISSION;

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
