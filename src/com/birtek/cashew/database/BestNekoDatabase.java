package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class BestNekoDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestNekoDatabase.class);

    private static volatile BestNekoDatabase instance;

    private Connection bestNekoConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * bestneko table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private BestNekoDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            bestNekoConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = bestNekoConnection.prepareStatement("CREATE TABLE IF NOT EXISTS bestneko(userid text, neko int)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the bestneko table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static BestNekoDatabase getInstance() {
        BestNekoDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (BestNekoDatabase.class) {
            if (instance == null) {
                instance = new BestNekoDatabase();
            }
            return instance;
        }
    }

    /**
     * Sets the favourite neko for the user
     *
     * @param userID ID of the user who is setting their favourite neko
     * @param neko   ID of the neko to set as favourite
     * @return true if the setting was successful, false otherwise
     */
    public boolean setNeko(String userID, int neko) {
        if (isInDatabase(userID)) {
            return updateNeko(userID, neko);
        } else {
            return insertNeko(userID, neko);
        }
    }

    /**
     * Checks whether the user already defined their favourite neko
     *
     * @param userID ID of the user being checked
     * @return true if the user already has a database record, false if they don't or if an error occurred
     */
    private boolean isInDatabase(String userID) {
        try {
            PreparedStatement preparedStatement = bestNekoConnection.prepareStatement("SELECT COUNT(*) FROM bestneko WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates user's favourite neko record in the database
     *
     * @param userID ID of the user whose record is being updated
     * @param neko   ID of the neko being set as favourite
     * @return true if the update was successful, false otherwise
     */
    private boolean updateNeko(String userID, int neko) {
        try {
            PreparedStatement preparedStatement = bestNekoConnection.prepareStatement("UPDATE bestneko SET neko = ? WHERE userid = ?");
            preparedStatement.setInt(1, neko);
            preparedStatement.setString(2, userID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoDatabase.updateNeko()");
            return false;
        }
    }

    /**
     * Inserts user's favourite neko record into the database
     *
     * @param userID ID of the user whose record is being inserted
     * @param neko   ID of the neko being set as favourite
     * @return true if the insertion was successful, false otherwise
     */
    private boolean insertNeko(String userID, int neko) {
        try {
            PreparedStatement preparedStatement = bestNekoConnection.prepareStatement("INSERT INTO bestneko(userid, neko) VALUES(?, ?)");
            preparedStatement.setString(1, userID);
            preparedStatement.setInt(2, neko);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoDatabase.insertNeko()");
            return false;
        }
    }

    /**
     * Gets the ID of the user's favourite neko
     *
     * @param userID ID of the user whose favourite neko ID will be fetched
     * @return the ID of the neko, 0 if the user didn't set their preference yet, or -1 if an error occurred
     */
    public int getNeko(String userID) {
        try {
            PreparedStatement preparedStatement = bestNekoConnection.prepareStatement("SELECT neko FROM bestneko WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoDatabase.getNeko()");
            return -1;
        }
    }
}
