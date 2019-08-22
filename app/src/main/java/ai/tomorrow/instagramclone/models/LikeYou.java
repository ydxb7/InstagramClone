package ai.tomorrow.instagramclone.models;

public class LikeYou {

    private String liked_by_user_id;
    private String photo_id;
    private String date_created;

    public LikeYou() {
    }

    public LikeYou(String liked_by_user_id, String photo_id, String date_created) {
        this.liked_by_user_id = liked_by_user_id;
        this.photo_id = photo_id;
        this.date_created = date_created;
    }

    public String getLiked_by_user_id() {
        return liked_by_user_id;
    }

    public void setLiked_by_user_id(String liked_by_user_id) {
        this.liked_by_user_id = liked_by_user_id;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "LikeYou{" +
                "liked_by_user_id='" + liked_by_user_id + '\'' +
                ", photo_id='" + photo_id + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
