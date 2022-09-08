package com.birtek.cashew.database;

import java.time.Instant;

public final class CachedMessage {
    private final String messageID;
    private final String userID;
    private final String content;
    private final long timestamp;

    public CachedMessage(String messageID, String userID, String content) {
        this.messageID = messageID;
        this.userID = userID;
        this.content = content;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String messageID() {
        return messageID;
    }

    public String userID() {
        return userID;
    }

    public String content() {
        return content;
    }

    public long lastChangeTimestamp() {
        return timestamp;
    }

}
