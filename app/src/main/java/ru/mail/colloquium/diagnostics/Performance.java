package ru.mail.colloquium.diagnostics;

import android.os.SystemClock;
import android.util.Log;

public class Performance {

    public static void test1() {

        int x = 0;
        long t0 = SystemClock.elapsedRealtime();
        for (int i = 0; i < 100000; i++) {
            MyClass o = new MyClass();
            x += o.hashCode() % 2;
        }

        long t1 = SystemClock.elapsedRealtime();
        for (int i = 0; i < 100000; i++) {

            try {
                MyClass o = MyClass.class.newInstance();
                x += o.hashCode() % 2;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        long t2 = SystemClock.elapsedRealtime();
        MyClass etalon = new MyClass();
        for (int i = 0; i < 100000; i++) {
            Object o = etalon.clone();
            x += o.hashCode() % 2;
        }
        long t3 = SystemClock.elapsedRealtime();
        Log.v("Performance", "ctor: " + (t1 - t0) + ", reflection:" + (t2 - t1) + ", clone: " + (t3 - t2));
    }

    public static class MyClass implements Cloneable {
        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
