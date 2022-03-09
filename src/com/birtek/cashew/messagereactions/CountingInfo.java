package com.birtek.cashew.messagereactions;

public class CountingInfo {
    private final boolean active;
    private final String userID;
    private final int value;
    private final String messageID;

    public CountingInfo(boolean active, String userID, int value, String messageID) {
        this.active = active;
        this.userID = userID;
        this.value = value;
        this.messageID = messageID;
    }

    public boolean getActive() {
        return active;
    }

    public String getUserID() {
        return userID;
    }

    public int getValue() {
        return value;
    }

    public String getMessageID() {
        return messageID;
    }
}
