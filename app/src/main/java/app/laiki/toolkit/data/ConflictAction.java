package app.laiki.toolkit.data;


import app.laiki.BuildConfig;

public enum ConflictAction {
    ROLLBACK, ABORT, FAIL, IGNORE, REPLACE,
    IGNORE_OR_FAIL_IF_DEBUG {
        @Override
        public String toString() {
            return BuildConfig.DEBUG ? FAIL.toString() : IGNORE.toString();
        }
    }
}
