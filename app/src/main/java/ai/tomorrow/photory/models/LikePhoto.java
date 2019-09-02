package ai.tomorrow.photory.models;

public class LikePhoto {

    private String liked_by_user_id;
    private String liked_to_user_id;
    private String date_created;
    private String photo_id;

    public LikePhoto() {
    }

    public LikePhoto(String liked_by_user_id, String liked_to_user_id, String date_created, String photo_id) {
        this.liked_by_user_id = liked_by_user_id;
        this.liked_to_user_id = liked_to_user_id;
        this.date_created = date_created;
        this.photo_id = photo_id;
    }

    public String getLiked_by_user_id() {
        return liked_by_user_id;
    }

    public void setLiked_by_user_id(String liked_by_user_id) {
        this.liked_by_user_id = liked_by_user_id;
    }

    public String getLiked_to_user_id() {
        return liked_to_user_id;
    }

    public void setLiked_to_user_id(String liked_to_user_id) {
        this.liked_to_user_id = liked_to_user_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    @Override
    public String toString() {
        return "LikePhoto{" +
                "liked_by_user_id='" + liked_by_user_id + '\'' +
                ", liked_to_user_id='" + liked_to_user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                ", photo_id='" + photo_id + '\'' +
                '}';
    }
}
