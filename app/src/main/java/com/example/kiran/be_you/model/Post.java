package com.example.kiran.be_you.model;

public class Post {
    private String title, desc, blog_image;

    public Post() {} // Required empty public constructor

    public Post(String title, String desc, String blog_image) {
        this.title = title;
        this.desc = desc;
        this.blog_image = blog_image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public String getBlog_image() {
        return blog_image;
    }
}
