package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class CasesimCasesDatabase extends CasesimDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasesimCasesDatabase.class);

    private static volatile CasesimCasesDatabase instance;

    private Connection casesimCasesConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/casesimCases.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private CasesimCasesDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            casesimCasesConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/casesimCases.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to casesimCases.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CasesimCasesDatabase getInstance() {
        CasesimCasesDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CasesimCasesDatabase.class) {
            if (instance == null) {
                instance = new CasesimCasesDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a list of names of all cases in the Casesim database
     *
     * @return ArrayList of Strings containing names of all cases, or null if an error occurred
     */
    public ArrayList<String> getAllCasesNames() {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT name FROM Cases");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getAllCasesNames()");
            return null;
        }
    }

    /**
     * Gets a {@link CaseInfo CaseInfos} for the specified case
     *
     * @param caseName name of the case requested
     * @return {@link CaseInfo CaseInfo} object containing all information about the chosen case, or null if the case
     * wasn't found or an error occurred
     */
    public CaseInfo getCaseInfo(String caseName) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Cases WHERE name = ?");
            preparedStatement.setString(1, caseName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getInt(5));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getCaseInfo()");
            return null;
        }
    }

    /**
     * Gets a list of {@link SkinInfo SkinInfos} for all skins in the case
     *
     * @param caseInfo {@link CaseInfo CaseInfo} of the case requested
     * @return ArrayList of {@link SkinInfo SkinInfos} for all skins in the requested case
     */
    public ArrayList<SkinInfo> getCaseSkins(CaseInfo caseInfo) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Skins WHERE caseId = ?");
            preparedStatement.setInt(1, caseInfo.caseId());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getCaseSkins()");
            return null;
        }
    }

    /**
     * Gets a list of {@link SkinInfo SkinInfos} for all knives in the case
     *
     * @param caseInfo {@link CaseInfo CaseInfo} of the case requested
     * @return ArrayList of {@link SkinInfo SkinInfos} for all knives in the requested case
     */
    public ArrayList<SkinInfo> getCaseKnives(CaseInfo caseInfo) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * From Knives where knifeGroup = ?");
            preparedStatement.setInt(1, caseInfo.knifeGroup());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getCaseKnives()");
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
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Skins WHERE _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getSkinById()");
            return null;
        }
    }

    /**
     * Obtains a SkinInfo object for the knife with the given ID
     *
     * @param id ID of the knife to obtain
     * @return a SkinInfo object corresponding to the given ID, or null if the knife wasn't found or if an error occurred
     */
    public SkinInfo getKnifeById(int id) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Knives WHERE _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getKnifeById()");
            return null;
        }
    }

    /**
     * Gets a CaseInfo object for the first case with the provided KnifeGroup
     *
     * @param knifeGroup knifeGroup to obtain the first case with
     * @return a {@link CaseInfo CaseInfo) object of the corresponding case or null if an error occurred
     */
    public CaseInfo getCaseByKnifeGroup(int knifeGroup) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Cases WHERE knifeGroup = ? ORDER BY _id DESC LIMIT 1");
            preparedStatement.setInt(1, knifeGroup);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getInt(5));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CasesimCasesDatabase.getCaseByKnifeGroup()");
            return null;
        }
    }
}
