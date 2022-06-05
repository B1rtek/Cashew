package com.birtek.cashew.timings;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BirthdayRemindersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayRemindersManager.class);
    private final HashMap<Integer, BirthdayReminder> birthdayReminders = new HashMap<Integer, BirthdayReminder>();
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
        ArrayList<BirthdayReminder> reminders = database.getBirthdayReminders();
        if (reminders == null || reminders.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminders!");
            return;
        }
        createRemindersMap(reminders);
        ArrayList<BirthdayReminderDefaults> defaultsList = database.getBirthdayReminderDefaults();
        if (defaultsList == null || defaultsList.isEmpty()) {
            LOGGER.error("Failed to obtain the list of birthday reminder defaults!");
            return;
        }
        createDefaultsMap(defaultsList);
        BirthdayReminder.setJdaInstance(jdaInstance);
        scheduleReminders();
    }

    /**
     * I'm assuming that the bot will never run for an entire year without restarts
     */
    private void scheduleReminders() {
        for (BirthdayReminder reminder : birthdayReminders.values()) {
            reminder = scheduleReminder(reminder);
            if(reminder != null) {
                birthdayReminders.put(reminder.getId(), reminder);
            }
        }
    }

    BirthdayReminder scheduleReminder(BirthdayReminder reminder) {
        BirthdayReminderDefaults defaults = birthdayReminderDefaults.get(reminder.getServerID());
        if (defaults.override() || reminder.getChannelID().isEmpty()) {
            reminder.setChannelID(defaults.channelID());
        }
        Date executionDate = getExecutionDate(reminder);
        if (executionDate != null) {
            timer.schedule(reminder, executionDate);
            return reminder;
        } else {
            return null;
        }
    }

    private Date getExecutionDate(BirthdayReminder reminder) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date proposedDate = dateFormatter.parse(reminder.getDateAndTime());
            Date timeNow = new Date();
            if(timeNow.after(proposedDate)) {
                proposedDate = addOneYear(proposedDate);
            }
            return proposedDate;
        } catch (ParseException e) {
            LOGGER.warn("Failed to schedule reminder with id = " + reminder.getId());
            return null;
        }
    }

    private Date addOneYear(Date proposedDate) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(proposedDate);
        c.add(Calendar.YEAR, 1);
        return c.getTime();
    }

    private void createDefaultsMap(ArrayList<BirthdayReminderDefaults> defaultsList) {
        for (BirthdayReminderDefaults defaults : defaultsList) {
            birthdayReminderDefaults.put(defaults.serverID(), defaults);
        }
    }

    private void createRemindersMap(ArrayList<BirthdayReminder> reminders) {
        for(BirthdayReminder reminder: reminders) {
            birthdayReminders.put(reminder.getId(), reminder);
        }
    }

    public boolean addBirthdayReminder(BirthdayReminder reminder) {
        reminder = scheduleReminder(reminder);
        if(reminder != null) {
            birthdayReminders.put(reminder.getId(), reminder);
            return true;
        }
        return false;
    }

    public void deleteBirthdayReminder(int id) {
        birthdayReminders.get(id).cancel();
        birthdayReminders.remove(id);
    }

    public boolean updateBirthdayReminder(BirthdayReminder reminder) {
        deleteBirthdayReminder(reminder.getId());
        return addBirthdayReminder(reminder);
    }

    public void updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        birthdayReminderDefaults.put(defaults.serverID(), defaults);
    }
}
