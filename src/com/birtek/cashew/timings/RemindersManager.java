package com.birtek.cashew.timings;

import com.birtek.cashew.Database;
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

    public RemindersManager() {
        getReminders();
        scheduleReminders();
    }

    public void setJDA(JDA jdaInstance) {
        ReminderRunnable.setJdaInstance(jdaInstance);
    }

    private void getReminders() {
        Database database = Database.getInstance();
        ArrayList<ReminderRunnable> remindersArrayList = database.getAllReminders();
        if(remindersArrayList == null) {
            LOGGER.error("Failed to obtain the list of reminders!");
            return;
        }
        createRemindersMap(remindersArrayList);
    }

    private void createRemindersMap(ArrayList<ReminderRunnable> remindersArrayList) {
        for(ReminderRunnable reminder: remindersArrayList) {
            reminders.put(reminder.getId(), reminder);
        }
    }

    private void scheduleReminders() {
        ArrayList<ReminderRunnable> toDelete = new ArrayList<>();
        for(ReminderRunnable reminder: reminders.values()) {
            ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
            if(reminderFuture == null) {
                toDelete.add(reminder);
            } else {
                remindersFutures.put(reminder.getId(), reminderFuture);
            }
        }
        Database database = Database.getInstance();
        for(ReminderRunnable reminder: toDelete) {
            database.deleteReminder(reminder.getId(), reminder.getUserID());
        }
    }

    private ScheduledFuture<?> scheduleReminder(ReminderRunnable reminder) {
        int initialDelay = calculateInitialDelay(reminder.getDateTime());
        if(initialDelay <= 0) {
            return null;
        }
        return scheduler.schedule(reminder, initialDelay, TimeUnit.SECONDS);
    }

    private int calculateInitialDelay(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
        LocalDateTime timeOfExecution = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
        if(now.isAfter(timeOfExecution)) return -1;
        ZonedDateTime zdt = ZonedDateTime.of(timeOfExecution, ZoneId.of("Europe/Warsaw"));
        Instant instantOfExecution = zdt.toInstant();
        Duration diff = Duration.between(Instant.now(), instantOfExecution);
        return (int) diff.toSeconds();
    }

    public void addReminder(ReminderRunnable reminder) {
        ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
        remindersFutures.put(reminder.getId(), reminderFuture);
        reminders.put(reminder.getId(), reminder);
    }

    public void deleteReminder(int id) {
        remindersFutures.get(id).cancel(false);
        remindersFutures.remove(id);
        remindersFutures.remove(id);
    }
}
