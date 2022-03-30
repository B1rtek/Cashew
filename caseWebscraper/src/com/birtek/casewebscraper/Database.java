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
            PreparedStatement casesCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Cases(_id INTEGER PRIMARY KEY, name TEXT, url TEXT, imageUrl TEXT, knifeGroup INTEGER)");
            PreparedStatement skinsCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Skins(_id INTEGER PRIMARY KEY AUTOINCREMENT, caseId INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT, stashUrl TEXT)");
            PreparedStatement knivesCreate = casesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Knives(_id INTEGER PRIMARY KEY AUTOINCREMENT, knifeGroup INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT, stashUrl TEXT)");
            casesCreate.execute();
            skinsCreate.execute();
            knivesCreate.execute();
            PreparedStatement collectionsCreate = collectionsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Collections(_id INTEGER PRIMARY KEY, name TEXT, url TEXT, imageUrl TEXT)");
            PreparedStatement colSkinsCreate = collectionsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Skins(_id INTEGER PRIMARY KEY AUTOINCREMENT, collectionId INTEGER, name TEXT, rarity INTEGER, minFloat REAL, maxFloat REAL, description TEXT, flavorText TEXT, finishStyle TEXT, wearImg1 TEXT, wearImg2 TEXT, wearImg3 TEXT, inspectFN TEXT, inspectMW TEXT, inspectFT TEXT, inspectWW TEXT, inspectBS TEXT, stashUrl TEXT)");
            collectionsCreate.execute();
            colSkinsCreate.execute();
            PreparedStatement capsulesCreate = capsulesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Capsules(_id INTEGER PRIMARY KEY, name TEXT, url TEXT, imageUrl TEXT)");
            PreparedStatement stickersCreate = capsulesConnection.prepareStatement("CREATE TABLE IF NOT EXISTS Stickers(_id INTEGER PRIMARY KEY AUTOINCREMENT, capsuleId INTEGER, name TEXT, rarity INTEGER, imageUrl TEXT, inspectUrl TEXT, stashUrl TEXT)");
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
            switch (type) {
                case "case" -> preparedStatement = casesConnection.prepareStatement("SELECT _id FROM Cases");
                case "collection" -> preparedStatement = collectionsConnection.prepareStatement("SELECT _id FROM Collections ");
                case "capsule" -> preparedStatement = capsulesConnection.prepareStatement("SELECT _id FROM Capsules");
            }
            if (preparedStatement == null) {
                LOGGER.error("Null prepared statement in getCaseId()!");
                return 0;
            }
            ResultSet results = preparedStatement.executeQuery();
            return getNewIdFromResults(results);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    private int getNewIdFromResults(ResultSet results) throws SQLException {
        if (results == null) {
            return 0;
        }
        int newId = 1;
        while (results.next()) {
            if(results.getInt(1) == newId) {
                newId++;
            } else {
                return newId;
            }
        }
        return newId;
    }

    public boolean knifesAlreadyDone(String knifeUrl) {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("SELECT _id FROM Knives WHERE stashUrl = ?");
            preparedStatement.setString(1, knifeUrl);
            ResultSet results = preparedStatement.executeQuery();
            if(results == null) {
                LOGGER.warn("Couldn't check if knife already exist, defaulting to NO");
                return false;
            }
            return results.next();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public int getKnifeGroup(String knifeUrl) {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("SELECT knifeGroup FROM Knives WHERE stashUrl = ?");
            preparedStatement.setString(1, knifeUrl);
            ResultSet results = preparedStatement.executeQuery();
            if (results == null) {
                return 0;
            }
            if (results.next()) {
                return results.getInt(1);
            } else {
                return getNewKnifeGroup();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    private int getNewKnifeGroup() {
        try {
            PreparedStatement preparedStatement = casesConnection.prepareStatement("SELECT DISTINCT knifeGroup FROM Knives");
            ResultSet results = preparedStatement.executeQuery();
            return getNewIdFromResults(results);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    public boolean saveContainerToDatabase(String type, String caseName, String caseUrl, String caseImageUrl, int knifeGroup) {
        try {
            PreparedStatement preparedStatement = null;
            switch (type) {
                case "case" -> {
                    preparedStatement = casesConnection.prepareStatement("INSERT INTO Cases(name, url, imageUrl, knifeGroup) VALUES(?, ?, ?, ?)");
                    preparedStatement.setInt(4, knifeGroup);
                }
                case "collection" -> preparedStatement = collectionsConnection.prepareStatement("INSERT INTO Collections(name, url, imageUrl) VALUES(?, ?, ?)");
                case "capsule" -> preparedStatement = capsulesConnection.prepareStatement("INSERT INTO Capsules(name, url, imageUrl) VALUES(?, ?, ?)");
            }
            if (preparedStatement == null) {
                LOGGER.error("Null prepared statement in saveContainerToDatabase()!");
                return false;
            }
            preparedStatement.setString(1, caseName);
            preparedStatement.setString(2, caseUrl);
            preparedStatement.setString(3, caseImageUrl);
            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public boolean saveItemToDatabase(SkinWebscraper skinWebscraper) {
        try {
            if(skinWebscraper.getCaseID() == 0) {
                LOGGER.error("Container ID 0 in saveItemToDatabase()!");
                return false;
            }
            PreparedStatement preparedStatement = null;
            switch (skinWebscraper.getType()) {
                case "case" -> preparedStatement = casesConnection.prepareStatement("INSERT INTO Skins(caseId, name, rarity, minFloat, MaxFloat, description, flavorText, finishStyle, wearImg1, wearImg2, wearImg3, inspectFN, inspectMW, inspectFT, inspectWW, inspectBS, stashUrl) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                case "collection" -> preparedStatement = collectionsConnection.prepareStatement("INSERT INTO Skins(collectionId, name, rarity, minFloat, MaxFloat, description, flavorText, finishStyle, wearImg1, wearImg2, wearImg3, inspectFN, inspectMW, inspectFT, inspectWW, inspectBS, stashUrl) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                case "capsule" -> preparedStatement = capsulesConnection.prepareStatement("INSERT INTO Stickers(capsuleId, name, rarity, imageUrl, inspectUrl, stashUrl) VALUES(?, ?, ?, ?, ?, ?)");
                case "gloves", "knife" -> preparedStatement = casesConnection.prepareStatement("INSERT INTO Knives(knifeGroup, name, rarity, minFloat, MaxFloat, description, flavorText, finishStyle, wearImg1, wearImg2, wearImg3, inspectFN, inspectMW, inspectFT, inspectWW, inspectBS, stashUrl) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            if (preparedStatement == null) {
                LOGGER.error("Null prepared statement in saveItemToDatabase()!");
                return false;
            }
            preparedStatement.setInt(1, skinWebscraper.getCaseID());
            preparedStatement.setString(2, skinWebscraper.getName());
            preparedStatement.setInt(3, skinWebscraper.getRarity());
            if(skinWebscraper.getType().equals("capsule")) {
                preparedStatement.setString(4, skinWebscraper.getWearImg1());
                preparedStatement.setString(5, skinWebscraper.getInspectFN());
                preparedStatement.setString(6, skinWebscraper.getUrl());
            } else {
                preparedStatement.setDouble(4, skinWebscraper.getMinFloat());
                preparedStatement.setDouble(5, skinWebscraper.getMaxFloat());
                preparedStatement.setString(6, skinWebscraper.getDescription());
                preparedStatement.setString(7, skinWebscraper.getFlavorText());
                preparedStatement.setString(8, skinWebscraper.getFinishStyle());
                preparedStatement.setString(9, skinWebscraper.getWearImg1());
                preparedStatement.setString(10, skinWebscraper.getWearImg2());
                preparedStatement.setString(11, skinWebscraper.getWearImg3());
                preparedStatement.setString(12, skinWebscraper.getInspectFN());
                preparedStatement.setString(13, skinWebscraper.getInspectMW());
                preparedStatement.setString(14, skinWebscraper.getInspectFT());
                preparedStatement.setString(15, skinWebscraper.getInspectWW());
                preparedStatement.setString(16, skinWebscraper.getInspectBS());
                preparedStatement.setString(17, skinWebscraper.getUrl());
            }
            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}
