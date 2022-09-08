package com.birtek.cashew.database;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageCache {

    HashMap<String, HashMap<String, CachedMessage>> cachingMap;

    public MessageCache() {
        cachingMap = new HashMap<>();
    }

    /**
     * Adds a new message to the cache and removes all messages older than 1 hour
     *
     * @param message  {@link CachedMessage message} to cache
     * @param serverID ID of the server from which the message came
     */
    public void newMessage(CachedMessage message, String serverID) {
        HashMap<String, CachedMessage> serverMessages = cachingMap.get(serverID);
        if (serverMessages == null) {
            serverMessages = new HashMap<>();
        } else {
            long currentTimestamp = Instant.now().toEpochMilli();
            ArrayList<String> toRemove = new ArrayList<>();
            for (String id : serverMessages.keySet()) {
                if (currentTimestamp - serverMessages.get(id).lastChangeTimestamp() > 3600000) {
                    toRemove.add(id);
                }
            }
            for (String id : toRemove) {
                serverMessages.remove(id);
            }
        }
        serverMessages.put(message.messageID(), message);
        cachingMap.put(serverID, serverMessages);
    }

    /**
     * Gets the cached message with the provided ID
     *
     * @param messageID ID of the message to get
     * @param serverID  ID of the server from which the message is requested
     * @return the {@link CachedMessage CachedMessage} if it's still in the cache or null if it's not
     */
    public CachedMessage getMessage(String messageID, String serverID) {
        HashMap<String, CachedMessage> serverMessages = cachingMap.get(serverID);
        if (serverMessages == null) {
            serverMessages = new HashMap<>();
            cachingMap.put(serverID, serverMessages);
            return null;
        } else {
            return serverMessages.get(messageID);
        }
    }

    /**
     * Removes a message from the cache
     * @param messageID ID of the message to remove from cache
     * @param serverID ID of the server from which the message came
     */
    public void deleteCachedMessage(String messageID, String serverID) {
        HashMap<String, CachedMessage> serverMessages = cachingMap.get(serverID);
        if (serverMessages == null) {
            serverMessages = new HashMap<>();
        } else {
            serverMessages.remove(messageID);
        }
        cachingMap.put(serverID, serverMessages);
    }
}
