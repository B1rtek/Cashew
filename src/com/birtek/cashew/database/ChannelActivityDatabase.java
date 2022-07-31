package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ChannelActivityDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelActivityDatabase.class);

    private static volatile ChannelActivityDatabase instance;

    private Connection channelActivityConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * channelactivity table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private ChannelActivityDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            channelActivityConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = channelActivityConnection.prepareStatement("DROP TABLE IF EXISTS channelactivity");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to remove the channelactivity table!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static ChannelActivityDatabase getInstance() {
        ChannelActivityDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ChannelActivityDatabase.class) {
            if (instance == null) {
                instance = new ChannelActivityDatabase();
            }
            return instance;
        }
    }

    /**
     * Checks the channel activity level set for the channel
     *
     * @param channelID channel ID of the channel to check the setting for
     * @return int that can take four values:
     * 0 - Cashew will not react to messages in the channel
     * 1 - Cashew will react to messages but the reactions won't contain any pings
     * 2 - Cashew will react to messages and in certain conditions might ping someone, send a longer message or more than one message
     * -1 - The query has failed
     */
    public int getChannelActivity(String channelID) {
        try {
            PreparedStatement preparedStatement = channelActivityConnection.prepareStatement("SELECT activity FROM channelactivity WHERE channelid = ?");
            preparedStatement.setString(1, channelID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1);
            }
            return 0; // not found in the database - by default activity is set to off
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ChannelActivityDatabase.getChannelActivity()");
            return -1;
        }
    }

    /**
     * Updates the channel activity level of the channel to the desired level
     *
     * @param channelID     channel ID of the channel getting an update to its settings
     * @param activityLevel new activity level, activity levels and their meaning is described in {@link #getChannelActivity(String) getChannelActivity()} method
     * @return true if the update was successful, false otherwise
     */
    public boolean updateChannelActivity(String channelID, int activityLevel) {
        if (isInDatabase(channelID)) {
            return updateActivitySettings(channelID, activityLevel);
        } else {
            return insertActivitySettings(channelID, activityLevel);
        }
    }

    /**
     * Checks whether the channel with the provided channel ID has an entry in the database
     *
     * @param channelID ID of the channel to check the presence of in the database
     * @return true if the channel has its record in the database, false otherwise.
     * In case of the query failing, false will be returned
     */
    private boolean isInDatabase(String channelID) {
        try {
            PreparedStatement preparedStatement = channelActivityConnection.prepareStatement("SELECT COUNT(*) FROM channelactivity WHERE channelid = ?");
            preparedStatement.setString(1, channelID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ChannelActivityDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates the channel activity record of the channel in the database
     *
     * @param channelID     channel to update the record of
     * @param activityLevel new value of the activity setting for the channel
     * @return true if the update was successful, false otherwise (also when an exception gets thrown)
     */
    private boolean updateActivitySettings(String channelID, int activityLevel) {
        try {
            PreparedStatement preparedStatement = channelActivityConnection.prepareStatement("UPDATE channelactivity SET activity = ? WHERE channelid = ?");
            preparedStatement.setInt(1, activityLevel);
            preparedStatement.setString(2, channelID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ChannelActivityDatabase.updateActivitySettings()");
            return false;
        }
    }

    /**
     * Inserts a new channel activity record of the channel into the database
     *
     * @param channelID     channel to create a new record of
     * @param activityLevel new value of the activity setting for the channel
     * @return true if the insertion was successful, false otherwise (also when an exception gets thrown)
     */
    private boolean insertActivitySettings(String channelID, int activityLevel) {
        try {
            PreparedStatement preparedStatement = channelActivityConnection.prepareStatement("INSERT INTO channelactivity(channelid, activity) VALUES(?, ?)");
            preparedStatement.setString(1, channelID);
            preparedStatement.setInt(2, activityLevel);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ChannelActivityDatabase.insertActivitySettings()");
            return false;
        }
    }
}
