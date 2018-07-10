package app.laiki.diagnostics;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.crashlytics.android.core.CrashlyticsListener;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.laiki.BuildConfig;
import app.laiki.R;
import app.laiki.model.AppData;
import app.laiki.toolkit.concurrent.ThreadPool;
import app.laiki.toolkit.data.SQLiteCommands;
import app.laiki.toolkit.data.SQLiteStatementSimpleBuilder;
import app.laiki.toolkit.io.FileUtils;
import app.laiki.ui.ReqCodes;
import app.laiki.ui.base.BaseActivity;
import io.fabric.sdk.android.Fabric;

import static app.laiki.App.data;
import static app.laiki.diagnostics.Logger.logDb;
import static app.laiki.diagnostics.Logger.logV;

public class DebugUtils {
    public static void init(Context context) {
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().listener(new MyCrashlyticsListener()).build())
                .build();

        Fabric fabric = new Fabric.Builder(context)
                .kits(crashlytics)
                .debuggable(true)
                .build();

        Fabric.with(fabric);
        Crashlytics.setBool("Release", !BuildConfig.DEBUG);
        Crashlytics.setBool("SlowDevice", ThreadPool.isSlowDevice());

    }

    public static void safeThrow(Exception e, boolean throwIdDebug) {
        if (BuildConfig.DEBUG && throwIdDebug)
            throw new RuntimeException(e);
        else
            safeThrow(e);
    }

    public static void safeThrow(RuntimeException e, boolean throwIdDebug) {
        if (BuildConfig.DEBUG && throwIdDebug)
            throw e;
        else
            safeThrow(e);
    }

    public static void safeThrow(Throwable e) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
//        FlurryAgent.onError();
        Crashlytics.logException(e);
    }

    public static void testAppData() {
        if (!BuildConfig.DEBUG)
            return;

        Field[] commandsSetFields = AppData.class.getDeclaredFields();
        SQLiteDatabase db;
        try {
            Field fdb = AppData.class.getDeclaredField("db");
            fdb.setAccessible(true);
            db = (SQLiteDatabase) fdb.get(data());
        } catch (NoSuchFieldException e1) {
            throw new RuntimeException(e1);
        } catch (IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
        for (Field commandsSetField : commandsSetFields) {
            if (!SQLiteCommands.class.isAssignableFrom(commandsSetField.getType()) &&
                    !commandsSetField.getType().isAnnotationPresent(TestSQLiteCommand.class))
                continue;

            try {
                Object commandsSet = commandsSetField.get(data());
                Class<?> commandsSetType = commandsSet.getClass();
                while (commandsSetType != Object.class) {
                    Field[] fs = commandsSetType.getDeclaredFields();
                    for (Field f : fs) {
                        if (ThreadLocal.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            Object o = f.get(commandsSet);
                            if (o instanceof SQLiteStatementSimpleBuilder) {

                                SQLiteStatementSimpleBuilder builder = (SQLiteStatementSimpleBuilder) o;
                                logDb(f.getName() + " = \"" + builder.sql + "\"");

                                builder.get();
                            }
                        } else if (f.getType() != String.class)
                            continue;
                        if (!f.isAnnotationPresent(TestSQLiteCommand.class))
                            continue;
                        f.setAccessible(true);
                        String cmd = f.get(commandsSet).toString();
                        logDb(f.getName() + " = \"" + cmd + "\"");
                        db.compileStatement(cmd);
                    }

                    Method[] methods = commandsSetType.getDeclaredMethods();
                    for (Method m : methods) {
                        if (m.getReturnType() == String.class || m.getReturnType() == StringBuilder.class) {
                            if (!m.isAnnotationPresent(TestSQLiteCommand.class))
                                continue;
                            if (m.getParameterTypes().length > 0)
                                throw new RuntimeException("Can't check method " + m.getName());
                            m.setAccessible(true);
                            String cmd = m.invoke(commandsSet).toString();
                            logDb(m.getName() + "() = \"" + cmd + "\"");
                            db.compileStatement(cmd);
                        }
                    }

                    commandsSetType = commandsSetType.getSuperclass();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void importFile(Context context) {
        if (context instanceof BaseActivity) {
            if (!((BaseActivity) context).requestPermissions(ReqCodes.STORAGE_PERMISSION, R.string.storage_permission_explanation, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                return;
        }

        try {
            File dst = saveFile();
            if (dst == null)
                return;

            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", dst);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("application/octet-stream");
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File saveFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), Logger.APP_NAME);
        if (!dir.exists() && !dir.mkdir())
            return null;

        File dst = new File(dir, "data.sqlite");
        int idx = 1;
        while (dst.exists()) {
            dst = new File(dir, "data_" + idx + ".sqlite");
            idx++;
        }
        logV(Logger.LOG_VERBOSE, "IMPORT_DB", dst.getName());
        FileUtils.copyFile(new File(data().getDbPath()), dst);

        return dst;
    }

    private static class MyCrashlyticsListener implements CrashlyticsListener {
        @Override
        public void crashlyticsDidDetectCrashDuringPreviousExecution() {
            Logger.trace();
        }
    }


}
