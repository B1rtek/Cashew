package com.birtek.cashew.reactions;

import com.birtek.cashew.database.CountingDatabase;
import com.birtek.cashew.database.CountingInfo;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CountingMessageDeletionDetector extends ListenerAdapter {
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        CountingDatabase database = CountingDatabase.getInstance();
        String channelID = event.getChannel().getId();
        CountingInfo info = database.getCountingData(channelID);
        if (info != null && info.active() && info.messageID().equals(event.getMessageId())) {
            String userName = "<@!" + info.userID() + ">";
            Objects.requireNonNull(event.getJDA().getTextChannelById(channelID)).sendMessage(userName + " deleted their message with a count of ` " + info.value() + " `! The next number is ` "+(info.value()+1)+" `!").queue();
        }
    }
}
