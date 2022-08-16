package com.birtek.cashew.database;

import org.json.JSONObject;

import java.util.ArrayList;

public class ReactionsSettings extends Settings {

    /**
     * Creates a ReactionsSettings object from an existing JSON from the database, used by the
     * {@link ReactionsSettingsDatabase ReactionsDatabase}
     *
     * @param jsonSettings a {@link JSONObject} with reactions settings for a server
     * @param serverID     ID of the server to which the settings belong
     */
    public ReactionsSettings(JSONObject jsonSettings, String serverID) {
        super(jsonSettings, serverID);
    }

    /**
     * Creates a new empty object with a JSON containing { "all": false } as settings
     *
     * @param serverID ID of the server to which these settings belong to
     */
    public ReactionsSettings(String serverID) {
        super(serverID);
    }

    public static void setAllReactions(ArrayList<Reaction> reactions) {
        ArrayList<String> allReactionsIDs = new ArrayList<>();
        for (Reaction reaction : reactions) {
            allReactionsIDs.add(String.valueOf(reaction.id()));
        }
        setAllOptions(ReactionsSettings.class, allReactionsIDs);
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
        String reaction = reactionID == 0 ? "all" : String.valueOf(reactionID);
        return getOptionStatus(reaction, channelID);
    }

    /**
     * Sets the activity of a reaction to a certain state in the specified channel
     *
     * @param reactionID ID of the reaction to change settings of, 0 for all
     * @param channelID  ID of the channel to change the setting in, set to "all" to set it in all channels
     * @param state      new settings state - true is on, false is off
     */
    public void setActivity(int reactionID, String channelID, boolean state) {
        String reaction = reactionID == 0 ? "all" : String.valueOf(reactionID);
        setOptionStatus(reaction, channelID, state);
    }
}
