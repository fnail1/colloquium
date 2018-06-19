package app.laiki.diagnostics.statistics;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;


public abstract class StatParam {

    protected StatParam(String name) {
        this.name = name;
    }

    public abstract void apply(Bundle out);


    public abstract void apply(Map<String, String> m);

    public final String name;


    public static StatParam duration(int ms) {
        return new IntegerStatParam("duration", ms);
    }

    public static StatParam duration(long ms) {
        return new LongStatParam("duration", ms);
    }

    public static StatParam reason(String reason) {
        return new StringStatParam("reason", reason);
    }

    public static StatParam length(String text) {
        return length(text == null ? 0 : text.length());
    }

    @NonNull
    public static StatParam length(int value) {
        String v;
        if (value == 0)
            v = "0";
        else if (value <= 10)
            v = "1-10";
        else if (value <= 20)
            v = "11-20";
        else if (value <= 30)
            v = "21-30";
        else
            v = "30+";
        return new StringStatParam("length", v);
    }

    public static StatParam type(String name) {
        if (name == null)
            name = "unknown";
        return new StringStatParam("type", name);
    }

    public static StatParam result(String name) {
        return new StatParam.StringStatParam("result", name);
    }

    public static StatParam invite(boolean value) {
        return new BooleanStatParam("invite", value);
    }

    public static StatParam favorite(boolean value) {
        return new BooleanStatParam("favorite", value);
    }

    public static StatParam source(String value) {
        return new StringStatParam("source", value);
    }

    public static StatParam price(int value) {
        return new DoubleStatParam("price", value);
    }

    public static StatParam priceRange(int price) {
        String value;
        if (price <= 100) {
            value = "0-100";
        } else if (price <= 250) {
            value = "101-250";
        } else if (price <= 500) {
            value = "251-500";
        } else if (price <= 1000) {
            value = "501-1000";
        } else
            value = "1000+";

        return new StringStatParam("price", value);
    }


    public static StatParam like(boolean like) {
        return new BooleanStatParam("like", like);
    }


    public static class BooleanStatParam extends StatParam {
        private final boolean value;

        public BooleanStatParam(String name, boolean value) {
            super(name);
            this.value = value;
        }

        @Override
        public void apply(Bundle out) {
            out.putBoolean(name, value);
        }

        @Override
        public void apply(Map<String, String> m) {
            m.put(name, String.valueOf(value));
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    public static class IntegerStatParam extends StatParam {
        private final int value;

        public IntegerStatParam(String name, int value) {
            super(name);
            this.value = value;
        }

        @Override
        public void apply(Bundle out) {
            out.putInt(name, value);
        }

        @Override
        public void apply(Map<String, String> m) {
            m.put(name, String.valueOf(value));
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    public static class LongStatParam extends StatParam {
        private final long value;

        public LongStatParam(String name, long value) {
            super(name);
            this.value = value;
        }

        @Override
        public void apply(Bundle out) {
            out.putLong(name, value);
        }

        @Override
        public void apply(Map<String, String> m) {
            m.put(name, String.valueOf(value));
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    public static class StringStatParam extends StatParam {
        private final String value;

        public StringStatParam(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        public void apply(Bundle out) {
            out.putString(name, value);
        }

        @Override
        public void apply(Map<String, String> m) {
            m.put(name, value);
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }


    public static class DoubleStatParam extends StatParam {
        private final double value;

        public DoubleStatParam(String key, double value) {
            super(key);
            this.value = value;
        }

        @Override
        public void apply(Bundle out) {
            out.putDouble(name, value);
        }

        @Override
        public void apply(Map<String, String> m) {
            m.put(name, String.valueOf(value));
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    public static class NullStatParam extends StatParam {
        protected NullStatParam() {
            super("NULL");
        }

        @Override
        public void apply(Bundle out) {

        }

        @Override
        public void apply(Map<String, String> m) {
        }
    }
}
