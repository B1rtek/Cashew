package com.birtek.cashew.timings;

import com.birtek.cashew.database.WhenRule;
import com.birtek.cashew.database.WhenSettings;
import com.birtek.cashew.database.WhenSettingsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manager for all servers' {@link com.birtek.cashew.database.WhenSettings WhenSettings}
 */
public class WhenSettingsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhenSettingsManager.class);

    private final HashMap<String, WhenSettings> settingsMap;

    /**
     * Creates the manager object and fetches all settings from the database. If the database returns an error, the bot
     * shuts down after the error is logged
     */
    public WhenSettingsManager() {
        WhenSettingsDatabase database = WhenSettingsDatabase.getInstance();
        settingsMap = database.getAllWhenSettings();
        if (settingsMap == null) {
            LOGGER.error("Failed to obtain WhenSettings!");
            System.exit(1);
        }
    }

    /**
     * Gets all rules of the selected type from the settings map
     *
     * @param serverID    ID of the server where the rules were requested
     * @param triggerType type of the rule of which the rules were requested
     * @return ArrayList of {@link WhenRule WhenRules} matching the type or null if an error occurred
     */
    public ArrayList<WhenRule> getRulesOfTriggerType(String serverID, int triggerType) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settingsMap.put(serverID, new WhenSettings(serverID));
            return new ArrayList<>();
        } else {
            return settings.getRulesByType(triggerType);
        }
    }

    /**
     * Gets all rules from a server
     *
     * @param serverID ID of the server to get all rules from
     * @return ArrayList of {@link WhenRule WhenRules} containing all rules from the server or null if an error occurred
     */
    public ArrayList<WhenRule> getAllRulesFromServer(String serverID) {
        ArrayList<WhenRule> allRules = getRulesOfTriggerType(serverID, 1);
        if(allRules == null) return null;
        for(int i=2; i<=WhenSettings.triggersCount; i++) {
            ArrayList<WhenRule> rulesOfType = getRulesOfTriggerType(serverID, i);
            if(rulesOfType == null) return null;
            allRules.addAll(rulesOfType);
        }
        return allRules;
    }

    /**
     * Gets the selected page of rules from the settings map
     *
     * @param serverID   ID of the server where the rules were requested
     * @param pageNumber number of the page that was requested
     * @return ArrayList of {@link WhenRule WhenRules} representing the requested page or null if an error occurred
     */
    public ArrayList<WhenRule> getWhenRulesPage(String serverID, int pageNumber) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settingsMap.put(serverID, new WhenSettings(serverID));
            return new ArrayList<>();
        } else {
            return settings.getRulesPage(pageNumber);
        }
    }

    /**
     * Gets the number of pages of when rules on the server
     *
     * @param serverID ID of the server to get the number of pages of
     * @return the number of pages of when rules on the server
     */
    public int getWhenRulesPageCount(String serverID) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) return 0;
        return settings.getRulesPageCount();
    }

    /**
     * Gets the rule with the selected index from the server settings
     *
     * @param serverID ID of the server where the rule was requested
     * @param index    index of the requested rule
     * @return {@link WhenRule WhenRule} of the matching index, or null if it doesn't exist
     */
    public WhenRule getRuleByIndex(String serverID, int index) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settingsMap.put(serverID, new WhenSettings(serverID));
            return null;
        } else {
            return settings.getRuleByIndex(index);
        }
    }

    /**
     * Updates a server's settings record in the database and in the manager
     *
     * @param serverID ID of the server which settings are being saved
     * @param settings settings to save
     * @return true if the saving was successful, false otherwise
     */
    private boolean saveServerSettings(String serverID, WhenSettings settings) {
        WhenSettingsDatabase database = WhenSettingsDatabase.getInstance();
        if (!database.setWhenSettings(settings)) return false;
        settingsMap.put(serverID, settings);
        return true;
    }

    /**
     * Updates the server's settings and saves the changes to the database
     *
     * @param serverID ID of the server where the rule has been added
     * @param rule     {@link WhenRule WhenRule} to add
     * @return true if the addition was successful, false otherwise
     */
    public boolean addWhenRule(String serverID, WhenRule rule) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) {
            settings = new WhenSettings(serverID);
        }
        settings.addWhenRule(rule);
        return saveServerSettings(serverID, settings);
    }

    /**
     * Removes a rule from server's settings and saves the changes to the database
     *
     * @param serverID ID of the server where the rule has been added
     * @param index    index of the rule to remove, starting from 1
     * @return true if the deletion was successful, false otherwise
     */
    public boolean removeWhenRuleByIndex(String serverID, int index) {
        WhenSettings settings = settingsMap.get(serverID);
        if (settings == null) return false;
        if (!settings.removeRuleByIndex(index)) return false;
        return saveServerSettings(serverID, settings);
    }
}
