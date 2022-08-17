package com.birtek.cashew.timings;

import com.birtek.cashew.commands.Help;
import com.birtek.cashew.database.CommandsSettings;
import com.birtek.cashew.database.CommandsSettingsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manager for all server's {@link CommandsSettings CommandsSettings}
 */
public class CommandsSettingsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsSettingsManager.class);

    private final HashMap<String, CommandsSettings> settingsMap;

    /**
     * Creates the manager object, fetches all reactions and passes them to CommandsSettings,
     * and fetches all settings from the database. If the database returns an error, bot
     * shuts down
     */
    public CommandsSettingsManager() {
        CommandsSettings.setAllCommands(new ArrayList<>(Help.commands.subList(2, Help.commands.size() - 1)));
        CommandsSettingsDatabase database = CommandsSettingsDatabase.getInstance();
        settingsMap = database.getAllCommandsSettings();
        if (settingsMap == null) {
            LOGGER.error("Failed to obtain commands settings!");
            System.exit(1);
        }
    }

    /**
     * Gets the command settings for the given command in a certain channel in a certain server. If the
     * {@link CommandsSettings CommandsSettings} object for the server doesn't yet exist, it will be created but NOT
     * inserted into the database
     *
     * @param serverID  ID of the server to check the settings for
     * @param channelID ID of the channel to check the settings for
     * @param command   name of the command to check the settings for
     * @return command setting - true or false representing on and off
     */
    public boolean getCommandSettings(String serverID, String channelID, String command) {
        CommandsSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settingsMap.put(serverID, new CommandsSettings(serverID));
            return true;
        } else {
            return settings.getCommandSettings(command, channelID);
        }
    }

    /**
     * Sets the command settings for a certain command in a certain channel in a certain server. If the
     * {@link CommandsSettings CommandsSettings} object for the server doesn't yet exist, it will be created and
     * inserted into the database
     *
     * @param serverID  ID of the server to update the settings for
     * @param channelID ID of the channel to update the settings for
     * @param command   name of the command to update the settings for
     * @param state     new setting to set for the specified combination of options
     * @return true if the update was successful, false otherwise
     */
    public boolean updateCommandSettings(String serverID, String channelID, String command, boolean state) {
        CommandsSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settings = new CommandsSettings(serverID);
        }
        settings.setCommandSettings(command, channelID, state);
        CommandsSettingsDatabase database = CommandsSettingsDatabase.getInstance();
        if (!database.setCommandsSettings(settings)) return false;
        settingsMap.put(serverID, settings);
        return true;
    }
}
