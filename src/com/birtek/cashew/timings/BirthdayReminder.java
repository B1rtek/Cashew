package com.birtek.cashew.timings;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.Objects;

public class BirthdayReminder implements Runnable {

    private int id;
    private final String message, dateAndTime, serverID, userID;
    private String channelID;
    private static JDA jdaInstance = null;

    public BirthdayReminder(int id, String message, String dateAndTime, String channelID, String serverID, String userID) {
        this.id = id;
        this.message = message;
        this.dateAndTime = dateAndTime;
        this.channelID = channelID;
        this.serverID = serverID;
        this.userID = userID;
    }

    private static void cantBeDelivered(String reason, BirthdayReminder task) {
        PrivateChannel privateChannel = Objects.requireNonNull(jdaInstance.getUserById(task.getUserID())).openPrivateChannel().complete();
        if(privateChannel != null) {
            EmbedBuilder deliveryFailEmbed = new EmbedBuilder();
            deliveryFailEmbed.setTitle("Your birthday reminder could not be delivered");
            deliveryFailEmbed.setDescription("Reason: " + reason);
            deliveryFailEmbed.setColor(Color.red);
            String serverName = task.getServerID();
            Guild server = jdaInstance.getGuildById(task.getServerID());
            if(server != null) {
                serverName = server.getName();
            }
            deliveryFailEmbed.addField("Server", serverName, true);
            String channelName = task.getChannelID();
            TextChannel channel = jdaInstance.getTextChannelById(task.getChannelID());
            if(channel != null) {
                channelName = channel.getName();
            }
            deliveryFailEmbed.addField("Channel", channelName, true);
            deliveryFailEmbed.addField("Message", task.getMessage(), false);
            privateChannel.sendMessageEmbeds(deliveryFailEmbed.build()).queue();
        }
    }

    @Override
    public void run() {
        TextChannel textChannel = jdaInstance.getTextChannelById(this.channelID);
        if(textChannel == null) {
            cantBeDelivered("Destination channel doesn't exist", this);
            return;
        }
        try {
            textChannel.sendMessage(this.message).queue();
        } catch (InsufficientPermissionException e) {
            cantBeDelivered("Cashew can't send messages in the destination channel", this);
        } catch (IllegalArgumentException e) {
            cantBeDelivered("Your reminder was too long to send", this);
        } catch (UnsupportedOperationException e) {
            cantBeDelivered("Birthday reminders can't be sent to bots", this);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getServerID() {
        return serverID;
    }

    public String getUserID() {
        return userID;
    }

    public JDA getJdaInstance() {
        return jdaInstance;
    }

    public static void setJdaInstance(JDA jda) {
        jdaInstance = jda;
    }
}
