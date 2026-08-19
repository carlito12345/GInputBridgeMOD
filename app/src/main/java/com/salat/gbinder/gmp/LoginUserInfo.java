package com.salat.gbinder.gmp;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginUserInfo implements Parcelable {
    private boolean login;
    private boolean expired;
    private String nickName;
    private String avatarUrl;

    public LoginUserInfo() {
    }

    protected LoginUserInfo(Parcel in) {
        login = in.readByte() != 0;
        expired = in.readByte() != 0;
        nickName = in.readString();
        avatarUrl = in.readString();
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (login ? 1 : 0));
        dest.writeByte((byte) (expired ? 1 : 0));
        dest.writeString(nickName);
        dest.writeString(avatarUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LoginUserInfo> CREATOR = new Creator<LoginUserInfo>() {
        @Override
        public LoginUserInfo createFromParcel(Parcel in) {
            return new LoginUserInfo(in);
        }

        @Override
        public LoginUserInfo[] newArray(int size) {
            return new LoginUserInfo[size];
        }
    };
}
