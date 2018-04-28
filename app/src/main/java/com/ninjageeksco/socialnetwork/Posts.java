package com.ninjageeksco.socialnetwork;

/**
 * Created by Ahmad on 14/03/2018.
 */

public class Posts {
    public  String uid, time,date,fullname,postimage,describtion,prfileimage;
    public Posts(){}
    public Posts(String uid, String time, String date, String fullname, String postimage, String describtion, String prfileimage) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.fullname = fullname;
        this.postimage = postimage;
        this.describtion = describtion;
        this.prfileimage = prfileimage;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {this.uid = uid;}
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    public String getPostimage() {
        return postimage;
    }
    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }
    public String getDescribtion() {
        return describtion;
    }
    public void setDescribtion(String describtion) {
        this.describtion = describtion;
    }
    public String getPrfileimage() {return prfileimage;}

    public void setPrfileimage(String prfileimage) {this.prfileimage = prfileimage;}
}
