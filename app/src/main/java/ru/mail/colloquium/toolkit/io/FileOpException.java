package ru.mail.colloquium.toolkit.io;

import android.support.annotation.NonNull;

import java.io.File;

public class FileOpException extends Exception {
    public FileOpException(FileOp op, File file) {
        super("failed to " + op, new Exception(descrFile(file)));
    }

    public FileOpException(FileOp op, File file1, File file2) {
        super("failed to " + op, new Exception(descrFile(file1) + ", " + descrFile(file2)));
    }

    public FileOpException(FileOp op, File file, Throwable e) {
        super("failed to " + op, new Exception(descrFile(file), e));
    }

    @NonNull
    private static String descrFile(File f) {
        return f.getAbsolutePath() + " (" + (f.exists() ? "exist" : "not exist") + ")";
    }

    public enum FileOp {
        DELETE, RENAME, MKDIR
    }
}
