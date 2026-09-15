package com.binary_dysfunction;

import java.util.List;

public class Chat {

    public String id;
    public List<Object> members;
    public boolean isGroupChat;
    public String groupName;
    public String pfpPath;

    public Chat(String id, List<Object> members, boolean isGroupChat, String groupName, String pfpPath) {
        this.id = id;
        this.members = members;
        this.isGroupChat = isGroupChat;
        if (isGroupChat) this.groupName = groupName;
        else {
            for (Object object : members) {
                if (object != Main.loggedInAccount.username) {
                    this.groupName = (String) object;
                }
            }
        }
        this.pfpPath = pfpPath;
    }
}
