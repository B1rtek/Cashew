package com.birtek.cashew.messagereactions;

public class CountingInfo {
    private final boolean active;
    private final String userID;
    private final int value;

    public CountingInfo(boolean active, String userID, int value) {
        this.active = active;
        this.userID = userID;
        this.value = value;
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
}
