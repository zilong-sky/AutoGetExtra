package com.zilong.autogetextra;

import android.os.Parcel;
import android.os.Parcelable;

public class DemoBean implements Parcelable {
    private String demo;

    public DemoBean() {
    }

    protected DemoBean(Parcel in) {
        demo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(demo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DemoBean> CREATOR = new Creator<DemoBean>() {
        @Override
        public DemoBean createFromParcel(Parcel in) {
            return new DemoBean(in);
        }

        @Override
        public DemoBean[] newArray(int size) {
            return new DemoBean[size];
        }
    };

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }
}
