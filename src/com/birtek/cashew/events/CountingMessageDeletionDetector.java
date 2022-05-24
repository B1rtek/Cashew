package com.birtek.cashew.events;

import com.birtek.cashew.Database;
import com.birtek.cashew.messagereactions.CountingInfo;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CountingMessageDeletionDetector extends ListenerAdapter {
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Database database = Database.getInstance();
        String channelID = event.getChannel().getId();
        CountingInfo info = database.getCountingData(channelID);
        if (info.active() && info.messageID().equals(event.getMessageId())) {
            String userName = "<@!" + info.userID() + ">";
            Objects.requireNonNull(event.getJDA().getTextChannelById(channelID)).sendMessage(userName + " deleted their message with a count of ` " + info.value() + " `! The next number is ` "+(info.value()+1)+" `!").queue();
        }
    }
}
