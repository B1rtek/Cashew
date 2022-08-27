package com.birtek.cashew.database;

/**
 * Class that saves the information about a rule set up by server moderators
 * Rule types are as follows:
 * 1 - member joins the server
 * 2 - member leaves the server
 * 3 - member reacts to a message
 * 4 - member removes a reaction
 * 5 - member edits a message
 * 6 - member deletes a message
 * All types require their parameters - for example 3 and 4 require the sourceMessageID,
 * the correctness of data can be verified with {@link #verify() verify()}
 * There are following actions that can be performed:
 * 1 - send a message
 * 2 - add a role to the interacting user
 * 3 - remove a role from the interacting user
 * 4 - pass the information to setter's DMs
 */
public class WhenRule {

    final String serverID;
    final int ruleType;
    String sourceMessageID, sourceChannelID, sourceReaction, targetChannelID, targetMessageContent, targetRoleID, targetUserID;
    private int actionType;

    /**
     * Constructor that takes all input parameters. Actions to perform can be configured with dedicated methods
     *
     * @param serverID        ID of the server where the rule is being set up
     * @param ruleType        ID of the rule, types of rules are listed above
     * @param sourceMessageID (optional) source message matching the rule type, set to null if not needed
     * @param sourceChannelID (optional) source channel matching the rule type, set to null if not needed
     * @param sourceReaction  (optional) source reaction unicode string matching the rule type, set to null if not needed
     */
    public WhenRule(String serverID, int ruleType, String sourceMessageID, String sourceChannelID, String sourceReaction) {
        this.serverID = serverID;
        this.ruleType = ruleType;
        this.sourceMessageID = sourceMessageID;
        this.sourceChannelID = sourceChannelID;
        this.sourceReaction = sourceReaction;
    }

    public String getServerID() {
        return serverID;
    }

    public int getRuleType() {
        return ruleType;
    }

    public int getActionType() {
        return actionType;
    }

    public String getSourceMessageID() {
        return sourceMessageID;
    }

    public String getSourceChannelID() {
        return sourceChannelID;
    }

    public String getTargetChannelID() {
        return targetChannelID;
    }

    public String getTargetMessageContent() {
        return targetMessageContent;
    }

    public String getTargetRoleID() {
        return targetRoleID;
    }

    public String getTargetUserID() {
        return targetUserID;
    }

    public String getSourceReaction() {
        return sourceReaction;
    }

    /**
     * Verifies whether the rule has all input parameters set up correctly
     *
     * @return true if it's correct, false otherwise
     */
    boolean verify() {
        if (ruleType == 3 || ruleType == 4) {
            return sourceMessageID != null;
        }
        return true;
    }

    /**
     * Removes all action data
     */
    private void clearData() {
        targetChannelID = null;
        targetMessageContent = null;
        targetRoleID = null;
        targetUserID = null;
    }

    /**
     * Sets up the send message action (1)
     *
     * @param targetMessageContent content of the message that will be sent when triggered
     * @param targetChannelID      ID of the channel in which the message will be sent
     */
    public void sendMessage(String targetMessageContent, String targetChannelID) {
        clearData();
        this.targetMessageContent = targetMessageContent;
        this.targetChannelID = targetChannelID;
        actionType = 1;
    }

    /**
     * Sets up the add role action (2)
     *
     * @param targetRoleID ID of the role to add to the interacting user when triggered
     */
    public void addRole(String targetRoleID) {
        clearData();
        this.targetRoleID = targetRoleID;
        actionType = 2;
    }

    /**
     * Sets up the add role action (3)
     *
     * @param targetRoleID ID of the role to remove from the interacting user when triggered
     */
    public void removeRole(String targetRoleID) {
        clearData();
        this.targetRoleID = targetRoleID;
        actionType = 3;
    }

    /**
     * Sets up the pass to DM action (4)
     *
     * @param targetUserID ID of the user to whom the action will be passed
     */
    public void passToDM(String targetUserID) {
        clearData();
        this.targetUserID = targetUserID;
        actionType = 4;
    }
}
