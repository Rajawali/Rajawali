package org.rajawali3d.examples.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Locale;

public class Example implements INamed, Parcelable {

    public static final Creator<Example> CREATOR = new Creator<Example>() {
        @Override
        public Example createFromParcel(Parcel in) {
            return new Example(in);
        }

        @Override
        public Example[] newArray(int size) {
            return new Example[size];
        }
    };

    private static final String BASE_URL
            = "https://github.com/Rajawali/Rajawali/tree/master/examples/src/main/java";
    @StringRes
    private final int name;
    private final String url;
    private final Class aClass;

    public Example(@StringRes int name,
            @NonNull Class<?> aClass) {

        this.name = name;
        this.aClass = aClass;
        this.url = aClass.getName()
                .replaceAll("\\.", "/");
    }

    protected Example(Parcel in) {
        name = in.readInt();
        url = in.readString();
        aClass = (Class) in.readSerializable();
    }

    @StringRes
    @Override
    public int getName() {
        return name;
    }

    @NonNull
    public String getPath() {
        return String.format(Locale.ENGLISH, "%s/%s.java", BASE_URL, url);
    }

    @NonNull
    public Class getType() {
        return aClass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(name);
        dest.writeString(url);
        dest.writeSerializable(aClass);
    }

}
