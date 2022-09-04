package com.birtek.cashew.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WhenSettings {

    private final String serverID;
    private final JSONObject settings;

    private final int triggersCount = 6;

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

    /**
     * Adds a rule to the server rules JSON, each rule has an "act" property for the target action, and a combination
     * of following:
     * "scid" for source channel ID
     * "smid" for source message ID
     * "sreid" for source reaction ID
     * "tcid" for target channel ID
     * "tmc" for target message content
     * "trid" for target role ID
     * "tuid" for target user ID
     *
     * @param rule {@link WhenRule rule} to add to the settings JSON
     */
    public void addWhenRule(WhenRule rule) {
        JSONObject ruleObject = new JSONObject();
        ruleObject.put("act", rule.getActionType());
        if (rule.getSourceChannelID() != null) ruleObject.put("scid", rule.getSourceChannelID());
        if (rule.getSourceMessageID() != null) ruleObject.put("smid", rule.getSourceMessageID());
        if (rule.getSourceReaction() != null) ruleObject.put("sreid", rule.getSourceReaction());
        if (rule.getTargetChannelID() != null) ruleObject.put("tcid", rule.getTargetChannelID());
        if (rule.getTargetMessageContent() != null) ruleObject.put("tmc", rule.getTargetMessageContent());
        if (rule.getTargetRoleID() != null) ruleObject.put("trid", rule.getTargetRoleID());
        if (rule.getTargetUserID() != null) ruleObject.put("tuid", rule.getTargetUserID());
        JSONArray ruleObjects = new JSONArray();
        if (settings.has(String.valueOf(rule.getTriggerType()))) {
            ruleObjects = settings.getJSONArray(String.valueOf(rule.getTriggerType()));
        }
        ruleObjects.put(ruleObject);
        settings.put(String.valueOf(rule.getTriggerType()), ruleObjects);
    }

    /**
     * Gets all rules of the selected type from the JSON
     *
     * @param type int representing the rule type, rule types are listed in {@link WhenRule WhenRule}
     * @return ArrayList of {@link WhenRule WhenRules} of the selected type
     */
    public ArrayList<WhenRule> getRulesByType(int type) {
        ArrayList<WhenRule> rules = new ArrayList<>();
        if (!settings.has(String.valueOf(type))) return rules;
        JSONArray rulesObjects = settings.getJSONArray(String.valueOf(type));
        for (int i = 0; i < rulesObjects.length(); i++) {
            JSONObject rule = rulesObjects.getJSONObject(i);
            WhenRule whenRule = new WhenRule(serverID);
            switch (type) { // read trigger
                case 1 -> whenRule.memberJoinTrigger();
                case 2 -> whenRule.memberLeaveTrigger();
                case 3 -> {
                    String sourceMessageID = rule.getString("smid");
                    String sourceReaction = rule.getString("sreid");
                    whenRule.memberReactsTrigger(sourceMessageID, sourceReaction);
                }
                case 4 -> {
                    String sourceMessageID = rule.getString("smid");
                    String sourceReaction = rule.getString("sreid");
                    whenRule.memberRemovesReactionTrigger(sourceMessageID, sourceReaction);
                }
                case 5 -> {
                    String sourceChannelID = null;
                    if (rule.has("scid")) sourceChannelID = rule.getString("scid");
                    whenRule.memberEditsMessageTrigger(sourceChannelID);
                }
                case 6 -> {
                    String sourceChannelID = null;
                    if (rule.has("scid")) sourceChannelID = rule.getString("scid");
                    whenRule.memberDeletesMessageTrigger(sourceChannelID);
                }
            }
            switch (rule.getInt("act")) {
                case 1 -> {
                    String targetMessageContent = rule.getString("tmc");
                    String targetChannelID = rule.getString("tcid");
                    whenRule.sendMessageAction(targetMessageContent, targetChannelID);
                }
                case 2 -> {
                    String targetRoleID = rule.getString("trid");
                    whenRule.addRoleAction(targetRoleID);
                }
                case 3 -> {
                    String targetRoleID = rule.getString("trid");
                    whenRule.removeRoleAction(targetRoleID);
                }
                case 4 -> {
                    String targetUserID = rule.getString("tuid");
                    whenRule.passToDMAction(targetUserID);
                }
                case 5 -> {
                    String targetChannelID = rule.getString("tcid");
                    whenRule.passToChannel(targetChannelID);
                }
            }
            rules.add(whenRule);
        }
        return rules;
    }

    /**
     * Gets the requested page of the {@link WhenRule WhenRules}, each page consists of 10 entries
     *
     * @param pageNumber number of the page to obtain
     * @return an ArrayList of {@link WhenRule WhenRules} making up that page
     */
    public ArrayList<WhenRule> getRulesPage(int pageNumber) {
        int firstIndex = pageNumber * 10, currentIndex = 0;
        ArrayList<WhenRule> rulesPage = new ArrayList<>();
        for (int ruleType = 1; ruleType <= triggersCount; ruleType++) {
            if (!settings.has(String.valueOf(ruleType))) continue;
            int rulesTypeLength = settings.getJSONArray(String.valueOf(ruleType)).length();
            if (currentIndex < firstIndex && currentIndex + rulesTypeLength > firstIndex) {
                int insideIndex = firstIndex - currentIndex;
                ArrayList<WhenRule> rulesOfType = getRulesByType(ruleType);
                while (insideIndex < rulesTypeLength && rulesPage.size() < 10) {
                    rulesPage.add(rulesOfType.get(insideIndex));
                }
                currentIndex += rulesTypeLength;
            } else if (currentIndex < firstIndex + 10) {
                ArrayList<WhenRule> rulesOfType = getRulesByType(ruleType);
                for (int insideIndex = 0; insideIndex < rulesTypeLength && rulesPage.size() < 10; insideIndex++) {
                    rulesPage.add(rulesOfType.get(insideIndex));
                }
                currentIndex += rulesTypeLength;
            } else {
                break;
            }
        }
        return rulesPage;
    }

    /**
     * Gets the number of pages (each containing max. 10 entries) of when rules
     *
     * @return the number of pages
     */
    public int getRulesPageCount() {
        int total = 0;
        for (int ruleType = 1; ruleType <= triggersCount; ruleType++) {
            if (!settings.has(String.valueOf(ruleType))) continue;
            total += settings.getJSONArray(String.valueOf(ruleType)).length();
        }
        return total / 10 + (total % 10 == 0 ? 0 : 1);
    }

    /**
     * Gets a rule by its index on the rules list
     *
     * @param index index of the rule, counting from 0
     * @return a WhenRule with a matching index, or null if it doesn't exist
     */
    public WhenRule getRuleByIndex(int index) {
        int currentIndex = 0;
        for (int ruleType = 0; ruleType <= triggersCount; ruleType++) {
            if (!settings.has(String.valueOf(ruleType))) continue;
            int rulesTypeLength = settings.getJSONArray(String.valueOf(ruleType)).length();
            if (currentIndex + rulesTypeLength > index) {
                int insideIndex = index - currentIndex;
                ArrayList<WhenRule> rulesOfType = getRulesByType(ruleType);
                return rulesOfType.get(insideIndex);
            }
            currentIndex += rulesTypeLength;
        }
        return null;
    }

    /**
     * Removes a rule with the given index from the rules JSON
     *
     * @param index index of the rule to remove, 0 to remove all of them
     * @return true if the removal was successful, false otherwise (when the index was invalid)
     */
    public boolean removeRuleByIndex(int index) {
        if (index == 0) {
            this.settings.clear();
            return true;
        }
        int currentIndex = 0;
        for (int ruleType = 0; ruleType <= triggersCount; ruleType++) {
            if (!settings.has(String.valueOf(ruleType))) continue;
            JSONArray rulesOfType = settings.getJSONArray(String.valueOf(ruleType));
            int rulesTypeLength = rulesOfType.length();
            if (currentIndex + rulesTypeLength > index) {
                int insideIndex = index - currentIndex;
                rulesOfType.remove(insideIndex);
                settings.put(String.valueOf(ruleType), rulesOfType);
                return true;
            }
            currentIndex += rulesTypeLength;
        }
        return false;
    }
}
