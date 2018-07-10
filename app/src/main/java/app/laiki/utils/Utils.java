package app.laiki.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.annotations.SerializedName;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import app.laiki.R;

import static app.laiki.diagnostics.DebugUtils.safeThrow;
import static app.laiki.diagnostics.Logger.trace;


public final class Utils {

    private Utils() {
    }

    public static void startShareIntent(Activity activity, String shareText) {
        startShareIntent(activity, null, shareText);
    }

    public static void startShareIntent(Activity activity, @Nullable String destinationAppPackageName, String shareText) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        if (destinationAppPackageName != null) {
            intent.setPackage(destinationAppPackageName);
            try {
                activity.startActivity(intent);
                return;
            } catch (ActivityNotFoundException e) {
                PackageManager pm = activity.getPackageManager();
                Intent openAppIntent = pm.getLaunchIntentForPackage(destinationAppPackageName);
                if (openAppIntent != null) {
                    openAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(openAppIntent);
                    return;
                }
            }
            startShareIntent(activity, null, shareText);
        } else {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));
        }
    }

    public static void hideKeyboard(View view) {
        if (view == null)
            return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity a) {
        InputMethodManager inputManager = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = a.getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static void closeCloseable(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            //
        } catch (IncompatibleClassChangeError e) {
            try {
                closeable.getClass().getMethod("close").invoke(closeable);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e2) {
                throw new RuntimeException(e2);
            } catch (InvocationTargetException e2) {
                //
            }
        }
    }

    /**
     * See http://stackoverflow.com/a/17625641
     *
     * @param context context
     * @return unique device id
     */
    public static String getDeviceId(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(android_id))
            android_id = getUniquePsuedoID();

        return android_id;
    }

    /**
     * Return pseudo unique ID
     *
     * @return ID
     */
    public static String getUniquePsuedoID() {
        // If ALL else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Arrays.hashCode(Build.SUPPORTED_ABIS) % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

        // Thanks to @Roman SL!
        // http://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        String serial = null;
        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "2f8ab2c67788e3d7"; // some value
        }

        // Thanks @Joe!
        // http://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    public static int getColor(Context context, @ColorRes int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorResId);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(colorResId);
        }
    }

    /**
     * Converts DIP units into pixels
     *
     * @param context The reference to a context to take display metrics from
     * @param dp      Size in DIP units
     * @return Size in pixels
     */
    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Converts SP units into pixels
     *
     * @param context The reference to a context to take display metrics from
     * @param sp      Size in SP units
     * @return Size in pixels
     */
    public static float spToPx(Context context, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    @Nullable
    public static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static String md5(String s) {
        return toHexString(md5(s.getBytes()));
    }

    public static byte[] md5(byte[] bytes) {
        MessageDigest md5 = getMd5();
        md5.update(bytes);
        return md5.digest();

    }

    private static WeakReference<MessageDigest> md5ref;

    private static MessageDigest getMd5() {
        if (md5ref != null) {
            MessageDigest md5 = md5ref.get();
            if (md5 != null)
                return md5;
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5ref = new WeakReference<>(md5);
            return md5;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    public static String toHexString(byte[] hash) {
//        StringBuilder sb = new StringBuilder(hash.length * 2);
        char[] buf = new char[hash.length << 1];
        for (int i = 0, hashLength = hash.length; i < hashLength; i++) {
            byte b = hash[i];
            int idx = i << 1;
            buf[idx] = DIGITS[(b >> 4) & 0xf];
            buf[idx + 1] = DIGITS[b & 0xf];

        }

        return new String(buf);
    }

    public static void startGooglePlay(@NonNull Context context) {

        Uri storeUri = Uri.parse("market://details?id=app.laiki");
        Intent goToStoreIntent = new Intent(Intent.ACTION_VIEW, storeUri);
        goToStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (goToStoreIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(goToStoreIntent);
        else
            safeThrow(new Throwable("Can not open Google Play"));

    }
}
