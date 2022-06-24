package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
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

    @Override
    public void run() {
        PrivateChannel privateChannel = Objects.requireNonNull(jdaInstance.getUserById(userID)).openPrivateChannel().complete();
        if(privateChannel != null) {
            EmbedBuilder reminderEmbed = new EmbedBuilder();
            if(this.ping) {
                reminderEmbed.setTitle(Objects.requireNonNull(privateChannel.getUser()).getAsMention() + ", your reminder:");
            } else {
                reminderEmbed.setTitle(Objects.requireNonNull(privateChannel.getUser()).getName() + ", your reminder:");
            }
            reminderEmbed.setDescription(this.content);
            privateChannel.sendMessageEmbeds(reminderEmbed.build()).queue();
        }
        Database database = Database.getInstance();
        if(!database.deleteReminder(this.id, this.userID)) LOGGER.warn("Failed to remove Reminder " + this.id + " from RemindersManager!");
        Cashew.remindersManager.deleteReminder(this.id);
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
