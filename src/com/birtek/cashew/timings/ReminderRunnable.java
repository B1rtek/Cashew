package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ReminderRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderRunnable.class);
    private int id;
    private final boolean ping;
    private final String content, dateTime, userID;
    private static JDA jdaInstance;

    /**
     * A class that contains all information needed to send the reminder scheduled by a user
     *
     * @param id       ID of the reminder assigned by the database
     * @param ping     if set to true, a ping will be a part of the reminder
     * @param content  text content of the reminder
     * @param dateTime timestamp in a String form interpretable by date formatters with the reminder delivery time and
     *                 date
     * @param userID   ID of the user who set the reminder
     */
    public ReminderRunnable(int id, boolean ping, String content, String dateTime, String userID) {
        this.id = id;
        this.ping = ping;
        this.content = content;
        this.dateTime = dateTime;
        this.userID = userID;
    }

    public static void setJdaInstance(JDA jdaInstance) {
        ReminderRunnable.jdaInstance = jdaInstance;
    }

    /**
     * Sends a reminder to the person who set the reminder according to its settings, and then automatically removes it
     */
    @Override
    public void run() {
        PrivateChannel privateChannel = Objects.requireNonNull(jdaInstance.getUserById(userID)).openPrivateChannel().complete();
        if (privateChannel != null) {
            EmbedBuilder reminderEmbed = new EmbedBuilder();
            reminderEmbed.setTitle(Objects.requireNonNull(privateChannel.getUser()).getName() + ", your reminder:");
            reminderEmbed.setDescription(this.content);
            if (this.ping) {
                privateChannel.sendMessageEmbeds(reminderEmbed.build()).append(privateChannel.getUser().getAsMention()).queue();
            } else {
                privateChannel.sendMessageEmbeds(reminderEmbed.build()).queue();
            }
        }
        if (Cashew.remindersManager.deleteReminder(this.id, this.userID) != 1)
            LOGGER.warn("Failed to remove Reminder " + this.id + " from RemindersManager!");
    }

    public int getId() {
        return id;
    }

    public boolean isPing() {
        return ping;
    }

    public String getContent() {
        return content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setId(int id) {
        this.id = id;
    }
}
