package com.birtek.cashew.timings;

import com.birtek.cashew.database.RemindersDatabase;
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

public class RemindersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessagesManager.class);
    private final HashMap<Integer, ReminderRunnable> reminders = new HashMap<>();
    private final HashMap<Integer, ScheduledFuture<?>> remindersFutures = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Initializes the manager by getting and scheduling all reminders listed in the database
     */
    public RemindersManager() {
        getReminders();
        scheduleReminders();
    }

    public void setJDA(JDA jdaInstance) {
        ReminderRunnable.setJdaInstance(jdaInstance);
    }

    /**
     * Gets all reminders from the database and puts them on HashMaps
     */
    private void getReminders() {
        RemindersDatabase database = RemindersDatabase.getInstance();
        ArrayList<ReminderRunnable> remindersArrayList = database.getAllReminders();
        if (remindersArrayList == null) {
            LOGGER.error("Failed to obtain the list of reminders!");
            return;
        }
        createRemindersMap(remindersArrayList);
    }

    private void createRemindersMap(ArrayList<ReminderRunnable> remindersArrayList) {
        for (ReminderRunnable reminder : remindersArrayList) {
            reminders.put(reminder.getId(), reminder);
        }
    }

    /**
     * Schedules all reminders for execution
     */
    private void scheduleReminders() {
        for (ReminderRunnable reminder : reminders.values()) {
            ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
            remindersFutures.put(reminder.getId(), reminderFuture);
        }
    }

    /**
     * Schedules a reminder by calculating initial delay needed to schedule the task and then schedules it with
     * calculated parameters
     *
     * @param reminder {@link ReminderRunnable ReminderRunnable} to schedule
     * @return a {@link ScheduledFuture ScheduledFuture} generated for this reminder
     */
    private ScheduledFuture<?> scheduleReminder(ReminderRunnable reminder) {
        int initialDelay = calculateInitialDelay(reminder.getDateTime());
        return scheduler.schedule(reminder, initialDelay, TimeUnit.SECONDS);
    }

    /**
     * Calculates initial delay needed for scheduling {@link Runnable Runnables} with
     * {@link ScheduledExecutorService ScheduledExecutorService} using complicated date stuff and avoiding accidental
     * timezone conversions that ruin everything
     *
     * @param dateTimeString execution time set by the user who created the {@link ReminderRunnable reminder}
     * @return number of seconds before the planned execution specified by the dateTimeString as an integer
     */
    private int calculateInitialDelay(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime timeOfExecution = LocalDateTime.parse(dateTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        if (now.isAfter(timeOfExecution)) {
            timeOfExecution = now.plusSeconds(10); // schedule the missed ones to be delivered immediately
        }
        Instant instantOfExecution = timeOfExecution.toInstant();
        Duration diff = Duration.between(Instant.now(), instantOfExecution);
        return (int) diff.toSeconds() + 1;
    }

    /**
     * Adds the {@link ReminderRunnable reminder} to the database and then schedules its execution
     *
     * @param reminder {@link ReminderRunnable ReminderRunnable} representing the reminder to add
     * @return an ID assigned by the database belonging to this reminder or -1 if an error occurred
     */
    public int addReminder(ReminderRunnable reminder) {
        RemindersDatabase database = RemindersDatabase.getInstance();
        ReminderRunnable reminderWithID = database.addReminder(reminder);
        if (reminderWithID == null) return -1;
        ScheduledFuture<?> reminderFuture = scheduleReminder(reminderWithID);
        remindersFutures.put(reminderWithID.getId(), reminderFuture);
        reminders.put(reminderWithID.getId(), reminder);
        return reminderWithID.getId();
    }

    /**
     * Removes a reminder from the database and unschedules it
     *
     * @param id     ID of the reminder to remove given by the user
     * @param userID ID of the user who is trying to remove the reminder
     * @return 1 if the deletion was successful, 0 if the user tried to remove a reminder that isn't theirs or that
     * doesn't exist, or -1 if an error occurred
     */
    public int deleteReminder(int id, String userID) {
        RemindersDatabase database = RemindersDatabase.getInstance();
        int returnValue = database.deleteReminder(id, userID);
        if(returnValue <= 0) return returnValue;
        remindersFutures.get(id).cancel(false);
        remindersFutures.remove(id);
        remindersFutures.remove(id);
        return 1;
    }

    /**
     * Removes all user's reminders
     * @param userID ID of the user who requested deletion of all their reminders
     * @return true if all reminders were successfully deleted, false otherwise
     */
    public boolean deleteAllReminders(String userID) {
        RemindersDatabase database = RemindersDatabase.getInstance();
        ArrayList<Integer> deletedIDs = database.deleteUsersReminders(userID);
        if(deletedIDs == null) return false;
        for(int id: deletedIDs) {
            remindersFutures.get(id).cancel(false);
            remindersFutures.remove(id);
            remindersFutures.remove(id);
        }
        return true;
    }
}
