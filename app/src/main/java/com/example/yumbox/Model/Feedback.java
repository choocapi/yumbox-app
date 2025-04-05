package com.example.yumbox.Model;

public class Feedback {
    private String userUid;
    private float rating;
    private String content;
    private Long currentTime = 0L;

    public Feedback() {
    }

    public Feedback(String userUid, float rating, String content, Long currentTime) {
        this.userUid = userUid;
        this.rating = rating;
        this.content = content;
        this.currentTime = currentTime;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }
}
