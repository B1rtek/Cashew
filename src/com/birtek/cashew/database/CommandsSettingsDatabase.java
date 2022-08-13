package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;

public class CommandsSettingsDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsSettingsDatabase.class);

    private static volatile CommandsSettingsDatabase instance;

    private Connection commandsSettingsConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * cmdsettings table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private CommandsSettingsDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            commandsSettingsConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = commandsSettingsConnection.prepareStatement("CREATE TABLE IF NOT EXISTS cmdsettings(serverid TEXT, settings TEXT)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the cmdsettings table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static CommandsSettingsDatabase getInstance() {
        CommandsSettingsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (CommandsSettingsDatabase.class) {
            if (instance == null) {
                instance = new CommandsSettingsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets all commands settings from the database and returns them in a HashMap mapping the server ID to the settings
     *
     * @return HashMap with all settings from the database or null if an error occurred
     */
    public HashMap<String, CommandsSettings> getAllCommandsSettings() {
        try {
            PreparedStatement preparedStatement = commandsSettingsConnection.prepareStatement("SELECT * FROM cmdsettings");
            ResultSet results = preparedStatement.executeQuery();
            HashMap<String, CommandsSettings> commandsHashMap = new HashMap<>();
            while(results.next()) {
                commandsHashMap.put(results.getString(1), new CommandsSettings(results.getString(2)));
            }
            return commandsHashMap;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CommandsSettingsDatabase.getAllCommandsSettings()");
            return null;
        }
    }

    /**
     * Sets new reactions settings for a server
     *
     * @param settings {@link CommandsSettings CommandsSettings} object with settings of the server
     * @return true if the setting was successful, false otherwise
     */
    public boolean setCommandsSettings(CommandsSettings settings) {
        if (isInDatabase(settings.getServerID())) {
            return updateCommandsSettings(settings);
        } else {
            return insertCommandsSettings(settings);
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
            PreparedStatement preparedStatement = commandsSettingsConnection.prepareStatement("SELECT COUNT(*) FROM cmdsettings WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CommandsSettingsDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates server's commands settings record in the database
     *
     * @param settings {@link CommandsSettings CommandsSettings} to update the record to
     * @return true if the update was successful, false otherwise or if an error occurred
     */
    private boolean updateCommandsSettings(CommandsSettings settings) {
        try {
            PreparedStatement preparedStatement = commandsSettingsConnection.prepareStatement("UPDATE cmdsettings SET settings = ? WHERE serverid = ?");
            preparedStatement.setString(1, settings.getSettings().toString());
            preparedStatement.setString(2, settings.getServerID());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CommandsSettingsDatabase.updateCommandsSettings()");
            return false;
        }
    }

    /**
     * Inserts server's commands settings into the database
     *
     * @param settings {@link CommandsSettings CommandsSettings} to insert into the database
     * @return true if the insertion was successful, false otherwise or if an error occurred
     */
    private boolean insertCommandsSettings(CommandsSettings settings) {
        try {
            PreparedStatement preparedStatement = commandsSettingsConnection.prepareStatement("INSERT INTO cmdsettings(serverid, settings) VALUES(?, ?)");
            preparedStatement.setString(1, settings.getServerID());
            preparedStatement.setString(2, settings.getSettings().toString());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at CommandsSettingsDatabase.insertCommandsSettings()");
            return false;
        }
    }
}
