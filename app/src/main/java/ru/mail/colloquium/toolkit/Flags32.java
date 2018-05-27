package ru.mail.colloquium.toolkit;

import android.os.Parcel;
import android.os.Parcelable;

public class Flags32 implements Parcelable{
    public int data;

    public Flags32() {

    }
    public Flags32(int value) {
        data = value;
    }

    protected Flags32(Parcel in) {
        data = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Flags32> CREATOR = new Creator<Flags32>() {
        @Override
        public Flags32 createFromParcel(Parcel in) {
            return new Flags32(in);
        }

        @Override
        public Flags32[] newArray(int size) {
            return new Flags32[size];
        }
    };

    public int get() {
        return data;
    }

    public boolean get(int mask) {
        return mask == (data & mask);
    }

    public void set(int mask, boolean value) {
        if (value)
            data |= mask;
        else
            data &= ~mask;
    }

    public boolean getAndSet(int mask, boolean value){
        int copy = data;
        set(mask, value);
        return (copy == data) == value;
    }

    public void set(int value) {
        data = value;
    }

    @Override
    public String toString() {
        return "0x" + Integer.toHexString(data) + "(" + data + ")";
    }
}
