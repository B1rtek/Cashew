package com.birtek.cashew.timings;

import com.birtek.cashew.database.BirthdayReminderDefaults;
import com.birtek.cashew.database.BirthdayRemindersDatabase;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BirthdayRemindersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayRemindersManager.class);
    private final HashMap<Integer, BirthdayReminder> birthdayReminders = new HashMap<>();
    private final HashMap<Integer, ScheduledFuture<?>> birthdayRemindersFutures = new HashMap<>();
    private final HashMap<String, BirthdayReminderDefaults> birthdayReminderDefaults = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final JDA jdaInstance;

    public BirthdayRemindersManager(JDA jda) {
        this.jdaInstance = jda;
        getReminders();
        scheduleReminders();
    }

    private void getReminders() {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        ArrayList<BirthdayReminder> reminders = database.getAllReminders();
        if (reminders == null || reminders.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminders!");
            return;
        }
        createRemindersMap(reminders);
        ArrayList<BirthdayReminderDefaults> defaultsList = database.getAllDefaults();
        if (defaultsList == null || defaultsList.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminder defaults!");
            return;
        }
        createDefaultsMap(defaultsList);
        BirthdayReminder.setJdaInstance(jdaInstance);
    }

    /**
     * I'm assuming that the bot will never run for an entire year without restarts
     */
    private void scheduleReminders() {
        for (BirthdayReminder reminder : birthdayReminders.values()) {
            ScheduledFuture<?> scheduleReminderFuture = scheduleReminder(reminder);
            birthdayRemindersFutures.put(reminder.getId(), scheduleReminderFuture);
        }
    }

    ScheduledFuture<?> scheduleReminder(BirthdayReminder reminder) {
        BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(reminder.getServerID());
        if (defaults != null) {
            if (defaults.override() || reminder.getChannelID().isEmpty()) {
                reminder.setChannelID(defaults.channelID());
            }
        }
        int initialDelay = calculateInitialDelay(reminder.getDateAndTime());
        return scheduler.scheduleAtFixedRate(reminder, initialDelay, 31536000, TimeUnit.SECONDS);
    }

    private int calculateInitialDelay(String executionDateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime timeOfNextRun = LocalDateTime.parse(executionDateTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        while (now.isAfter(timeOfNextRun)) {
            timeOfNextRun = timeOfNextRun.plusYears(1);
        }
        Instant instantOfNextRun = timeOfNextRun.toInstant();
        Duration diff = Duration.between(Instant.now(), instantOfNextRun);
        return (int) diff.toSeconds();
    }

    private void createDefaultsMap(ArrayList<BirthdayReminderDefaults> defaultsList) {
        for (BirthdayReminderDefaults defaults : defaultsList) {
            birthdayReminderDefaults.put(defaults.serverID(), defaults);
        }
    }

    private void createRemindersMap(ArrayList<BirthdayReminder> reminders) {
        for (BirthdayReminder reminder : reminders) {
            birthdayReminders.put(reminder.getId(), reminder);
        }
    }

    /**
     * Adds a reminder to the database and then if that's successful, schedules it for execution
     *
     * @param reminder {@link BirthdayReminder BirthdayReminder} to add
     * @return true if the addition was successful, false otherwise
     */
    public boolean addBirthdayReminder(BirthdayReminder reminder) {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        BirthdayReminder reminderWithID = database.setBirthdayReminder(reminder);
        if (reminderWithID == null) return false;
        ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
        birthdayReminders.put(reminder.getId(), reminder);
        birthdayRemindersFutures.put(reminder.getId(), reminderFuture);
        return true;
    }

    public boolean deleteBirthdayReminder(String serverID, String userID) {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        int deletedReminderID = database.deleteBirthdayReminder(serverID, userID);
        if (deletedReminderID == -1) return false;
        birthdayRemindersFutures.get(deletedReminderID).cancel(false);
        birthdayRemindersFutures.remove(deletedReminderID);
        birthdayReminders.remove(deletedReminderID);
        return true;
    }

    private boolean updateBirthdayReminder(BirthdayReminder reminder) {
        if(!deleteBirthdayReminder(reminder.getServerID(), reminder.getUserID())) return false;
        return addBirthdayReminder(reminder);
    }

    public boolean updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        if(!database.setBirthdayRemindersDefaults(defaults)) return false;
        birthdayReminderDefaults.put(defaults.serverID(), defaults);
        ArrayList<BirthdayReminder> affectedReminders = database.getBirthdayRemindersFromServer(defaults.serverID());
        if (affectedReminders == null) {
            LOGGER.warn("Failed to obtain birthday reminders for server " + defaults.serverID());
            return false;
        } else {
            for (BirthdayReminder reminder : affectedReminders) {
                if(!updateBirthdayReminder(reminder)) return false;
            }
        }
        return true;
    }

    public String getDefaultChannel(String serverID) {
        BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(serverID);
        if (defaults == null) return null;
        return defaults.channelID();
    }
}
