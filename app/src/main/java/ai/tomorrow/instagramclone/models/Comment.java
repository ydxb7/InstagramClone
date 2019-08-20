package ai.tomorrow.instagramclone.models;

import java.util.List;

public class Comment {

    private String comment;
    private String user_id;
    private String photo_id;
    private String comment_id;
    private List<Like> likes;
    private String date_created;
    private String reply_to_username;

    public Comment() {
    }

    public Comment(String comment, String user_id, String photo_id, String comment_id, List<Like> likes, String date_created, String reply_to_username) {
        this.comment = comment;
        this.user_id = user_id;
        this.photo_id = photo_id;
        this.comment_id = comment_id;
        this.likes = likes;
        this.date_created = date_created;
        this.reply_to_username = reply_to_username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getReply_to_username() {
        return reply_to_username;
    }

    public void setReply_to_username(String reply_to_username) {
        this.reply_to_username = reply_to_username;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", photo_id='" + photo_id + '\'' +
                ", comment_id='" + comment_id + '\'' +
                ", likes=" + likes +
                ", date_created='" + date_created + '\'' +
                ", reply_to_username='" + reply_to_username + '\'' +
                '}';
    }
}
