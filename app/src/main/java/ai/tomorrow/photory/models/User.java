package ai.tomorrow.photory.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private static final String TAG = "User";

    private String user_id;
    private String email;
    private Long phone_number;
    private String username;

    public User(String user_id, String email, Long phone_number, String username) {
        this.user_id = user_id;
        this.email = email;
        this.phone_number = phone_number;
        this.username = username;
    }

    public User() {
    }

    protected User(Parcel in) {
        user_id = in.readString();
        email = in.readString();
        if (in.readByte() == 0) {
            phone_number = null;
        } else {
            phone_number = in.readLong();
        }
        username = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(Long phone_number) {
        this.phone_number = phone_number;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(email);
        if (phone_number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(phone_number);
        }
        dest.writeString(username);
    }
}
