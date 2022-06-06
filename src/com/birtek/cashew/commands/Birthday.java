package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import com.birtek.cashew.timings.BirthdayReminder;
import com.birtek.cashew.timings.BirthdayReminderDefaults;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Birthday extends BaseCommand {

    private final HashMap<String, String> monthMap = new HashMap<>();
    private final ArrayList<String> monthList = new ArrayList<>() {
        {
            add("January");
            add("February");
            add("March");
            add("April");
            add("May");
            add("June");
            add("July");
            add("August");
            add("September");
            add("October");
            add("November");
            add("December");
        }
    };

    public Birthday() {
        for(int i=1; i<=12; i++) {
            String number = String.valueOf(i);
            if(number.length() < 2) number = '0' + number;
            monthMap.put(monthList.get(i-1), number);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("birthday")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "set" -> {
                    String month = event.getOption("month", "", OptionMapping::getAsString);
                    if(month.length() <= 2) {
                        try {
                            Integer.parseInt(month);
                            if(month.length() < 2) month = '0' + month;
                        } catch (NumberFormatException e) {
                            event.reply("Invalid month number specified").setEphemeral(true).queue();
                            return;
                        }
                    } else {
                        month = monthMap.get(month);
                        if(month == null) {
                            event.reply("Invalid month specified").setEphemeral(true).queue();
                            return;
                        }
                    }
                    int day = event.getOption("day", 32, OptionMapping::getAsInt);
                    if(day < 1 || day > 31) {
                        event.reply("Invalid day specified").setEphemeral(true).queue();
                        return;
                    }
                    String hour = event.getOption("hour", "12:00:00", OptionMapping::getAsString);
                    if(isInvalidTimestamp(hour)) {
                        event.reply("Invalid timestamp specified").setEphemeral(true).queue();
                        return;
                    }
                    String date = createDateStringFromArguments(day, month, hour);
                    if(date == null) {
                        event.reply("Invalid date specified").setEphemeral(true).queue();
                        return;
                    }
                    String message = event.getOption("message", "\uD83C\uDF89 Happy birthday, " + event.getUser().getAsMention() + "! \uD83E\uDD73", OptionMapping::getAsString);
                    if(message.isEmpty() || message.length() > 2000) {
                        event.reply("Invalid message length").setEphemeral(true).queue();
                        return;
                    }
                    MessageChannel channel = event.getOption("channel", null, OptionMapping::getAsTextChannel);
                    String channelID;
                    if(channel == null) {
                            channelID = Cashew.birthdayRemindersManager.getDefaultChannel(Objects.requireNonNull(event.getGuild()).getId());
                            if(channelID == null) {
                                channelID = event.getChannel().getId();
                            }
                    } else {
                        channelID = channel.getId();
                    }
                    Database database = Database.getInstance();
                    BirthdayReminder reminder = new BirthdayReminder(0, message, date, channelID, Objects.requireNonNull(event.getGuild()).getId(), event.getUser().getId());
                    if(database.addBirthdayReminder(reminder)) {
                        event.reply("Successfully added a birthday reminder!").setEphemeral(true).queue();
                    } else {
                        event.reply("Something went wrong while adding this birthday reminder").setEphemeral(true).queue();
                    }
                }
                case "delete" -> {
                    Database database = Database.getInstance();
                    if (database.deleteBirthdayReminder(Objects.requireNonNull(event.getGuild()).getId(), event.getUser().getId())) {
                        event.reply("Successfully removed the birthday reminder!").setEphemeral(true).queue();
                    } else {
                        event.reply("Something went wrong while removing your birthday reminder (maybe you didn't have one?)").setEphemeral(true).queue();
                    }
                }
                case "setdefault" -> {
                    if(checkSlashCommandPermissions(event, manageServerPermission)) {
                        MessageChannel channel = event.getOption("channel", null, OptionMapping::getAsTextChannel);
                        if(channel == null) {
                            event.reply("Invalid channel specified").setEphemeral(true).queue();
                            return;
                        }
                        boolean override = Objects.equals(event.getOption("type", "default", OptionMapping::getAsString), "override");
                        Database database = Database.getInstance();
                        BirthdayReminderDefaults defaults = new BirthdayReminderDefaults(Objects.requireNonNull(event.getGuild()).getId(), channel.getId(), override);
                        if(database.addBirthdayRemindersDefaults(defaults)) {
                            event.reply("Default channel added!").setEphemeral(true).queue();
                        } else {
                            event.reply("Something went wrong while executing this command").setEphemeral(true).queue();
                        }
                    } else {
                        event.reply("You do not have permission to use this command").setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().startsWith("birthday")) {
            switch (event.getFocusedOption().getName()) {
                case "month" -> {
                    ArrayList<String> matching = autocompleteFromList(monthList, event.getOption("month", "", OptionMapping::getAsString));
                    event.replyChoiceStrings(matching).queue();
                }
                case "type" -> {
                    ArrayList<String> options = new ArrayList<>() {
                        {
                            add("override");
                            add("default");
                        }
                    };
                    ArrayList<String> matching = autocompleteFromList(options, event.getOption("type", "", OptionMapping::getAsString));
                    event.replyChoiceStrings(matching).queue();
                }
            }
        }
    }

    private String createDateStringFromArguments(int day, String month, String hour) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateNow = LocalDateTime.now();
        String dayString = String.valueOf(day);
        if(dayString.length() == 1) dayString = '0' + dayString;
        try {
            String dateString = dateNow.getYear() + "-" + month + "-" + dayString + " " + hour;
            dateFormat.parse(dateString);
            return dateString;
        } catch (ParseException e) {
            return null;
        }
    }
}
