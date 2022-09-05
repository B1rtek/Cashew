package com.birtek.cashew.reactions;

import com.birtek.cashew.database.CountingDatabase;
import com.birtek.cashew.database.CountingInfo;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CountingMessageModificationDetector extends ListenerAdapter {
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        CountingDatabase database = CountingDatabase.getInstance();
        String channelID = event.getChannel().getId();
        CountingInfo info = database.getCountingData(channelID);
        if (info != null && info.active() && info.messageID().equals(event.getMessageId())) {
            String userName = "<@!" + info.userID() + ">";
            Objects.requireNonNull(event.getJDA().getTextChannelById(channelID)).sendMessage(userName + " edited their message with a count of ` " + info.value() + " `! The next number is ` "+(info.value()+1)+" `!").queue();
        }
    }
}
