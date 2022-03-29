package com.birtek.casewebscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

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
            PreparedStatement casesCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Cases(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, url TEXT, imageUrl TEXT, knifeGroup INTEGER)");
            PreparedStatement skinsCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Skins(_id INTEGER PRIMARY KEY AUTOINCREMENT, caseId INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT)");
            PreparedStatement knivesCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Knives(_id INTEGER PRIMARY KEY AUTOINCREMENT, knifeGroup INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT)");
            PreparedStatement knifeGroupTableCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS KnifeGroups(_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT)");
            casesCreate.execute();
            skinsCreate.execute();
            knivesCreate.execute();
            knifeGroupTableCreate.execute();
            PreparedStatement collectionsCreate = collectionsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Collections(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, url TEXT, imageUrl TEXT)");
            PreparedStatement colSkinsCreate = collectionsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Skins(_id INTEGER PRIMARY KEY AUTOINCREMENT, collectionId INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT)");
            collectionsCreate.execute();
            colSkinsCreate.execute();
            PreparedStatement capsulesCreate = capsulesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Capsules(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, url TEXT, imageUrl TEXT)");
            PreparedStatement stickersCreate = capsulesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Stickers(_od INTEGER PRIMARY KEY AUTOINCREMENT, capsuleId INTEGER, name TEXT, rarity INTEGER, imageUrl TEXT, inspectUrl TEXT)");
            capsulesCreate.execute();
            stickersCreate.execute();
        } catch (SQLException e) {
            System.err.println("An error occurred while creating the tables");
            e.printStackTrace();
        }
    }

    public int getContainerId(String type) {
        try {
            PreparedStatement preparedStatement = null;
            switch(type) {
                case "case" -> preparedStatement = casesConnection.prepareStatement("SELECT Count(*) FROM Cases");
                case "collection" -> preparedStatement = collectionsConnection.prepareStatement("SELECT Count(*) FROM Collections ");
                case "capsule" -> preparedStatement = capsulesConnection.prepareStatement("SELECT Count(*) FROM Capsules");
            }
            if(preparedStatement == null) {
                LOGGER.error("Null prepared statement in getCaseId()!");
                return 0;
            }
            ResultSet results = preparedStatement.executeQuery();
            if (results == null) {
                return 0;
            }
            if (results.next()) {
                return results.getInt(1) + 1;
            }
            return 1;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    public int getKnifeGroup(String knifeUrl) {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("SELECT _id FROM KnifeGroups WHERE url = ?");
            preparedStatement.setString(1, knifeUrl);
            ResultSet results = preparedStatement.executeQuery();
            if (results == null) {
                return 0;
            }
            if (results.next()) {
                return results.getInt(1);
            } else {
                return insertNewKnifeGroup(knifeUrl);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    private int insertNewKnifeGroup(String knifeUrl) {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("INSERT INTO KnifeGroups(url) VALUES(?)");
            preparedStatement.setString(1, knifeUrl);
            if (preparedStatement.executeUpdate() == 0) {
                return 0;
            }
            preparedStatement = casesConnection.prepareStatement("SELECT Count(*) FROM KnifeGroups");
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    public boolean saveCaseToDatabase(String caseName, String caseUrl, String caseImageUrl, int knifeGroup) {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("INSERT INTO Cases(name, url, imageUrl, knifeGroup) VALUES(?, ?, ?, ?)");
            preparedStatement.setString(1, caseName);
            preparedStatement.setString(2, caseUrl);
            preparedStatement.setString(3, caseImageUrl);
            preparedStatement.setInt(4, knifeGroup);
            return preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}
