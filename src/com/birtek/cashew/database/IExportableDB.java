package com.birtek.cashew.database;

public interface IExportableDB {

    default void addToList() {
        Database.exportableDBs.add(this);
    }

    abstract boolean importDataFromServer(String serverID, String destinationServerID, String userID);

    abstract boolean importDataFromUser(String userID, String targetUserID, String serverID);

    abstract boolean deleteDataFromUser(String userID, String serverID);
}
