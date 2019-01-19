package com.example.kiran.be_you;

/**
 * Created by Kiran on 6/16/2017.
 */

public class post {
    private String title;
    private String desc;
    private String blog_image;
    private String thumb_image;
    public post(){
    }
    public post(String title, String desc, String blog_image, String thumb_image) {
        this.title = title;
        this.desc = desc;
        this.blog_image = blog_image;
        this.thumb_image = thumb_image;
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

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBlog_image() {
        return blog_image;
    }

    public void setBlog_image(String blog_image) {
        this.blog_image = blog_image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
