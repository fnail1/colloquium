package ru.mail.colloquium.diagnostics;

import android.support.annotation.NonNull;

import java.util.Arrays;

import ru.mail.colloquium.diagnostics.statistics.StatParam;
import ru.mail.colloquium.diagnostics.statistics.StatisticsScreen;

public class StatContext {
    private final Object screen;
    private final StatParam[] params;
    private final String optionalSuffix;

    public StatContext(Object screen, StatParam... params) {
        this(screen, null, params);
    }

    public StatContext(Object screen, String nameSuffix, StatParam... params) {
        this.optionalSuffix = nameSuffix;
        this.screen = screen;
        this.params = params;
    }

    public static String screenName(@NonNull Object screen) {
        while (screen instanceof StatContext)
            screen = ((StatContext) screen).screen();

        if (screen instanceof String)
            return screen.toString();

        StatisticsScreen name = screen.getClass().getAnnotation(StatisticsScreen.class);
        return name == null ? screen.getClass().getSimpleName() : name.value();
    }

    public String optionalSuffix() {
        return optionalSuffix;
    }

    public static StatParam[] params(Object screen) {
        if (screen instanceof StatContext)
            return ((StatContext) screen).params();
        return null;
    }

    public static StatParam[] params(Object screen, StatParam[] params) {
        if (screen instanceof StatContext)
            return ((StatContext) screen).params(params);
        return params;
    }

    public Object screen() {
        return screen;
    }

    public StatParam[] params() {
        return params;
    }

    public StatParam[] params(StatParam param) {
        if (params == null)
            return new StatParam[]{param};
        StatParam[] p = Arrays.copyOf(params, params.length + 1);
        p[params.length] = param;
        return p;
    }

    public StatParam[] params(StatParam... ps) {
        if (ps == null)
            return this.params;

        if (this.params == null)
            return ps;

        StatParam[] p = Arrays.copyOf(this.params, params.length + ps.length);
        System.arraycopy(ps, 0, p, params.length, ps.length);
        return p;
    }

}
