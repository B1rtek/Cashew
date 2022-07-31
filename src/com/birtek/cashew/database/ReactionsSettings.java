package com.birtek.cashew.database;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class that stores message reactions settings and updates their JSONs
 */
public class ReactionsSettings {

    private final String serverID;
    private final JSONObject settings;

    private static ArrayList<Reaction> allReactions;

    /**
     * Creates a ReactionsSettings object from an existing JSON from the database, used by the
     * {@link ReactionsSettingsDatabase ReactionsDatabase}
     *
     * @param jsonSettings a {@link JSONObject} with reactions settings for a server
     * @param serverID     ID of the server to which the settings belong
     */
    public ReactionsSettings(JSONObject jsonSettings, String serverID) {
        this.settings = jsonSettings;
        this.serverID = serverID;
    }

    /**
     * Creates a new empty object with a JSON containing { "all": false } as settings
     *
     * @param serverID ID of the server to which these settings belong to
     */
    public ReactionsSettings(String serverID) {
        this.settings = new JSONObject();
        this.settings.put("all", false);
        this.serverID = serverID;
    }

    public static void setAllReactions(ArrayList<Reaction> reactions) {
        allReactions = reactions;
    }

    /**
     * Retrieves the activity setting from the JSON by first checking global setting, then the reaction and then the
     * channel specific one
     *
     * @param reactionID ID of the reaction to check settings for
     * @param channelID  ID of the channel to check settings for
     * @return boolean telling whether the bot should react in the channel or not
     */
    public boolean getActivity(int reactionID, String channelID) {
        boolean result = false;
        // first check for a global setting
        if (settings.has("all")) {
            result = settings.getBoolean("all");
        }
        // next check for a reaction specific one
        String srID = String.valueOf(reactionID);
        if (settings.has(srID)) {
            JSONObject reactionObject = settings.getJSONObject(srID);
            // check for all channels setting
            if (reactionObject.has("all")) {
                result = reactionObject.getBoolean("all");
            }
            // next check for a channel specific one
            if (reactionObject.has(channelID)) {
                result = reactionObject.getBoolean(channelID);
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
     * @param reactionID ID of the reaction to change settings of, 0 for all
     * @param channelID  ID of the channel to change the setting in, set to "all" to set it in all channels
     * @param state      new settings state - true is on, false is off
     */
    public void setActivity(int reactionID, String channelID, boolean state) {
        String srID = String.valueOf(reactionID);
        if (reactionID == 0) {
            if(!channelID.equals("all")) { // turning on or off all reactions in a certain channel
                for(Reaction reaction: allReactions) {
                    if(getActivity(reaction.id(), channelID) != state) {
                        JSONObject reactionSettings;
                        if(!settings.has(String.valueOf(reaction.id()))) {
                            reactionSettings = new JSONObject();
                        } else {
                            reactionSettings = settings.getJSONObject(String.valueOf(reaction.id()));
                        }
                        reactionSettings.put(channelID, state);
                        settings.put(String.valueOf(reaction.id()), reactionSettings);
                    }
                }
            } else { // setting all - remove all settings and replace them with { "all": <state> }
                Iterator<String> reactionsKeys = settings.keys();
                ArrayList<String> keys = new ArrayList<>();
                while (reactionsKeys.hasNext()) {
                    keys.add(reactionsKeys.next());
                }
                for(String key: keys) {
                    settings.remove(key);
                }
                settings.put("all", state);
            }
        } else {
            JSONObject reactionObject;
            if (!settings.has(srID)) {
                reactionObject = new JSONObject();
            } else {
                reactionObject = settings.getJSONObject(srID);
            }
            if (channelID.equals("all")) { // setting for all channels - remove settings for single channels
                Iterator<String> channelsKeys = reactionObject.keys();
                ArrayList<String> keys = new ArrayList<>();
                while (channelsKeys.hasNext()) {
                    keys.add(channelsKeys.next());
                }
                for(String key: keys) {
                    reactionObject.remove(key);
                }
            }
            reactionObject.put(channelID, state);
            settings.put(srID, reactionObject);
        }
    }
}
