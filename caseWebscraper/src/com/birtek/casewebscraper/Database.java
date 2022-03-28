package com.birtek.casewebscraper;

import java.sql.*;

public final class Database {

    private static volatile Database instance;

    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String CASES_DB = "jdbc:sqlite:../databases/casesimCases.db";
    public static final String COLLECTIONS_DB = "jdbc:sqlite:../databases/casesimCollections.db";
    public static final String CAPSULES_DB = "jdbc:sqlite:../databases/casesimCapsules.db";

    private Connection casesConnection;
    private Connection collectionsConnection;
    private Connection capsulesConnection;

    private Database() {
        try {
            Class.forName(Database.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Missing JDBC driver");
            e.printStackTrace();
        }

        try {
            casesConnection = DriverManager.getConnection(CASES_DB);
            collectionsConnection = DriverManager.getConnection(COLLECTIONS_DB);
            capsulesConnection = DriverManager.getConnection(CAPSULES_DB);
        } catch (SQLException e) {
            System.err.println("There was a problem while establishing a connection with the databases");
            e.printStackTrace();
        }

        createTables();
    }

    public static Database getInstance() {
        Database result = instance;
        if (result != null) {
            return result;
        }
        synchronized (Database.class) {
            if (instance == null) {
                instance = new Database();
            }
            return instance;
        }
    }

    private void createTables() {
        try {
            Statement casesCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Cases(_id INTEGER PRIMARY KEY AUTOINCREMENT)");
        } catch (SQLException e) {
            System.err.println("An error occurred while creating the tables");
            e.printStackTrace();
        }

    }

}
