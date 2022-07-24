package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.RemindersDatabase;
import com.birtek.cashew.timings.ReminderRunnable;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.ArrayList;
import java.util.Objects;

public class Reminder extends BaseCommand {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reminder")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "set" -> {
                    // check if the current reminders count isn't already at 10 (maximum)
                    RemindersDatabase database = RemindersDatabase.getInstance();
                    if (database.getRemindersCount(event.getUser().getId()) >= 10) {
                        event.reply("You already have the maximum number of reminders set").setEphemeral(true).queue();
                        return;
                    }
                    String content = event.getOption("content", null, OptionMapping::getAsString);
                    if (content == null) {
                        event.reply("Content of the reminder cannot be empty").setEphemeral(true).queue();
                        return;
                    }
                    if (content.length() > 256) {
                        event.reply("Content of the reminder cannot be longer than 256 characters").setEphemeral(true).queue();
                        return;
                    }
                    int time = event.getOption("time", 0, OptionMapping::getAsInt);
                    if (time <= 0) {
                        event.reply("The reminder timer needs to be greater than 0").setEphemeral(true).queue();
                        return;
                    }
                    String unit = event.getOption("unit", "hours", OptionMapping::getAsString);
                    if (!timeUnits.contains(unit)) {
                        event.reply("Invalid time unit specified").setEphemeral(true).queue();
                        return;
                    }
                    boolean ping = event.getOption("ping", true, OptionMapping::getAsBoolean);
                    String timeString = calculateTargetTime(time, unit);
                    ReminderRunnable reminder = new ReminderRunnable(0, ping, content, timeString, event.getUser().getId());
                    int id = Cashew.remindersManager.addReminder(reminder);
                    if (id != -1) {
                        event.reply("Successfully added a reminder! ID = " + id).setEphemeral(true).queue();
                    } else {
                        event.reply("Something went wrong while adding the reminder (Error 2)").setEphemeral(true).queue();
                    }
                }
                case "list" -> {
                    RemindersDatabase database = RemindersDatabase.getInstance();
                    ArrayList<ReminderRunnable> reminders = database.getUserReminders(event.getUser().getId());
                    if (reminders == null) {
                        event.reply("Something went wrong while checking your reminders (Error 3)").setEphemeral(true).queue();
                        return;
                    }
                    if (reminders.isEmpty()) {
                        event.reply("You don't have any reminders set").setEphemeral(true).queue();
                        return;
                    }
                    Table remindersTable = new Table(4, BorderStyle.UNICODE_BOX, ShownBorders.HEADER_AND_COLUMNS);
                    remindersTable.setColumnWidth(0, 2, 10);
                    remindersTable.setColumnWidth(1, 19, 19);
                    remindersTable.setColumnWidth(2, 4, 4);
                    remindersTable.setColumnWidth(3, 1, 48);
                    remindersTable.addCell("ID");
                    remindersTable.addCell("Scheduled for");
                    remindersTable.addCell("Ping");
                    remindersTable.addCell("Reminder content");
                    for (ReminderRunnable reminder : reminders) {
                        remindersTable.addCell(String.valueOf(reminder.getId()));
                        remindersTable.addCell(reminder.getDateTime());
                        remindersTable.addCell(reminder.isPing() ? "yes" : "no");
                        remindersTable.addCell(reminder.getContent());
                    }
                    String tableContent = "```prolog\n" + remindersTable.render() + "\n```";
                    event.reply(tableContent).setEphemeral(true).queue();
                }
                case "delete" -> {
                    int id = event.getOption("id", -1, OptionMapping::getAsInt);
                    if (id == -1) {
                        event.reply("Wrong ID!").setEphemeral(true).queue();
                        return;
                    }
                    int result = Cashew.remindersManager.deleteReminder(id, event.getUser().getId());
                    if (result == 1) {
                        event.reply("Reminder successfully deleted!").setEphemeral(true).queue();
                    } else if (result == -1) {
                        event.reply("Something went wrong while deleting the reminder (Error 1)").setEphemeral(true).queue();
                    } else {
                        event.reply("Reminder with this ID doesn't exist").setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("reminder")) {
            if (event.getFocusedOption().getName().equals("unit")) {
                event.replyChoiceStrings(autocompleteFromList(timeUnits, event.getOption("unit", "", OptionMapping::getAsString))).queue();
            }
        }
    }
}
