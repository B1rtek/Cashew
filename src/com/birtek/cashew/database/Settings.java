package com.birtek.cashew.database;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class that stores settings for servers in JSON form
 */
public class Settings {
    protected static HashMap<Class<?>, ArrayList<String>> allOptions = new HashMap<>();
    private final String serverID;
    private final JSONObject settings;

    /**
     * Creates a Settings object from an existing JSON from the database, used by the
     * corresponding subclass' database
     *
     * @param jsonSettings a {@link JSONObject} with reactions settings for a server
     * @param serverID     ID of the server to which the settings belong
     */
    public Settings(JSONObject jsonSettings, String serverID) {
        this.settings = jsonSettings;
        this.serverID = serverID;
    }

    /**
     * Creates a new empty object with a JSON containing { "all": false } as settings
     *
     * @param serverID ID of the server to which these settings belong to
     */
    public Settings(String serverID) {
        this.settings = new JSONObject();
        this.settings.put("all", false);
        this.serverID = serverID;
    }

    protected void setAllOptions(ArrayList<String> options) {
        allOptions.put(this.getClass(), options);
    }

    /**
     * Retrieves the setting for an option from the JSON by first checking global setting, then the reaction and then the
     * channel specific one
     *
     * @param option    name of the option to check settings for
     * @param channelID ID of the channel to check settings for
     * @return boolean telling the state of the setting
     */
    public boolean getOptionStatus(String option, String channelID) {
        boolean result = false;
        // first check for a global setting
        if (settings.has("all")) {
            result = settings.getBoolean("all");
        }
        // next check for an option specific one
        if (settings.has(option)) {
            JSONObject optionObject = settings.getJSONObject(option);
            // check for all channels setting
            if (optionObject.has("all")) {
                result = optionObject.getBoolean("all");
            }
            // next check for a channel specific one
            if (optionObject.has(channelID)) {
                result = optionObject.getBoolean(channelID);
            }
        }
        return result;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public String getServerID() {
        return serverID;
    }

    /**
     * Sets the activity of a reaction to a certain state in the specified channel
     *
     * @param option    name of the reaction to change settings of, "all" for all
     * @param channelID ID of the channel to change the setting in, set to "all" to set it in all channels
     * @param state     new setting state - true is on, false is off
     */
    public void setOptionStatus(String option, String channelID, boolean state) {
        if (option.equals("all")) {
            if (!channelID.equals("all")) { // turning on or off all an option in a certain channel
                ArrayList<String> options = allOptions.get(this.getClass());
                for (String opt : options) {
                    if (getOptionStatus(opt, channelID) != state) {
                        JSONObject optionSettings;
                        if (!settings.has(opt)) {
                            optionSettings = new JSONObject();
                        } else {
                            optionSettings = settings.getJSONObject(opt);
                        }
                        optionSettings.put(channelID, state);
                        settings.put(opt, optionSettings);
                    }
                }
            } else { // setting all - remove all settings and replace them with { "all": <state> }
                Iterator<String> optionsKeys = settings.keys();
                ArrayList<String> keys = new ArrayList<>();
                while (optionsKeys.hasNext()) {
                    keys.add(optionsKeys.next());
                }
                for (String key : keys) {
                    settings.remove(key);
                }
                settings.put("all", state);
            }
        } else {
            JSONObject optionSettings;
            if (!settings.has(option)) {
                optionSettings = new JSONObject();
            } else {
                optionSettings = settings.getJSONObject(option);
            }
            if (channelID.equals("all")) { // setting for all channels - remove settings for single channels
                Iterator<String> channelsKeys = optionSettings.keys();
                ArrayList<String> keys = new ArrayList<>();
                while (channelsKeys.hasNext()) {
                    keys.add(channelsKeys.next());
                }
                for (String key : keys) {
                    optionSettings.remove(key);
                }
            }
            optionSettings.put(channelID, state);
            settings.put(option, optionSettings);
        }
    }
}
