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

public class ScheduledMessagesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessagesManager.class);
    private final HashMap<Integer, ScheduledMessage> scheduledMessages = new HashMap<>();
    private final HashMap<Integer, ScheduledFuture<?>> scheduledFutures = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final JDA jdaInstance;

    public ScheduledMessagesManager(JDA jdaInstance) {
        this.jdaInstance = jdaInstance;
        getMessages();
        scheduleMessages();
    }

    private void getMessages() {
        Database database = Database.getInstance();
        ArrayList<ScheduledMessage> scheduledMessageArrayList = database.getScheduledMessages();
        if (scheduledMessageArrayList == null || scheduledMessageArrayList.isEmpty()) {
            LOGGER.error("Failed to obtain the list of scheduled messages!");
            return;
        }
        createScheduledMessagesMap(scheduledMessageArrayList);
    }

    private void createScheduledMessagesMap(ArrayList<ScheduledMessage> scheduledMessagesArrayList) {
        for (ScheduledMessage message : scheduledMessagesArrayList) {
            scheduledMessages.put(message.getId(), message);
        }
    }

    private void scheduleMessages() {
        for (ScheduledMessage message : scheduledMessages.values()) {
            ScheduledFuture<?> scheduledMessageFuture = scheduleMessage(message);
            scheduledFutures.put(message.getId(), scheduledMessageFuture);
        }
    }

    private ScheduledFuture<?> scheduleMessage(ScheduledMessage message) {
        int initialDelay = calculateInitialDelay(message.getExecutionTime());
        message.setJDA(jdaInstance);
        return scheduler.scheduleAtFixedRate(message, initialDelay, 86400, TimeUnit.SECONDS);
    }

    private int calculateInitialDelay(String executionTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        String currentYearString = String.valueOf(now.getYear());
        if (currentYearString.length() == 1) currentYearString = '0' + currentYearString;
        String currentMonthString = String.valueOf(now.getMonthValue());
        if(currentMonthString.length() == 1) currentMonthString = '0' + currentMonthString;
        String currentDayString = String.valueOf(now.getDayOfMonth());
        if(currentDayString.length() == 1) currentDayString = '0' + currentDayString;
        ZonedDateTime timeOfNextRun = LocalDateTime.parse(currentYearString + '-' + currentMonthString + '-' + currentDayString + ' ' + executionTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        if (now.isAfter(timeOfNextRun)) {
            timeOfNextRun = timeOfNextRun.plusDays(1);
        }
        Instant instantOfNextRun = timeOfNextRun.toInstant();
        Duration diff = Duration.between(Instant.now(), instantOfNextRun);
        return (int) diff.toSeconds();
    }

    public void addScheduledMessage(ScheduledMessage message) {
        ScheduledFuture<?> scheduledMessageFuture = scheduleMessage(message);
        scheduledFutures.put(message.getId(), scheduledMessageFuture);
        scheduledMessages.put(message.getId(), message);
    }

    public void deleteScheduledMessage(int id) {
        scheduledFutures.get(id).cancel(false);
        scheduledFutures.remove(id);
        scheduledMessages.remove(id);
    }
}
