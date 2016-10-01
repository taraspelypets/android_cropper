package com.example.lenovo.crop;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lenovo on 14.09.2016.
 */
public class SettingsParcelable implements Parcelable {
    public Uri photoUri = new Uri.Builder().build();
    boolean rotationGesture;
    boolean isAspectRatioFixed;
    boolean builtInAnimation;
    boolean grid;


    protected SettingsParcelable(Parcel in) {
        photoUri = in.readParcelable(Uri.class.getClassLoader());
    }

    protected SettingsParcelable() {
    }

    public static final Creator<SettingsParcelable> CREATOR = new Creator<SettingsParcelable>() {
        @Override
        public SettingsParcelable createFromParcel(Parcel in) {
            return new SettingsParcelable(in);
        }

        @Override
        public SettingsParcelable[] newArray(int size) {
            return new SettingsParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(photoUri, flags);
    }
}
