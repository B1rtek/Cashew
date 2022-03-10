package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseReaction extends ListenerAdapter {

    public boolean checkIfNotBot(MessageReceivedEvent event) {
        return !event.getAuthor().isBot();
    }

    public boolean checkActivitySettings(MessageReceivedEvent event, int requiredActivity) {
        String channelID = event.getChannel().getId();
        int activityPermission = 0;
        try {
            Database database = Database.getInstance();
            ResultSet results = database.channelActivitySelect(channelID);
            if(results!=null) {
                while(results.next()) {
                    activityPermission = results.getInt("activity");
                }
            } else {
                database.channelActivityInsert(channelID, 0);
            }
            return activityPermission >= requiredActivity;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}