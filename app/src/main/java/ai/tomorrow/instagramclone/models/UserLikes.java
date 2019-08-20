package ai.tomorrow.instagramclone.models;

import java.util.List;

public class UserLikes {

    private String user_id;
    private List<LikePhoto> photo_likes;
    private List<LikeComment> comment_likes;

    @Override
    public String toString() {
        return "UserLikes{" +
                "user_id='" + user_id + '\'' +
                ", photo_likes=" + photo_likes +
                ", comment_likes=" + comment_likes +
                '}';
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<LikePhoto> getPhoto_likes() {
        return photo_likes;
    }

    public void setPhoto_likes(List<LikePhoto> photo_likes) {
        this.photo_likes = photo_likes;
    }

    public List<LikeComment> getComment_likes() {
        return comment_likes;
    }

    public void setComment_likes(List<LikeComment> comment_likes) {
        this.comment_likes = comment_likes;
    }

    public UserLikes() {
    }

    public UserLikes(String user_id, List<LikePhoto> photo_likes, List<LikeComment> comment_likes) {
        this.user_id = user_id;
        this.photo_likes = photo_likes;
        this.comment_likes = comment_likes;
    }
}
