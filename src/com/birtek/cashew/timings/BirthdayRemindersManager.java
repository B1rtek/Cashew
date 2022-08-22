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

    /**
     * Initializes the manager by getting and scheduling all reminders listed in the database
     *
     * @param jda JDA instance created in the {@link com.birtek.cashew.Cashew Cashew} class
     */
    public BirthdayRemindersManager(JDA jda) {
        this.jdaInstance = jda;
        getReminders();
        scheduleReminders();
    }

    /**
     * Obtains all reminders saved in the database and puts them on HashMaps which map IDs of the reminders and default
     * settings onto their corresponding {@link BirthdayReminder BirthdayReminders} and
     * {@link BirthdayReminderDefaults BirthdayReminderDefaults} objects
     */
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
     * Schedules all reminders for execution without a repeat because calculating leap years might get tricky, and the
     * bot is restarted every 24h anyway
     */
    private void scheduleReminders() {
        for (BirthdayReminder reminder : birthdayReminders.values()) {
            ScheduledFuture<?> scheduleReminderFuture = scheduleReminder(reminder);
            birthdayRemindersFutures.put(reminder.getId(), scheduleReminderFuture);
        }
    }

    /**
     * Schedules a reminder by applying default settings of the server on which the reminder was set up, calculates
     * initial delay needed to schedule the task and then schedules them with calculated parameters
     *
     * @param reminder {@link BirthdayReminder BirthdayReminder} to schedule
     * @return a {@link ScheduledFuture ScheduledFuture} generated for this reminder
     */
    private ScheduledFuture<?> scheduleReminder(BirthdayReminder reminder) {
        BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(reminder.getServerID());
        if (defaults != null) {
            if (defaults.override() || reminder.getChannelID().isEmpty()) {
                reminder.setChannelID(defaults.channelID());
            }
        }
        int initialDelay = calculateInitialDelay(reminder.getDateAndTime());
        return scheduler.scheduleAtFixedRate(reminder, initialDelay, 31536000, TimeUnit.SECONDS);
    }

    /**
     * Calculates the Instant of the next time of execution for a birthday reminder with the provided birthday date
     *
     * @param executionDateTimeString execution time set by the user who created the {@link BirthdayReminder reminder}
     * @return Instant of the next time the birthday reminder should be delivered
     */
    public static Instant getNextRunTimeInstant(String executionDateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime timeOfNextRun = LocalDateTime.parse(executionDateTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        timeOfNextRun = timeOfNextRun.plusYears(now.getYear() - timeOfNextRun.getYear());
        return timeOfNextRun.toInstant();
    }

    /**
     * Calculates initial delay needed for scheduling {@link Runnable Runnables} with
     * {@link ScheduledExecutorService ScheduledExecutorService} using complicated date stuff and avoiding accidental
     * timezone conversions that ruin everything
     *
     * @param executionDateTimeString execution time set by the user who created the {@link BirthdayReminder reminder}
     * @return number of seconds before the planned execution specified by the executionDateTimeString as an integer
     */
    private int calculateInitialDelay(String executionDateTimeString) {
        Duration diff = Duration.between(Instant.now(), getNextRunTimeInstant(executionDateTimeString));
        return (int) diff.toSeconds() + 1;
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

    /**
     * Removes a reminder from the database and then if that's successful, cancels it's
     * {@link ScheduledFuture ScheduledFuture} and removes it from HashMaps
     *
     * @param serverID ID of the server from which the request came
     * @param userID   ID of the user who requested the removal of their reminder
     * @return true if the removal was successful, false otherwise
     */
    public boolean deleteBirthdayReminder(String serverID, String userID) {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        int deletedReminderID = database.deleteBirthdayReminder(serverID, userID);
        if (deletedReminderID == -1) return false;
        birthdayRemindersFutures.get(deletedReminderID).cancel(false);
        birthdayRemindersFutures.remove(deletedReminderID);
        birthdayReminders.remove(deletedReminderID);
        return true;
    }

    /**
     * Used by the Manager internally to update all reminders whose server's default settings changed
     *
     * @param reminder reminder that will be rescheduled with updated data
     */
    private void updateBirthdayReminder(BirthdayReminder reminder) {
        birthdayRemindersFutures.get(reminder.getId()).cancel(false);
        birthdayRemindersFutures.remove(reminder.getId());
        birthdayReminders.remove(reminder.getId());
        ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
        birthdayReminders.put(reminder.getId(), reminder);
        birthdayRemindersFutures.put(reminder.getId(), reminderFuture);
    }

    /**
     * Updates server's default settings for BirthdayReminders and updates all affected scheduled reminders
     *
     * @param defaults new {@link BirthdayReminderDefaults settings} to apply
     * @return true if the update was successful, false otherwise
     */
    public boolean updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        BirthdayRemindersDatabase database = BirthdayRemindersDatabase.getInstance();
        if (!database.setBirthdayRemindersDefaults(defaults)) return false;
        birthdayReminderDefaults.put(defaults.serverID(), defaults);
        ArrayList<BirthdayReminder> affectedReminders = database.getBirthdayRemindersFromServer(defaults.serverID());
        if (affectedReminders == null) {
            LOGGER.warn("Failed to obtain birthday reminders for server " + defaults.serverID());
            return false;
        } else {
            for (BirthdayReminder reminder : affectedReminders) {
                updateBirthdayReminder(reminder);
            }
        }
        return true;
    }

    /**
     * Gets the default BirthdayReminders channel of the provided server
     *
     * @param serverID ID of the server to check the default channel of
     * @return ID of the default channel as a String or null if something went wrong
     */
    public String getDefaultChannel(String serverID) {
        BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(serverID);
        if (defaults == null) return null;
        return defaults.channelID();
    }
}
