package com.birtek.cashew.events;

import com.birtek.cashew.Database;
import com.birtek.cashew.messagereactions.CountingInfo;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CountingMessageModificationDetector extends ListenerAdapter {
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        Database database = Database.getInstance();
        String channelID = event.getChannel().getId();
        CountingInfo info = database.getCountingData(channelID);
        if (info.getActive() && info.getMessageID().equals(event.getMessageId())) {
            String userName = "<@!" + info.getUserID() + ">";
            Objects.requireNonNull(event.getJDA().getTextChannelById(channelID)).sendMessage(userName + " edited their message with a count of ` " + info.getValue() + " `! The next number is ` "+(info.getValue()+1)+" `!").queue();
        }
    }
}
