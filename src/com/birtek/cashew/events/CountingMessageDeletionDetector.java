package com.birtek.cashew.events;

import com.birtek.cashew.Database;
import com.birtek.cashew.messagereactions.CountingInfo;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CountingMessageDeletionDetector extends ListenerAdapter {
    @Override public void onMessageDelete(MessageDeleteEvent event) {
        Database database = Database.getInstance();
        String channelID = event.getChannel().getId();
        CountingInfo info = database.getCountingData(channelID);
        if (info.getActive() && info.getMessageID().equals(event.getMessageId())) {
            String userName = "<@!" + info.getUserID() + ">";
            Objects.requireNonNull(event.getJDA().getTextChannelById(channelID)).sendMessage(userName + " deleted their message with a count of ` " + info.getValue() + " `! The next number is ` "+(info.getValue()+1)+" `!").queue();
        }
    }
}
