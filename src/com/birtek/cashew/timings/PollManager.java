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

public class PollManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollManager.class);
    private final HashMap<Integer, PollSummarizer> polls = new HashMap<>();
    private final HashMap<Integer, ScheduledFuture<?>> pollsFutures = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static JDA jdaInstance;

    public PollManager() {

    }

    public void start(JDA jda) {
        jdaInstance = jda;
        getPolls();
        schedulePollSummarizers();
    }

    private void getPolls() {
        Database database = Database.getInstance();
        ArrayList<PollSummarizer> pollSummarizersList = database.getAllPolls();
        if(pollSummarizersList == null) {
            LOGGER.error("Failed to obtain the list of polls!");
            return;
        }
        createPollsMap(pollSummarizersList);
    }

    private void createPollsMap(ArrayList<PollSummarizer> pollSummarizerList) {
        for(PollSummarizer poll: pollSummarizerList) {
            polls.put(poll.getId(), poll);
        }
    }

    private void schedulePollSummarizers() {
        for(PollSummarizer poll: polls.values()) {
            poll.setJdaInstance(jdaInstance);
            ScheduledFuture<?> pollFuture = schedulePollSummarizer(poll);
            pollsFutures.put(poll.getId(), pollFuture);
        }
    }

    private ScheduledFuture<?> schedulePollSummarizer(PollSummarizer poll) {
        int initialDelay = calculateInitialDelay(poll.getEndTime());
        return scheduler.schedule(poll, initialDelay, TimeUnit.SECONDS);
    }

    private int calculateInitialDelay(String pollEndTime) {
        // I know that it's a duplicate
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime timeOfExecution = LocalDateTime.parse(pollEndTime, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        if(now.isAfter(timeOfExecution)) {
            timeOfExecution = now.plusSeconds(10); // schedule the missed ones to be delivered immediately
        }
        Instant instantOfExecution = timeOfExecution.toInstant();
        Duration diff = Duration.between(Instant.now(), instantOfExecution);
        return (int) diff.toSeconds();
    }

    public void addPoll(PollSummarizer poll) {
        ScheduledFuture<?> pollFuture = schedulePollSummarizer(poll);
        pollsFutures.put(poll.getId(), pollFuture);
        polls.put(poll.getId(), poll);
    }

    public void deletePoll(int id) {
        pollsFutures.get(id).cancel(false);
        pollsFutures.remove(id);
        polls.remove(id);
    }
}
