package ru.mail.colloquium.diagnostics;

import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.logging.HttpLoggingInterceptor;
import ru.mail.colloquium.BuildConfig;
import ru.mail.colloquium.diagnostics.statistics.StatParam;
import ru.mail.colloquium.toolkit.data.SQLiteCommands;
import ru.mail.colloquium.toolkit.io.FileUtils;

import static ru.mail.colloquium.App.app;

@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public class Logger {
    public static final String APP_NAME = BuildConfig.APPLICATION_ID;

    public static final boolean LOG_ERRORS = true;
    public static final boolean LOG_VERBOSE = BuildConfig.DEBUG;
    public static final boolean LOG_DEBUG = LOG_VERBOSE;
    public static final boolean LOG_TRACE = LOG_VERBOSE;
    public static final boolean LOG_API = LOG_VERBOSE;
    public static final boolean LOG_FCM = LOG_VERBOSE;
    public static final boolean LOG_STAT = LOG_VERBOSE;
    public static final boolean LOG_DB = false;
    public static final boolean LOG_MEM = false;
    public static final boolean LOG_GLIDE = false;
    public static final boolean LOG_SYNC = false;

    private static final boolean SAVE_LOGS = BuildConfig.DEBUG;

    public static final String TAG_API = "API";
    public static final String TAG_FCM = "FCM";
    public static final String TAG_GLIDE = "GLIDE";
    public static final String TAG_STAT = "STATISTICS";
    public static final String TAG_DB = "DATABASE";
    public static final String TAG_TRACE = "TRACE";
    public static final String TAG_SYNC = "SYNC";


    public static final SimpleDateFormat formatLogTs;
    private static final LinkedBlockingQueue<String> logQueue;
    private static final AtomicInteger skippedMessages;
    private static final AtomicInteger line;
    public static final File logsDir;
    public static final File logFile;
    private static final Thread logThread;
    private static long lastMemoryValue;


    static {
        if (BuildConfig.DEBUG && SAVE_LOGS && LOG_VERBOSE) {
            formatLogTs = new SimpleDateFormat("dd HH:mm:ss.SSS", Locale.getDefault());
            logQueue = new LinkedBlockingQueue<>(1000);
            skippedMessages = new AtomicInteger(0);
            line = new AtomicInteger();
            logThread = new Thread(Logger::fileLoggerThread);
            logsDir = new File(Environment.getExternalStorageDirectory(), APP_NAME);
            if (logsDir.exists() || logsDir.mkdir()) {
                logFile = new File(logsDir, "log.txt");
            } else {
                logFile = null;
            }
        } else {
            formatLogTs = null;
            logQueue = null;
            skippedMessages = null;
            line = null;
            logThread = null;
            logsDir = null;
            logFile = null;
        }
    }

    public static void fileLoggerThread() {
        FileOutputStream fs;
        try {
            if (logFile.exists() && logFile.length() > 2 * 1024 * 1024) {
                int idx = 1;
                File archive;
                do {
                    archive = new File(logsDir, "log_" + idx + "_" + new Date() + ".zip");
                    idx++;
                } while (archive.exists());
                FileUtils.zip(logFile, archive);

                //noinspection ResultOfMethodCallIgnored
                logFile.delete();
            }
            fs = new FileOutputStream(logFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fs);
        OutputStreamWriter writer = new OutputStreamWriter(bufferedOutputStream);

        while (true) {
            try {
                String line = logQueue.take();
                writer.write(line);
//                    try {
//                        fs.getFD().sync();
//                    } catch (IOException ignored) {
//                    }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (IllegalMonitorStateException e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean saveLogs() {
        return logFile != null;
    }

    private static void putLogToFile(String tag, String message) {
        if (!saveLogs())
            return;

        if (logQueue.remainingCapacity() < 2) {
            skippedMessages.getAndIncrement();
            return;
        }
        try {
            if (skippedMessages.getAndSet(0) > 0)
                logQueue.put("!!!!!skipped " + skippedMessages.get() + " messages \n");

            try {
                logQueue.put("" + line.incrementAndGet() + " (" + Process.myPid() + ") [" + formatLogTs.format(new Date()) + "]: " + tag + ": " + message + "\n");
            } catch (OutOfMemoryError e) {
                logQueue.put(e.getMessage());
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (logThread) {
            if (logThread.getState() == Thread.State.NEW && app() != null)
                logThread.start();
        }

    }

    public static void logE(String tag, String s, Object... args) {
        if (LOG_ERRORS) {
            if (args.length > 0)
                s = String.format(Locale.US, s, (Object[]) args);
            Log.e(tag, s);
            putLogToFile(tag, s);
        }
    }

    public static void logW(String tag, String s, Object... args) {
        if (LOG_DEBUG) {
            if (args.length > 0)
                s = String.format(Locale.US, s, (Object[]) args);
            Log.w(tag, s);
            putLogToFile(tag, s);
        }
    }

    public static void logD(String tag, String s, Object... args) {
        if (LOG_DEBUG) {
            if (args.length > 0)
                s = String.format(Locale.US, s, (Object[]) args);
            Log.d(tag, s);
            putLogToFile(tag, s);
        }
    }

    public static void logV(String tag, String s, Object... args) {
        logV(LOG_VERBOSE, tag, s, args);
    }

    public static void logV(String tag, String s) {
        logV(LOG_VERBOSE, tag, s);
    }


    public static void logV(boolean flag, String tag, String s, Object... args) {
        if (flag) {
            if (args.length > 0)
                s = String.format(Locale.US, s, (Object[]) args);
            Log.v(tag, s);
            putLogToFile(tag, s);
        }
    }

    public static void logV(boolean flag, String tag, String s) {
        if (flag) {
            Log.v(tag, s);
            putLogToFile(tag, s);
        }
    }

    public static HttpLoggingInterceptor.Logger createApiLogger() {
        if (LOG_API) {
            return message -> logV(LOG_API, TAG_API, message);
        } else {
            return message -> {
            };
        }
    }

    public static SQLiteCommands.Logger createDbLogger() {
        if (LOG_DB) {
            return new SQLiteCommands.Logger() {
                @Override
                public boolean enabled() {
                    return true;
                }

                @Override
                public void logDb(String message) {
                    Logger.logDb(message);
                }

                @Override
                public void logDb(String message, Object arg) {
                    Logger.logDb(message, arg);
                }

                @Override
                public void logDb(String message, Object... args) {
                    Logger.logDb(message, args);
                }

                @Override
                public void safeThrow(Exception e) {
                    DebugUtils.safeThrow(e);
                }
            };
        } else {
            return new SQLiteCommands.Logger() {
                @Override
                public boolean enabled() {
                    return false;
                }

                @Override
                public void logDb(String message) {

                }

                @Override
                public void logDb(String message, Object arg) {

                }

                @Override
                public void logDb(String message, Object... args) {

                }

                @Override
                public void safeThrow(Exception e) {
                    DebugUtils.safeThrow(e);
                }
            };
        }
    }

    public static void logFcm(String mesage, Object... args) {
        logV(LOG_FCM, TAG_FCM, mesage, args);
    }

    public static void logStat(String message, String event, StatParam[] params) {
        if (!LOG_STAT)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(": \'");
        sb.append(event);
        sb.append("\'");
        if (params != null) {
            sb.append(" {");
            if (params.length > 0) {
                for (StatParam param : params) {
                    sb.append(param.toString()).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
            }

            sb.append('}');
        }
        logV(true, TAG_STAT, sb.toString());
    }

    public static void logStat(String message, String event, Bundle params) {
        if (!LOG_STAT)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(": \'");
        sb.append(event);
        sb.append("\'");
        if (params != null) {
            sb.append(" {");

            if (!params.keySet().isEmpty()) {
                for (String key : params.keySet()) {
                    sb.append(key).append('=').append(params.get(key)).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
            }

            sb.append('}');
        }
        logV(true, TAG_STAT, sb.toString());
    }


    public static void logStat(String message) {
        logV(LOG_STAT, TAG_STAT, message);
    }

    public static void logStat(String message, Object... args) {
        logV(LOG_STAT, TAG_STAT, message, args);
    }

    public static void logDb(String message) {
        logV(LOG_DB, TAG_DB, message);
    }

    public static void logDb(String message, Object arg) {
        logV(LOG_DB, TAG_DB, message, arg);
    }

    public static void logDb(String message, Object... args) {
        logV(LOG_DB, TAG_DB, message, args);
    }

    public static void logDb(String message, SQLiteStatement sql, int r) {
        if (LOG_DB) {
            StringBuilder sb = new StringBuilder();
            sb.append(message);
            sb.append("\n");
            sb.append(sql.toString());
            sb.append("\nreturns ").append(r);
            logV(LOG_VERBOSE, TAG_DB, sb.toString());
        }
    }

    public static void traceUi(Object caller) {
        traceUiInternal(caller, null);
    }

    public static void traceUi(Object caller, String message) {
        traceUiInternal(caller, message);
    }

    public static void traceUiInternal(Object caller, String message) {
        if (!LOG_DEBUG)
            return;
        StackTraceElement traceElement = new Throwable().getStackTrace()[2];

        String canonicalName = caller.getClass().getCanonicalName();
        int pt = canonicalName.lastIndexOf('.');
        if (pt > 0 && pt < canonicalName.length() - 1)
            canonicalName = canonicalName.substring(pt + 1);

        if (message == null)
            message = "";


        if (LOG_MEM) {
            Runtime runtime = Runtime.getRuntime();
            long current = runtime.totalMemory() - runtime.freeMemory();
            logD(TAG_TRACE, "UI %s(%x).%s %s\n" +
                            "MEMORY allocated: %,d delta: %+,d max: %,d, total: %,d, free: %,d",
                    canonicalName, caller.hashCode(), traceElement.getMethodName(), message,
                    current,
                    current - lastMemoryValue,
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory());
            lastMemoryValue = current;
        } else {
            logD(TAG_TRACE, "UI %s(%x).%s %s", canonicalName, caller.hashCode(), traceElement.getMethodName(), message);
        }
    }


    public static void traceUi(String caller, String message) {
        if (!LOG_DEBUG)
            return;

        if (message == null)
            message = "";

        if (LOG_MEM) {
            Runtime runtime = Runtime.getRuntime();
            long current = runtime.totalMemory() - runtime.freeMemory();
            logD(TAG_TRACE, "UI %s %s\n" +
                            "MEMORY allocated: %,d delta: %+,d max: %,d, total: %,d, free: %,d",
                    caller, message,
                    current,
                    current - lastMemoryValue,
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory());
            lastMemoryValue = current;
        } else {
            logD(TAG_TRACE, "%s: %s", caller, message);
        }
    }

    public static void trace() {
        if (!LOG_TRACE)
            return;
        StackTraceElement traceElement = new Throwable().getStackTrace()[1];
        String className = traceElement.getClassName();

        if (LOG_MEM) {
            Runtime runtime = Runtime.getRuntime();
            long current = runtime.totalMemory() - runtime.freeMemory();
            logV(LOG_TRACE, TAG_TRACE, "%s.%s\n" +
                            "MEMORY allocated: %,d delta: %+,d max: %,d, total: %,d, free: %,d",
                    className, traceElement.getMethodName(),
                    current,
                    current - lastMemoryValue,
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory());
            lastMemoryValue = current;
        } else {
            logV(LOG_TRACE, TAG_TRACE, "%s.%s", className, traceElement.getMethodName());
        }
    }

    public static void trace(String line) {
        if (!LOG_TRACE)
            return;
        StackTraceElement traceElement = new Throwable().getStackTrace()[1];
        String className = traceElement.getClassName();
        int pos = className.lastIndexOf('.');
        if (pos >= 0) {
            className = className.substring(pos) + 1;
        }
        if (LOG_MEM) {
            Runtime runtime = Runtime.getRuntime();
            long current = runtime.totalMemory() - runtime.freeMemory();
            logV(LOG_TRACE, TAG_TRACE, "%s.%s (%s)\n" +
                            "MEMORY allocated: %,d delta: %+,d max: %,d, total: %,d, free: %,d",
                    className, traceElement.getMethodName(), line,
                    current,
                    current - lastMemoryValue,
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory());
            lastMemoryValue = current;
        } else {
            logV(LOG_TRACE, TAG_TRACE, "%s.%s (%s)", className, traceElement.getMethodName(), line);
        }
    }

    public static void trace(String s, Object... args) {
        if (!LOG_TRACE)
            return;
        StackTraceElement traceElement = new Throwable().getStackTrace()[1];
        String className = traceElement.getClassName();
        int pos = className.lastIndexOf('.');
        if (pos >= 0) {
            className = className.substring(pos) + 1;
        }
        String line = String.format(Locale.getDefault(), s, args);
        if (LOG_MEM) {
            Runtime runtime = Runtime.getRuntime();
            long current = runtime.totalMemory() - runtime.freeMemory();
            logV(LOG_TRACE, TAG_TRACE, "%s.%s (%s)\n" +
                            "MEMORY allocated: %,d delta: %+,d max: %,d, total: %,d, free: %,d",
                    className, traceElement.getMethodName(), line,
                    current,
                    current - lastMemoryValue,
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory());
            lastMemoryValue = current;
        } else {
            logV(LOG_TRACE, TAG_TRACE, "%s.%s (%s)", className, traceElement.getMethodName(), line);
        }
    }

    public static String toString(Bundle args) {
        if (args == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        for (String s : args.keySet()) {
            sb.append(s).append("='").append(args.get(s)).append("\', ");
        }

        if (sb.length() > 0)
            sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }
}
