package ai.tomorrow.photory.models;

public class Follow {

    private String user_id;

    public Follow() {
    }

    public Follow(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Follow{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
