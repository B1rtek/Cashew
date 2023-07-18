package com.birtek.cashew.database;

import net.dv8tion.jda.internal.utils.tuple.Pair;

public abstract class TransferrableDatabase extends Database {

    public TransferrableDatabase() {
        Database.exportableDBs.add(this);
    }

    abstract public TransferResult importDataFromServer(String serverID, String destinationServerID, String userID);

    abstract public Pair<Integer, Integer> getInformationAboutDuplicates(String serverID, String destinationServerID);

    abstract public TransferResult importDataFromUser(String userID, String targetUserID, String serverID);

    abstract public TransferResult deleteDataFromUser(String userID, String serverID);

    public enum TransferResult {
        SUCCESS,
        DATABASE_ERROR,
        UNKNOWN_ERROR,
        CONFLICT,
        NO_DEFAULTS_SPECIFIED
    }
}
