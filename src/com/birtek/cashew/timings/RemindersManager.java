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
    private final JDA jdaInstance;

    public RemindersManager(JDA jdaInstance) {
        this.jdaInstance = jdaInstance;
        ReminderRunnable.setJdaInstance(jdaInstance);
        getReminders();
        scheduleReminders();
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
        for(ReminderRunnable reminder: reminders.values()) {
            ScheduledFuture<?> reminderFuture = scheduleReminder(reminder);
            remindersFutures.put(reminder.getId(), reminderFuture);
        }
    }

    private ScheduledFuture<?> scheduleReminder(ReminderRunnable reminder) {
        int initialDelay = calculateInitialDelay(reminder.getDateTime());
        return scheduler.schedule(reminder, initialDelay, TimeUnit.SECONDS);
    }

    private int calculateInitialDelay(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime timeOfExecution = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
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
