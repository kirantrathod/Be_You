package com.example.kiran.be_you.model;

public class User {
    public String name, uid, image, status, thumb_image, you;

    public User() {} // Required empty public constructor

    public User(String name, String uid, String image, String status, String thumb_image) {
        this.name = name;
        this.uid = uid;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
