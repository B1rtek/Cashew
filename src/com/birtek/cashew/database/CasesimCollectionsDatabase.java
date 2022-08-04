package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class CasesimCollectionsDatabase extends CasesimDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasesimCollectionsDatabase.class);

    private static volatile CasesimCollectionsDatabase instance;

    private Connection casesimCollectionsConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/casesimCollections.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private CasesimCollectionsDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            casesimCollectionsConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/casesimCollections.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to casesimCollections.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CasesimCollectionsDatabase getInstance() {
        CasesimCollectionsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CasesimCollectionsDatabase.class) {
            if (instance == null) {
                instance = new CasesimCollectionsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a list of names of all collections in the Casesim database
     *
     * @return ArrayList of Strings containing names of all collections, or null if an error occurred
     */
    public ArrayList<String> getAllCollectionsNames() {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT name FROM Collections");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCollectionsDatabase.getAllCollectionsNames()");
            return null;
        }
    }

    /**
     * Gets a {@link CaseInfo CaseInfos} for the specified collection
     *
     * @param collectionName name of the collection requested
     * @return {@link CaseInfo CaseInfo} object containing all information about the chosen collection, or null if the
     * collection wasn't found or an error occurred
     */
    public CaseInfo getCollectionInfo(String collectionName) {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT * FROM Collections WHERE name = ?");
            preparedStatement.setString(1, collectionName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), 0);
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCollectionsDatabase.getCollectionInfo()");
            return null;
        }
    }

    /**
     * Gets a list of {@link SkinInfo SkinInfos} for all skins in the collection
     *
     * @param collectionInfo {@link CaseInfo CaseInfo} of the collection requested
     * @return ArrayList of {@link SkinInfo SkinInfos} for all skins in the requested collection
     */
    public ArrayList<SkinInfo> getCollectionSkins(CaseInfo collectionInfo) {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT * FROM Skins WHERE collectionId = ?");
            preparedStatement.setInt(1, collectionInfo.caseId());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCollectionsDatabase.getCollectionSkins()");
            return null;
        }
    }

    /**
     * Obtains a SkinInfo object for the skin with the given ID
     *
     * @param id ID of the skin to obtain
     * @return a SkinInfo object corresponding to the given ID, or null if the skin wasn't found or if an error occurred
     */
    public SkinInfo getSkinById(int id) {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT * FROM Skins WHERE _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCollectionsDatabase.getSkinById()");
            return null;
        }
    }
}
