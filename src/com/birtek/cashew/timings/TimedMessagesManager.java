package com.birtek.cashew.timings;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.JDA;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class TimedMessagesManager {

    ArrayList<TimedMessage> timedMessagesArrayList = new ArrayList<>();
    Timer timer = new Timer();
    JDA jda;

    public TimedMessagesManager(JDA jda) throws ParseException {
        this.jda = jda;
        updateData();
        scheduleMessages();
    }

    //ukradzione z https://stackoverflow.com/questions/428918/how-can-i-increment-a-date-by-one-day-in-java
    private String addOneDay(String currentDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(currentDate));
        c.add(Calendar.DATE, 1);  // number of days to add
        return sdf.format(c.getTime());  // dt is now the new date
    }

    public void updateData() {
        timedMessagesArrayList.clear();
        cancelEverything();
        Database database = Database.getInstance();
        ArrayList<TimedMessage> newTimedMessages = database.getTimedMessages();
        if(newTimedMessages.size()>0) {
            timedMessagesArrayList = newTimedMessages;
        } else {
            System.out.println("something went seriously wrong (TimedMessagesManager.java, notifyDataSetChanged())");
        }
    }

    public void cancelEverything() {
        timer.cancel();
        timer.purge();
    }

    private void scheduleMessages() throws ParseException {
        timer = new Timer();
        for(TimedMessage message:timedMessagesArrayList) {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date timeNow = new Date();
            String plannedFirstExecutionDate = dateFormatter.format(timeNow).split("\\s+")[0];
            Date executionDate = dateFormatter.parse(plannedFirstExecutionDate + " " + message.executionTime);
            if(timeNow.after(executionDate)) {
                plannedFirstExecutionDate = addOneDay(plannedFirstExecutionDate);
                executionDate = dateFormatter.parse(plannedFirstExecutionDate + " " + message.executionTime);
            }
            message.jdaInstance = jda;
            timer.schedule(message, executionDate, message.repetitionInterval);
        }
    }

    public void refresh() throws ParseException {
        updateData();
        scheduleMessages();
    }
}