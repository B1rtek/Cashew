package com.birtek.cashew.database;

import org.json.JSONObject;

import java.util.ArrayList;

public class CommandsSettings extends Settings {

    /**
     * Creates a CommandsSettings object from an existing JSON from the database, used by the
     * {@link CommandsSettingsDatabase CommandsDatabase}
     *
     * @param jsonSettings a {@link JSONObject} with reactions settings for a server
     * @param serverID     ID of the server to which the settings belong
     */
    public CommandsSettings(JSONObject jsonSettings, String serverID) {
        super(jsonSettings, serverID);
    }

    /**
     * Creates a new empty object with a JSON containing { "all": true } as settings
     *
     * @param serverID ID of the server to which these settings belong to
     */
    public CommandsSettings(String serverID) {
        super(serverID, true);
    }

    public static void setAllCommands(ArrayList<String> commands) {
        setAllOptions(CommandsSettings.class, commands);
    }

    /**
     * Retrieves the command setting from the JSON by first checking global setting, then the reaction and then the
     * channel specific one
     *
     * @param command   command to check settings for
     * @param channelID ID of the channel to check settings for
     * @return boolean telling whether the command is turned on or off in that channel
     */
    public boolean getCommandSettings(String command, String channelID) {
        return getOptionStatus(command, channelID);
    }

    /**
     * Sets the command on or off in the specified channel
     *
     * @param command   command to change settings of, "all" all
     * @param channelID ID of the channel to change the setting in, set to "all" to set it in all channels
     * @param state     new settings state - true is on, false is off
     */
    public void setCommandSettings(String command, String channelID, boolean state) {
        setOptionStatus(command, channelID, state);
    }

}
