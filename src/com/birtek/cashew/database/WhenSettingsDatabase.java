package com.birtek.cashew.database;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class WhenSettingsDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhenSettingsDatabase.class);

    private static volatile WhenSettingsDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * whensettings table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private WhenSettingsDatabase() {
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS whensettings(serverid TEXT, settings TEXT)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the whensettings table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static WhenSettingsDatabase getInstance() {
        WhenSettingsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (WhenSettingsDatabase.class) {
            if (instance == null) {
                instance = new WhenSettingsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets all when rules settings from the database and returns them in a HashMap mapping the server ID to the settings
     *
     * @return HashMap with all settings from the database or null if an error occurred
     */
    public HashMap<String, WhenSettings> getAllWhenSettings() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM whensettings");
            ResultSet results = preparedStatement.executeQuery();
            HashMap<String, WhenSettings> settingsHashMap = new HashMap<>();
            while (results.next()) {
                settingsHashMap.put(results.getString(1), new WhenSettings(new JSONObject(results.getString(2)), results.getString(1)));
            }
            return settingsHashMap;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at WhenSettingsDatabase.getAllWhenSettings()");
            return null;
        }
    }

    /**
     * Sets new when rules settings for a server
     *
     * @param settings {@link WhenSettings WhenSettings} object with settings of the server
     * @return true if the setting was successful, false otherwise
     */
    public boolean setWhenSettings(WhenSettings settings) {
        if (isInDatabase(settings.getServerID())) {
            return updateWhenSettings(settings);
        } else {
            return insertWhenSettings(settings);
        }
    }

    /**
     * Checks whether a server has a when rules settings record in the database
     *
     * @param serverID ID of the server to check for existence in the database
     * @return true if the server has a record in the database, false if it doesn't or if an error occurred
     */
    private boolean isInDatabase(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM whensettings WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at WhenSettingsDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates server's when rules settings record in the database
     *
     * @param settings {@link WhenSettings WhenSettings} to update the record to
     * @return true if the update was successful, false otherwise or if an error occurred
     */
    private boolean updateWhenSettings(WhenSettings settings) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE whensettings SET settings = ? WHERE serverid = ?");
            preparedStatement.setString(1, settings.getSettings().toString());
            preparedStatement.setString(2, settings.getServerID());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at WhenSettingsDatabase.updateWhenSettings()");
            return false;
        }
    }

    /**
     * Inserts server's when rules settings into the database
     *
     * @param settings {@link WhenSettings WhenSettings} to insert into the database
     * @return true if the insertion was successful, false otherwise or if an error occurred
     */
    private boolean insertWhenSettings(WhenSettings settings) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO whensettings(serverid, settings) VALUES(?, ?)");
            preparedStatement.setString(1, settings.getServerID());
            preparedStatement.setString(2, settings.getSettings().toString());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at WhenSettingsDatabase.insertWhenSettings()");
            return false;
        }
    }

}
