package ai.tomorrow.instagramclone.models;

import ai.tomorrow.instagramclone.Login.LoginActivity;

public class User {

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
}
