package com.birtek.cashew.reactions;

import com.birtek.cashew.database.ChannelActivityDatabase;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BaseReaction extends ListenerAdapter {

    public boolean checkIfNotBot(MessageReceivedEvent event) {
        return !event.getAuthor().isBot();
    }

    public boolean checkActivitySettings(MessageReceivedEvent event, int requiredActivity) {
        String channelID = event.getChannel().getId();
        int activityPermission;
        ChannelActivityDatabase database = ChannelActivityDatabase.getInstance();
        activityPermission = database.getChannelActivity(channelID);
        return activityPermission >= requiredActivity;
    }
}