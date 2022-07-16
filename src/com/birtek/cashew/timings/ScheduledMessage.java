package com.birtek.cashew.timings;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class ScheduledMessage implements Runnable {

    private int id;
    private final String messageContent;
    private final String executionTime;
    private final String destinationChannelID;
    JDA jdaInstance;

    public ScheduledMessage(int id, String messageContent, String executionTime, String destinationChannelID) {
        this.id = id;
        this.messageContent = messageContent;
        this.executionTime = executionTime;
        this.destinationChannelID = destinationChannelID;
    }

    @Override
    public void run() {
        TextChannel textChannel = jdaInstance.getTextChannelById(this.destinationChannelID);
        assert textChannel != null;
        textChannel.sendMessage(this.messageContent).queue();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getDestinationChannelID() {
        return destinationChannelID;
    }

    public void setJDA(JDA jdaInstance) {
        this.jdaInstance = jdaInstance;
    }
}
