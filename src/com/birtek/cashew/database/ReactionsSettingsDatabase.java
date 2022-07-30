package com.birtek.cashew.database;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;

public class ReactionsSettingsDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionsSettingsDatabase.class);

    private static volatile ReactionsSettingsDatabase instance;

    private Connection reactionsConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * reactions table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private ReactionsSettingsDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            reactionsConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS reactions(serverid TEXT, settings TEXT)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the reactions table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static ReactionsSettingsDatabase getInstance() {
        ReactionsSettingsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ReactionsSettingsDatabase.class) {
            if (instance == null) {
                instance = new ReactionsSettingsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets all reactions settings from the database and returns them in a HashMap mapping the server ID to the settings
     *
     * @return HashMap with all settings from the database or null if an error occurred
     */
    public HashMap<String, ReactionsSettings> getAllReactionsSettings() {
        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("SELECT * FROM reactions");
            ResultSet results = preparedStatement.executeQuery();
            HashMap<String, ReactionsSettings> settingsHashMap = new HashMap<>();
            while (results.next()) {
                settingsHashMap.put(results.getString(1), new ReactionsSettings(new JSONObject(results.getString(2)), results.getString(1)));
            }
            return settingsHashMap;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ReactionsDatabase.getAllReactionsSettings()");
            return null;
        }
    }

    /**
     * Sets new reactions settings for a server
     *
     * @param settings {@link ReactionsSettings ReactionSettings} object with settings of the server
     * @return true if the setting was successful, false otherwise
     */
    public boolean setReactionsSettings(ReactionsSettings settings) {
        if (isInDatabase(settings.getServerID())) {
            return updateReactionsSettings(settings);
        } else {
            return insertReactionsSettings(settings);
        }
    }

    /**
     * Checks whether a server has a record in the database
     *
     * @param serverID ID of the server to check for existence in the database
     * @return true if the server has a record in the database, false if it doesn't or if an error occurred
     */
    private boolean isInDatabase(String serverID) {
        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("SELECT COUNT(*) FROM reactions WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ReactionsDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates server's reactions settings record in the database
     *
     * @param settings {@link ReactionsSettings ReactionsSettings} to update the record to
     * @return true if the update was successful, false otherwise or if an error occurred
     */
    private boolean updateReactionsSettings(ReactionsSettings settings) {
        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("UPDATE reactions SET settings = ? WHERE serverid = ?");
            preparedStatement.setString(1, settings.getSettings().toString());
            preparedStatement.setString(2, settings.getServerID());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ReactionsDatabase.updateReactionsSettings()");
            return false;
        }
    }

    /**
     * Inserts server's reactions settings into the database
     *
     * @param settings {@link ReactionsSettings ReactionsSettings} to insert into the database
     * @return true if the insertion was successful, false otherwise or if an error occurred
     */
    private boolean insertReactionsSettings(ReactionsSettings settings) {
        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("INSERT INTO reactions(serverid, settings) VALUES(?, ?)");
            preparedStatement.setString(1, settings.getServerID());
            preparedStatement.setString(2, settings.getSettings().toString());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ReactionsDatabase.insertReactionsSettings()");
            return false;
        }
    }
}
