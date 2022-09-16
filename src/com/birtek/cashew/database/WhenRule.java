package com.birtek.cashew.database;

import java.util.Objects;

/**
 * Class that saves the information about a rule set up by server moderators
 * Rule types are as follows:
 * 1 - member joins the server
 * 2 - member leaves the server
 * 3 - member reacts to a message
 * 4 - member removes a reaction
 * 5 - member edits a message
 * 6 - member deletes a message
 * There are following actions that can be performed:
 * 1 - send a message
 * 2 - add a role to the interacting user
 * 3 - remove a role from the interacting user
 * 4 - pass the information to setter's DMs
 * 5 - pass the information to a channel
 */
public class WhenRule {
    final String serverID;
    private String sourceMessageID, sourceChannelID, sourceReaction, targetChannelID, targetMessageContent, targetRoleID, targetUserID;
    private int triggerType, actionType;

    /**
     * Creates the
     *
     * @param serverID ID of the server where the rule is being set up
     */
    public WhenRule(String serverID) {
        this.serverID = serverID;
    }

    public String getServerID() {
        return serverID;
    }

    public int getTriggerType() {
        return triggerType;
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
     * Removes all trigger data
     */
    private void clearTrigger() {
        sourceReaction = null;
        sourceChannelID = null;
        sourceMessageID = null;
    }

    /**
     * Removes all action data
     */
    private void clearAction() {
        targetChannelID = null;
        targetMessageContent = null;
        targetRoleID = null;
        targetUserID = null;
    }

    /**
     * Sets up the member join trigger (1)
     */
    public void memberJoinTrigger() {
        clearTrigger();
        triggerType = 1;
    }

    /**
     * Sets up the member leave trigger (2)
     */
    public void memberLeaveTrigger() {
        clearTrigger();
        triggerType = 2;
    }

    /**
     * Sets up the member reaction to a message trigger (3)
     *
     * @param sourceMessageID ID of the message to which reacting will trigger the action, cannot be null
     * @param sourceReaction  unicode string representing the reaction emoji, cannot be null
     */
    public void memberReactsTrigger(String sourceMessageID, String sourceReaction) {
        clearTrigger();
        triggerType = 3;
        this.sourceMessageID = sourceMessageID;
        this.sourceReaction = sourceReaction;
    }

    /**
     * Sets up the member reaction removal trigger (4)
     *
     * @param sourceMessageID ID of the message from which removing a reaction will trigger the action, cannot be null
     * @param sourceReaction  unicode string representing the reaction emoji, cannot be null
     */
    public void memberRemovesReactionTrigger(String sourceMessageID, String sourceReaction) {
        clearTrigger();
        triggerType = 4;
        this.sourceMessageID = sourceMessageID;
        this.sourceReaction = sourceReaction;
    }

    /**
     * Sets up the member edits a message trigger (5)
     *
     * @param sourceChannelID channel in which editing a message triggers the action, can be null for server-wide detection
     */
    public void memberEditsMessageTrigger(String sourceChannelID) {
        clearTrigger();
        triggerType = 5;
        this.sourceChannelID = sourceChannelID;
    }

    /**
     * Sets up the member deletes a message trigger (6)
     *
     * @param sourceChannelID channel in which removing a message triggers the action, can be null for server-wide detection
     */
    public void memberDeletesMessageTrigger(String sourceChannelID) {
        clearTrigger();
        triggerType = 6;
        this.sourceChannelID = sourceChannelID;
    }

    /**
     * Sets up the send message action (1)
     *
     * @param targetMessageContent content of the message that will be sent when triggered
     * @param targetChannelID      ID of the channel in which the message will be sent
     */
    public void sendMessageAction(String targetMessageContent, String targetChannelID) {
        clearAction();
        this.targetMessageContent = targetMessageContent;
        this.targetChannelID = targetChannelID;
        actionType = 1;
    }

    /**
     * Sets up the add role action (2)
     *
     * @param targetRoleID ID of the role to add to the interacting user when triggered
     */
    public void addRoleAction(String targetRoleID) {
        clearAction();
        this.targetRoleID = targetRoleID;
        actionType = 2;
    }

    /**
     * Sets up the remove role action (3)
     *
     * @param targetRoleID ID of the role to remove from the interacting user when triggered
     */
    public void removeRoleAction(String targetRoleID) {
        clearAction();
        this.targetRoleID = targetRoleID;
        actionType = 3;
    }

    /**
     * Sets up the pass to DM action (4)
     *
     * @param targetUserID ID of the user to whom the action will be passed
     */
    public void passToDMAction(String targetUserID) {
        clearAction();
        this.targetUserID = targetUserID;
        actionType = 4;
    }

    /**
     * Sets up the pass to channel action (5)
     *
     * @param targetChannelID ID of the channel to pass the action to
     */
    public void passToChannel(String targetChannelID) {
        clearAction();
        this.targetChannelID = targetChannelID;
        actionType = 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhenRule whenRule = (WhenRule) o;
        return triggerType == whenRule.triggerType && actionType == whenRule.actionType && Objects.equals(serverID, whenRule.serverID) && Objects.equals(sourceMessageID, whenRule.sourceMessageID) && Objects.equals(sourceChannelID, whenRule.sourceChannelID) && Objects.equals(sourceReaction, whenRule.sourceReaction) && Objects.equals(targetChannelID, whenRule.targetChannelID) && Objects.equals(targetMessageContent, whenRule.targetMessageContent) && Objects.equals(targetRoleID, whenRule.targetRoleID) && Objects.equals(targetUserID, whenRule.targetUserID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverID, sourceMessageID, sourceChannelID, sourceReaction, targetChannelID, targetMessageContent, targetRoleID, targetUserID, triggerType, actionType);
    }
}
