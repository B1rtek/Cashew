package com.birtek.cashew.timings;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

public class BirthdayRemindersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayRemindersManager.class);
    private ArrayList<BirthdayReminder> birthdayReminders = new ArrayList<>();
    private final HashMap<String, BirthdayReminderDefaults> birthdayReminderDefaults = new HashMap<>();
    private final Timer timer = new Timer();
    private final JDA jdaInstance;

    public BirthdayRemindersManager(JDA jda) {
        this.jdaInstance = jda;
        getReminders();
        scheduleReminders();
    }

    private void getReminders() {
        Database database = Database.getInstance();
        birthdayReminders = database.getBirthdayReminders();
        if (birthdayReminders == null || birthdayReminders.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminders!");
            return;
        }
        ArrayList<BirthdayReminderDefaults> defaultsList = database.getBirthdayReminderDefaults();
        if (defaultsList == null || defaultsList.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminder defaults!");
            return;
        }
        createDefaultsMap(defaultsList);
        BirthdayReminder.setJdaInstance(jdaInstance);
        for (BirthdayReminder reminder : birthdayReminders) {
            BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(reminder.getServerID());
            if (defaults.override() || reminder.getChannelID().isEmpty()) {
                reminder.setChannelID(defaults.channelID());
            }
        }
    }

    /**
     * I'm assuming that the bot will never run for an entire year without restarts
     */
    private void scheduleReminders() {
        for (BirthdayReminder reminder : birthdayReminders) {
            Date executionDate = getExecutionDate(reminder);
            if (executionDate != null) timer.schedule(reminder, executionDate);
        }
    }

    private Date getExecutionDate(BirthdayReminder reminder) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return dateFormatter.parse(reminder.getDateAndTime());
        } catch (ParseException e) {
            LOGGER.warn("Failed to schedule reminder with id = " + reminder.getId());
            return null;
        }
    }

    private void createDefaultsMap(ArrayList<BirthdayReminderDefaults> defaultsList) {
        for (BirthdayReminderDefaults defaults : defaultsList) {
            birthdayReminderDefaults.put(defaults.serverID(), defaults);
        }
    }
}
