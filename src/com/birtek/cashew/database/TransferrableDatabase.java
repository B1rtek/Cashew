package com.birtek.cashew.database;

public abstract class TransferrableDatabase extends Database {

    public TransferrableDatabase() {
        Database.exportableDBs.add(this);
    }

    abstract boolean importDataFromServer(String serverID, String destinationServerID, String userID);

    abstract boolean importDataFromUser(String userID, String targetUserID, String serverID);

    abstract boolean deleteDataFromUser(String userID, String serverID);
}
