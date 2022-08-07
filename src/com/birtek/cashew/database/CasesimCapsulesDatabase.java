package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class CasesimCapsulesDatabase extends CasesimDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasesimCapsulesDatabase.class);

    private static volatile CasesimCapsulesDatabase instance;

    private Connection casesimCapsulesConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/casesimCapsules.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private CasesimCapsulesDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            casesimCapsulesConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/casesimCapsules.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to casesimCapsules.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CasesimCapsulesDatabase getInstance() {
        CasesimCapsulesDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CasesimCapsulesDatabase.class) {
            if (instance == null) {
                instance = new CasesimCapsulesDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a list of names of all capsules in the Casesim database
     *
     * @return ArrayList of Strings containing names of all capsules, or null if an error occurred
     */
    public ArrayList<String> getAllCapsulesNames() {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT name FROM Capsules");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCapsulesDatabase.getAllCapsulesNames()");
            return null;
        }
    }

    /**
     * Gets a {@link CaseInfo CaseInfos} for the specified capsule
     *
     * @param capsuleName name of the capsule requested
     * @return {@link CaseInfo CaseInfo} object containing all information about the chosen capsule, or null if the
     * capsule wasn't found or an error occurred
     */
    public CaseInfo getCapsuleInfo(String capsuleName) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Capsules WHERE name = ?");
            preparedStatement.setString(1, capsuleName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), 0);
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCapsulesDatabase.getCapsuleInfo()");
            return null;
        }
    }

    /**
     * Gets a {@link CaseInfo CaseInfos} for the specified capsule id
     *
     * @param capsuleID id of the capsule requested
     * @return {@link CaseInfo CaseInfo} object containing all information about the chosen capsule, or null if the
     * capsule wasn't found or an error occurred
     */
    public CaseInfo getCapsuleByID(int capsuleID) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Capsules WHERE _id = ?");
            preparedStatement.setInt(1, capsuleID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), 0);
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCapsulesDatabase.getCapsuleByID()");
            return null;
        }
    }

    /**
     * Gets a list of {@link SkinInfo SkinInfos} for all items in the capsule
     *
     * @param capsuleInfo {@link CaseInfo CaseInfo} of the capsule requested
     * @return ArrayList of {@link SkinInfo SkinInfos} for all items in the requested capsule, not all fields of the
     * SkinInfo object will be used
     */
    public ArrayList<SkinInfo> getCapsuleItems(CaseInfo capsuleInfo) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Stickers WHERE capsuleId = ?");
            preparedStatement.setInt(1, capsuleInfo.caseId());
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<SkinInfo> items = new ArrayList<>();
            while (results.next()) {
                items.add(new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], 0.0f, 0.0f, "", "", "", results.getString(5), "", "", results.getString(6), "", "", "", "", results.getString(7)));
            }
            return items;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCapsulesDatabase.getCapsuleItems()");
            return null;
        }
    }

    /**
     * Obtains a SkinInfo object for the item with the given ID
     *
     * @param id ID of the item to obtain
     * @return a SkinInfo object corresponding to the given ID, or null if the item wasn't found or if an error occurred
     */
    public SkinInfo getItemById(int id) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Stickers WHERE _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], 0.0f, 0.0f, "", "", "", results.getString(5), "", "", results.getString(6), "", "", "", "", results.getString(7));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCapsulesDatabase.getItemById()");
            return null;
        }
    }
}
