package com.birtek.cashew.timings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.TimerTask;

public class TimedMessage extends TimerTask {

    final String messageContent;
    final String executionTime;
    final int repetitionInterval;
    final String destinationChannelID;
    JDA jdaInstance;

    public TimedMessage(String messageContent, String executionTime, int repetitionInterval, String destinationChannelID) {
        this.messageContent = messageContent;
        this.executionTime = executionTime;
        this.repetitionInterval = repetitionInterval;
        this.destinationChannelID = destinationChannelID;
    }

    @Override
    public void run() {
        TextChannel textChannel = jdaInstance.getTextChannelById(this.destinationChannelID);
        assert textChannel != null;
        textChannel.sendMessage(this.messageContent).queue();
    }
}