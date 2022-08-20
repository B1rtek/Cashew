package com.birtek.cashew.timings;

import com.birtek.cashew.database.ScheduledMessagesDatabase;
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
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ArrayList<ScheduledMessage> scheduledMessageArrayList = database.getAllScheduledMessages();
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

    public static Instant getInstantFromExecutionTime(String executionTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        String currentYearString = String.valueOf(now.getYear());
        if (currentYearString.length() == 1) currentYearString = '0' + currentYearString;
        String currentMonthString = String.valueOf(now.getMonthValue());
        if (currentMonthString.length() == 1) currentMonthString = '0' + currentMonthString;
        String currentDayString = String.valueOf(now.getDayOfMonth());
        if (currentDayString.length() == 1) currentDayString = '0' + currentDayString;
        ZonedDateTime timeOfNextRun = LocalDateTime.parse(currentYearString + '-' + currentMonthString + '-' + currentDayString + ' ' + executionTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        if (now.isAfter(timeOfNextRun)) {
            timeOfNextRun = timeOfNextRun.plusDays(1);
        }
        return timeOfNextRun.toInstant();
    }

    private int calculateInitialDelay(String executionTimeString) {
        Instant instantOfNextRun = getInstantFromExecutionTime(executionTimeString);
        Duration diff = Duration.between(Instant.now(), instantOfNextRun);
        return (int) diff.toSeconds() + 1;
    }

    /**
     * Adds a {@link ScheduledMessage ScheduledMessage} to the database and schedules it afterwards
     *
     * @param message {@link ScheduledMessage ScheduledMessage} to add
     * @return ID of the message assigned by the database, or -1 if an error occurred
     */
    public int addScheduledMessage(ScheduledMessage message, String serverID) {
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ScheduledMessage messageWithID = database.addScheduledMessage(message, serverID);
        if (messageWithID == null) return -1;
        ScheduledFuture<?> scheduledMessageFuture = scheduleMessage(message);
        scheduledFutures.put(message.getId(), scheduledMessageFuture);
        scheduledMessages.put(message.getId(), message);
        return message.getId();
    }

    /**
     * Removes a {@link ScheduledMessage ScheduledMessage} from the database and unschedules it afterwards
     *
     * @param id       ID of the {@link ScheduledMessage ScheduledMessage} to remove, if it's equal to 0, all messages set on
     *                 the server will be removed
     * @param serverID ID of the server from which this request came
     * @return true if the removal was successful, false otherwise
     */
    public boolean deleteScheduledMessage(int id, String serverID) {
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ArrayList<ScheduledMessage> messagesToRemove = database.getScheduledMessages(id, serverID);
        if (!database.removeScheduledMessage(id, serverID)) return false;
        for (ScheduledMessage message : messagesToRemove) {
            scheduledFutures.get(message.getId()).cancel(false);
            scheduledFutures.remove(message.getId());
            scheduledMessages.remove(message.getId());
        }
        return true;
    }
}
