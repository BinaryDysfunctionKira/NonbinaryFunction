package com.binary_dysfunction;

public class Message {

    public String id;
    public String content;
    public long date;
    public boolean isRead;
    public String senderUID;

    public Message(String id, String content, long date, boolean isRead, String senderUID) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.senderUID = senderUID;
    }
}
