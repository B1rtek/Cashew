package com.birtek.cashew.timings;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemindersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessagesManager.class);

    private final JDA jdaInstance;

    public RemindersManager(JDA jdaInstance) {
        this.jdaInstance = jdaInstance;
    }

    public void addReminder(ReminderRunnable reminder) {

    }

    public void deleteReminder(int id) {

    }
}
