package ru.eqbeat.thedoorstracker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by A.Khmelik on 02.04.2016.
 */
public class UserApi implements Parcelable {

    @SerializedName("is_reg")
    int is_reg;


    @SerializedName("hash")
    String hash;

    @SerializedName("error_message")
    String error_message;

    public UserApi(String hash, int is_reg, String error_message) {
        this.is_reg = is_reg;
        this.error_message = error_message;
        this.hash = hash;
    }


    protected UserApi(Parcel in) {
        is_reg = in.readInt();
        error_message = in.readString();
        hash = in.readString();
    }

    public static final Creator<UserApi> CREATOR = new Creator<UserApi>() {
        @Override
        public UserApi createFromParcel(Parcel in) {
            return new UserApi(in);
        }

        @Override
        public UserApi[] newArray(int size) {
            return new UserApi[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(is_reg);
        dest.writeString(error_message);
        dest.writeString(hash);

    }
}
