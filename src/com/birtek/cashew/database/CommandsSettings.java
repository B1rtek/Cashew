package com.birtek.cashew.database;

import org.json.JSONObject;

import java.util.ArrayList;

public class CommandsSettings {

    private static ArrayList<String> allCommands;
    private final String serverID;
    private final JSONObject settings;

    /**
     * Creates a CommandsSettings object from an existing JSON from the database, used by the
     * {@link CommandsSettingsDatabase CommandsSettingsDatabase}
     *
     * @param jsonSettings a {@link JSONObject} with reactions settings for a server
     * @param serverID     ID of the server to which the settings belong
     */
    public CommandsSettings(JSONObject jsonSettings, String serverID) {
        this.settings = jsonSettings;
        this.serverID = serverID;
    }

    /**
     * Creates a new empty object with a JSON containing { "all": false } as settings
     *
     * @param serverID ID of the server to which these settings belong to
     */
    public CommandsSettings(String serverID) {
        this.settings = new JSONObject();
        this.settings.put("all", false);
        this.serverID = serverID;
    }

    public static void setAllCommands(ArrayList<String> commands) {
        allCommands = commands;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public String getServerID() {
        return serverID;
    }

}
