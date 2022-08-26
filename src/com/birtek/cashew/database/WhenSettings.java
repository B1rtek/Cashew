package com.birtek.cashew.database;

import org.json.JSONObject;

public class WhenSettings {

    private final String serverID;
    private final JSONObject settings;

    public WhenSettings(JSONObject jsonSettings, String serverID) {
        this.settings = jsonSettings;
        this.serverID = serverID;
    }

    public WhenSettings(String serverID) {
        this.settings = new JSONObject();
        this.serverID = serverID;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public String getServerID() {
        return serverID;
    }
}
