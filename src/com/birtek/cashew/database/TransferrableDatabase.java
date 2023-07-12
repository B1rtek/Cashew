package com.birtek.cashew.database;

public abstract class TransferrableDatabase extends Database {

    public TransferrableDatabase() {
        Database.exportableDBs.add(this);
    }

    abstract TransferResult importDataFromServer(String serverID, String destinationServerID, String userID);

    abstract TransferResult importDataFromUser(String userID, String targetUserID, String serverID);

    abstract TransferResult deleteDataFromUser(String userID, String serverID);

    public enum TransferResult {
        SUCCESS,
        DATABASE_ERROR,
        UNKNOWN_ERROR,
        NO_DEFAULTS_SPECIFIED
    }
}
