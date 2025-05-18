package hu.mobilalkfejl.fakebook.Models;

public class Posts {
    String desc, image;
    String userId;
    long timestamp;
    String id;

    int like;

    public Posts() {}

    public Posts(String desc, String image) {
        this.desc = desc;
        this.image = image;
    }

    public Posts(String desc, String image, String userId, long timestamp) {
        this.desc = desc;
        this.image = image;
        this.userId = userId;
        this.timestamp = timestamp;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long  getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}