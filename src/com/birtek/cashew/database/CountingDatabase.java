package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class CountingDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountingDatabase.class);

    private static volatile CountingDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * counting table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private CountingDatabase() {
        databaseURL = System.getenv("JDBC_DATABASE_URL");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            databaseConnection = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists counting ( _id bigserial constraint idx_16692_counting_pkey primary key, activity boolean, current bigint, channelid text, userid text, messageid text, typosleft integer );");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the counting table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CountingDatabase getInstance() {
        CountingDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CountingDatabase.class) {
            if (instance == null) {
                instance = new CountingDatabase();
            }
            return instance;
        }
    }

    /**
     * Marks the channel as enabled or disabled for the counting game
     *
     * @param newState  new state of the game - either on (true) or off (false)
     * @param channelID ID of the channel where the game is being turned on or off
     * @return true if the change was successful, false otherwise
     */
    public boolean setCountingStatus(boolean newState, String channelID) {
        if (isInDatabase(channelID)) {
            return updateCountingStatus(newState, channelID);
        } else {
            return insertCountingStatus(newState, channelID);
        }
    }

    /**
     * Checks if a channel already has a record in the counting table
     *
     * @param channelID ID of the channel to check for existence in the database
     * @return true if it is in the database, false if it's not or if an error occurred
     */
    private boolean isInDatabase(String channelID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM counting WHERE channelid = ?");
            preparedStatement.setString(1, channelID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates the counting game status of a channel in the database
     *
     * @param newState  new state of the game - either on (true) or off (false)
     * @param channelID ID of the channel where the game is being turned on or off
     * @return true if the update was successful, false otherwise
     */
    private boolean updateCountingStatus(boolean newState, String channelID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE counting SET activity = ? WHERE channelid = ?");
            preparedStatement.setBoolean(1, newState);
            preparedStatement.setString(2, channelID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.updateCountingStatus()");
            return false;
        }
    }

    /**
     * Inserts a new record into the counting database
     *
     * @param newState  new state of the game - either on (true) or off (false)
     * @param channelID ID of the channel where the game is being turned on or off
     * @return true if the update was successful, false otherwise
     */
    private boolean insertCountingStatus(boolean newState, String channelID) {
        if (!newState) return true;
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO counting(activity, current, channelid, typosleft) VALUES(?, ?, ?, ?)");
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, 0);
            preparedStatement.setString(3, channelID);
            preparedStatement.setInt(4, 3);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.insertCountingStatus()");
            return false;
        }
    }

    /**
     * Obtains all data about counting in a channel
     *
     * @param channelID channel ID of the channel to get the counting game data of
     * @return a CountingInfo object containing current state of the game etc. or null if an error occurred
     */
    public CountingInfo getCountingData(String channelID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT activity, userid, current, messageid, typosleft FROM counting WHERE channelid = ?");
            preparedStatement.setString(1, channelID);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return new CountingInfo(result.getBoolean(1), result.getString(2), result.getInt(3), result.getString(4), result.getInt(5));
            }
            return new CountingInfo(false, " ", 0, " ", 3);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.getCountingData()");
            return null;
        }
    }

    /**
     * Sets the count to a specified number in the channel
     *
     * @param countingInfo CountingInfo object containing information about new count
     * @param channelID    channel ID of the channel where the command was executed
     * @return true if the update was successful, false otherwise
     */
    public boolean setCount(CountingInfo countingInfo, String channelID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE counting SET current = ?, userid = ?, messageid = ?, typosleft = ? WHERE channelid = ?");
            preparedStatement.setInt(1, countingInfo.value());
            preparedStatement.setString(2, countingInfo.userID());
            preparedStatement.setString(3, countingInfo.messageID());
            preparedStatement.setInt(4, countingInfo.typosLeft());
            preparedStatement.setString(5, channelID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.setCount()");
            return false;
        }
    }

    public ArrayList<String> getAllActiveCountingChannels() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT channelid FROM counting WHERE activity = true");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<String> channels = new ArrayList<>();
            while (results.next()) {
                channels.add(results.getString(1));
            }
            return channels;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CountingDatabase.getAllActiveCountingChannels()");
            return null;
        }
    }
}
