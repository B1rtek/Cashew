package com.birtek.cashew.timings;

import com.birtek.cashew.database.Reaction;
import com.birtek.cashew.database.ReactionsDatabase;
import com.birtek.cashew.database.ReactionsSettings;
import com.birtek.cashew.database.ReactionsSettingsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manager for all server's {@link ReactionsSettings ReactionsSettings}
 */
public class ReactionsSettingsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionsSettingsManager.class);

    private final HashMap<String, ReactionsSettings> settingsMap;

    /**
     * Creates the manager object, fetches all reactions and passes them to ReactionsSettings,
     * and fetches all settings from the database. If the database returns an error, bot
     * shuts down
     */
    public ReactionsSettingsManager() {
        ReactionsDatabase rDatabase = ReactionsDatabase.getInstance();
        ArrayList<Reaction> allReactions = rDatabase.getAllReactions();
        if (allReactions == null) {
            LOGGER.error("Failed to obtain reactions!");
            System.exit(1);
        }
        ReactionsSettings.setAllReactions(allReactions);
        ReactionsSettingsDatabase database = ReactionsSettingsDatabase.getInstance();
        settingsMap = database.getAllReactionsSettings();
        if (settingsMap == null) {
            LOGGER.error("Failed to obtain reactions settings!");
            System.exit(1);
        }
    }

    /**
     * Gets the activity settings for a certain reaction in a certain channel in a certain server. If the
     * {@link ReactionsSettings ReactionsSettings} object for the server doesn't yet exist, it will be created but NOT
     * inserted into the database
     *
     * @param serverID   ID of the server to check the settings for
     * @param channelID  ID of the channel to check the settings for
     * @param reactionID ID of the reaction to check the settings for
     * @return activity setting for the channel - either true or false
     */
    public boolean getActivitySettings(String serverID, String channelID, int reactionID) {
        ReactionsSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settingsMap.put(serverID, new ReactionsSettings(serverID));
            return false;
        } else {
            return settings.getActivity(reactionID, channelID);
        }
    }

    /**
     * Sets the activity settings for a certain reaction in a certain channel in a certain server. If the
     * {@link ReactionsSettings ReactionsSettings} object for the server doesn't yet exist, it will be created and
     * inserted into the database
     *
     * @param serverID   ID of the server to check the settings for
     * @param channelID  ID of the channel to check the settings for
     * @param reactionID ID of the reaction to check the settings for
     * @param state      new setting to set for the specified combination of options
     * @return activity setting for the channel - either true or false
     */
    public boolean updateActivitySettings(String serverID, String channelID, int reactionID, boolean state) {
        ReactionsSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settings = new ReactionsSettings(serverID);
        }
        settings.setActivity(reactionID, channelID, state);
        ReactionsSettingsDatabase database = ReactionsSettingsDatabase.getInstance();
        if (!database.setReactionsSettings(settings)) return false;
        settingsMap.put(serverID, settings);
        return true;
    }
}
