package com.example.messages;

import com.google.firebase.Timestamp;

public class Message {

    private String sender;
    private String recipient;
    private String message;
    private Timestamp time;
    private String id;
    private String picturepath;

    public Message(String msg){
        message=msg;
    }
    public Message(String msg, String sender){
        message=msg;
        this.sender=sender;
    }
    public Message(){}
    public Message(String sender, String msg,String recipient, Timestamp t){
        message=msg;
        this.sender=sender;
        this.recipient=recipient;
        time=t;
        picturepath="nopicture";
    }

    public Message(String sender, String msg,String recipient, Timestamp t, String id){
        message=msg;
        this.sender=sender;
        this.recipient=recipient;
        time=t;
        this.id =id;
    }
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicturepath() {
        return picturepath;
    }

    public void setPicturepath(String picturepath) {
        this.picturepath = picturepath;
    }
}
