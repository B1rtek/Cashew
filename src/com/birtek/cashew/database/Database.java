package com.birtek.cashew.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    protected Connection databaseConnection;
    protected String databaseURL;

    /**
     * Tries to reestablish a connection
     *
     * @return true if it was successful, false if it wasn't
     */
    protected boolean reestablishConnection() {
        try {
            databaseConnection = DriverManager.getConnection(databaseURL);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
