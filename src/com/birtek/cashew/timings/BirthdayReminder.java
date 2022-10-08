package com.birtek.cashew.timings;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.Objects;

public class BirthdayReminder implements Runnable {

    private int id;
    private final String message, dateAndTime, serverID, userID;
    private String channelID;
    private static JDA jdaInstance = null;

    /**
     * Class describing a BirthdayReminder as well as being a {@link Runnable Runnable} that is scheduled and executed
     *
     * @param id          ID of the record in the database which generated this BirthdayReminder
     * @param message     message set up by the user who set up this reminder
     * @param dateAndTime date and time of when the reminder should be delivered (most likely the setter's birthday)
     * @param channelID   ID of the channel chosen by the setter in which the reminder will be sent if server's settings
     *                    don't specify an override channel
     * @param serverID    ID of the server on which this reminder was set up
     * @param userID      ID of the user who set up this reminder
     */
    public BirthdayReminder(int id, String message, String dateAndTime, String channelID, String serverID, String userID) {
        this.id = id;
        this.message = message;
        this.dateAndTime = dateAndTime;
        this.channelID = channelID;
        this.serverID = serverID;
        this.userID = userID;
    }

    /**
     * Sends an embed to a user whose BirthdayReminder couldn't be delivered due to some reason
     *
     * @param reason reason in a String describing why the reminder wasn't delivered
     * @param task   {@link BirthdayReminder BirthdayReminder} object corresponding to that reminder
     */
    private static void cantBeDelivered(String reason, BirthdayReminder task) {
        PrivateChannel privateChannel = Objects.requireNonNull(jdaInstance.getUserById(task.getUserID())).openPrivateChannel().complete();
        if (privateChannel != null) {
            EmbedBuilder deliveryFailEmbed = new EmbedBuilder();
            deliveryFailEmbed.setTitle("Your birthday reminder could not be delivered");
            deliveryFailEmbed.setDescription("Reason: " + reason);
            deliveryFailEmbed.setColor(Color.red);
            String serverName = task.getServerID();
            Guild server = jdaInstance.getGuildById(task.getServerID());
            if (server != null) {
                serverName = server.getName();
            }
            deliveryFailEmbed.addField("Server", serverName, true);
            String channelName = task.getChannelID();
            TextChannel channel = jdaInstance.getTextChannelById(task.getChannelID());
            if (channel != null) {
                channelName = channel.getName();
            }
            deliveryFailEmbed.addField("Channel", channelName, true);
            deliveryFailEmbed.addField("Message", task.getMessage(), false);
            privateChannel.sendMessageEmbeds(deliveryFailEmbed.build()).queue();
        }
    }

    /**
     * Sends the BirthdayReminder according to its settings, or an error message if something goes wrong
     */
    @Override
    public void run() {
        TextChannel textChannel = jdaInstance.getTextChannelById(this.channelID);
        if (textChannel == null) {
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

    public static void setJdaInstance(JDA jda) {
        jdaInstance = jda;
    }
}
