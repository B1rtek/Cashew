package com.birtek.cashew.database;

import org.json.JSONObject;

/**
 * Class that stores message reactions settings
 */
public class ReactionsSettings {

    private final String serverID;
    private JSONObject settings;

    ReactionsSettings(JSONObject jsonSettings, String serverID) {
        this.settings = jsonSettings;
        this.serverID = serverID;
    }

    public boolean setActivity(int reactionID, String channelID) {
        return false;
    }

    public boolean getActivity(int reaction) {
        return false;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public String getServerID() {
        return serverID;
    }
}
