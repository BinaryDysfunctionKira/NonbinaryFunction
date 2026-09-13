package com.binary_dysfunction;

import java.util.List;

public class Chat {

    public String id;
    public List<Object> members;
    public String lastSender;
    public String lastMessage;
    public long lastWrittenDate;
    public boolean isGroupChat;
    public String groupName;

    public Chat(String id, List<Object> members, String lastSender, String lastMessage, long lastWrittenDate, boolean isGroupChat, String groupName) {
        this.id = id;
        this.members = members;
        this.lastSender = lastSender;
        this.lastMessage = lastMessage;
        this.lastWrittenDate = lastWrittenDate;
        this.isGroupChat = isGroupChat;
        if (isGroupChat) this.groupName = groupName;
        else {
            for (Object object : members) {
                if (object != Main.loggedInAccount.username) {
                    this.groupName = (String) object;
                }
            }
        }
    }
}
