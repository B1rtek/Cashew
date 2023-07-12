package com.birtek.cashew.database;

public abstract class ExportableDatabase extends Database {

    public ExportableDatabase() {
        Database.exportableDBs.add(this);
    }

    abstract boolean importDataFromServer(String serverID, String destinationServerID, String userID);

    abstract boolean importDataFromUser(String userID, String targetUserID, String serverID);

    abstract boolean deleteDataFromUser(String userID, String serverID);
}
