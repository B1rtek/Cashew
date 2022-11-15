package com.birtek.cashew.badwayfarersubmissions;

import com.birtek.cashew.Cashew;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PostsManager {

    private static int currentlyScheduled = 0;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public String schedulePost(Post post) {
        Random random = new Random();
        int range = 10;//(12 + currentlyScheduled) * 3600;
        int delay = random.nextInt(range);
        scheduler.schedule(new PostRunnable(post), delay, TimeUnit.SECONDS);
        currentlyScheduled++;
        return getExecutionTime(delay);
    }

    public PostsManager() {
        PostsDatabase database = PostsDatabase.getInstance();
        ArrayList<Post> posts = database.getAllPosts();
        if (posts == null) {
            System.err.println("The database is offline!");
            System.exit(1);
        }
        for (Post post : posts) {
            System.out.println("Post scheduled for " + schedulePost(post));
        }
    }

    private String getExecutionTime(int delay) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime targetTime = LocalDateTime.now().atZone(ZoneId.of("Europe/Warsaw"));
        targetTime = targetTime.plusSeconds(delay);
        return dateTimeFormatter.format(targetTime);
    }

    private class PostRunnable implements Runnable {

        private final Post post;

        private PostRunnable(Post post) {
            this.post = post;
        }

        @Override
        public void run() {
            Cashew.badWayfarerBot.postSubmission(post, Bot.testChannelID);
            currentlyScheduled--;
            PostsDatabase database = PostsDatabase.getInstance();
            database.removePost(post.id());
        }
    }

}
