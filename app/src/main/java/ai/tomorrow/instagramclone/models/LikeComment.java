package ai.tomorrow.instagramclone.models;

public class LikeComment {

    private String liked_by_user_id;
    private String liked_to_user_id;
    private String date_created;
    private String comment_id;

    public LikeComment() {
    }

    @Override
    public String toString() {
        return "LikeComment{" +
                "liked_by_user_id='" + liked_by_user_id + '\'' +
                ", liked_to_user_id='" + liked_to_user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                ", comment_id='" + comment_id + '\'' +
                '}';
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

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public LikeComment(String liked_by_user_id, String liked_to_user_id, String date_created, String comment_id) {
        this.liked_by_user_id = liked_by_user_id;
        this.liked_to_user_id = liked_to_user_id;
        this.date_created = date_created;
        this.comment_id = comment_id;
    }
}
