package ai.tomorrow.instagramclone.models;

import java.util.List;

public class UserLikes {

    private String user_id;
    private List<Like> photo_likes;
    private List<Like> comment_likes;

    public UserLikes(String user_id, List<Like> photo_likes, List<Like> comment_likes) {
        this.user_id = user_id;
        this.photo_likes = photo_likes;
        this.comment_likes = comment_likes;
    }

    public UserLikes() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Like> getPhoto_likes() {
        return photo_likes;
    }

    public void setPhoto_likes(List<Like> photo_likes) {
        this.photo_likes = photo_likes;
    }

    public List<Like> getComment_likes() {
        return comment_likes;
    }

    public void setComment_likes(List<Like> comment_likes) {
        this.comment_likes = comment_likes;
    }

    @Override
    public String toString() {
        return "UserLikes{" +
                "user_id='" + user_id + '\'' +
                ", photo_likes=" + photo_likes +
                ", comment_likes=" + comment_likes +
                '}';
    }
}
