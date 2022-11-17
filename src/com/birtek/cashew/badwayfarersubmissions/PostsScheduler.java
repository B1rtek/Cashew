package com.birtek.cashew.badwayfarersubmissions;

import com.birtek.cashew.Cashew;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PostsScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static long nextPostTimestamp = 0;

    public PostsScheduler() {
        scheduleNextPost();
    }

    public static void scheduleNextPost() {
        PostsDatabase database = PostsDatabase.getInstance();
        int postsCount = database.getVerifiedPostsCount();
        if (postsCount <= 0) postsCount = 1;
        int range = (10 - Math.min(postsCount, 8)) * 3600;
        Random random = new Random();
        int delay = random.nextInt(range);
        delay = verifyExecutionTime(delay);
        nextPostTimestamp = Instant.now().getEpochSecond() + delay;
        scheduler.schedule(new SchedulerRunnable(), delay, TimeUnit.SECONDS);
    }

    private static int verifyExecutionTime(int delay) {
        ZonedDateTime targetTime = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        targetTime = targetTime.plusSeconds(delay);
        if (targetTime.getHour() >= 1 && targetTime.getHour() < 7) {
            delay += (7 - targetTime.getHour()) * 3600;
        }
        return delay;
    }

    public String getNextPostTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d/L H:mm").withLocale(new Locale("pl"));
        ZonedDateTime targetTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(nextPostTimestamp), ZoneOffset.UTC).atZone(ZoneId.of("Europe/Warsaw"));
        return dateTimeFormatter.format(targetTime);
    }

    private static class SchedulerRunnable implements Runnable {

        @Override
        public void run() {
            PostsDatabase database = PostsDatabase.getInstance();
            Post post = database.getOldestVerifiedPost();
            if (post != null && post.id() != 0) {
                if (Cashew.badWayfarerBot.postSubmission(post, Bot.badWayfarerChannelID)) {
                    database.removePost(post.id());
                }
            }
            PostsScheduler.scheduleNextPost();
        }
    }

}
