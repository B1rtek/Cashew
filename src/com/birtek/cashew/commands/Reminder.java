package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.RemindersDatabase;
import com.birtek.cashew.timings.ReminderRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A {@link net.dv8tion.jda.api.hooks.ListenerAdapter listener} for the /reminder command, which lets users set up to 10
 * reminders that will be delivered to their DMs
 */
public class Reminder extends BaseCommand {

    /**
     * Generates an embed listing all reminders
     *
     * @param reminders ArrayList of {@link ReminderRunnable ReminderRunnables} being a list of user's reminders
     * @param user      User who requested their reminders list
     * @return a {@link MessageEmbed MessageEmbed} containing short descriptions of reminders in the fields, consisting
     * of first 64 characters of the reminder content, and its delivery date in the field description, a thumbnail being
     * user's avatar, and a title "Your reminders"
     */
    private MessageEmbed generateRemindersEmbed(ArrayList<ReminderRunnable> reminders, User user) {
        EmbedBuilder remindersEmbed = new EmbedBuilder();
        remindersEmbed.setTitle("Your reminders");
        remindersEmbed.setThumbnail(user.getAvatarUrl());
        for (ReminderRunnable reminder : reminders) {
            String reminderContentShort = reminder.getContent().length() > 64 ? reminder.getContent().substring(0, 64) + "..." : reminder.getContent();
            remindersEmbed.addField(reminderContentShort, "Arrives " + TimeFormat.RELATIVE.format(calculateMillisFromDateTimeString(reminder.getDateTime())), false);
        }
        remindersEmbed.setFooter("Select a reminder with the dropdown menu to see it's details or remove it");
        return remindersEmbed.build();
    }

    /**
     * Generates a pair of ActionRows, one being a SelectMenu used to select the reminder in the embed, and the other
     * one with buttons which allow for interactions with the items
     *
     * @param reminders        ArrayList of {@link ReminderRunnable ReminderRunnables} being a list of user's reminders
     * @param user             User who requested their reminders list
     * @param deleteAllConfirm if set to true, will generate with the button that confirms deleting all reminders
     * @return a {@link Pair Pair} of {@link ActionRow ActionRows}, the first one with a {@link SelectMenu SelectMenu}
     * and the other one with three {@link Button Buttons} - "Show details", "Delete" and "Delete all"
     */
    private Pair<ActionRow, ActionRow> generateListActionRows(ArrayList<ReminderRunnable> reminders, User user, boolean deleteAllConfirm) {
        SelectMenu.Builder reminderSelectMenu = SelectMenu.create(user.getId() + ":reminder:list")
                .setPlaceholder("Select a reminder")
                .setRequiredRange(1, 1);
        int index = 0;
        for (ReminderRunnable reminder : reminders) {
            String reminderContentShort = reminder.getContent().length() > 64 ? reminder.getContent().substring(0, 64) + "..." : reminder.getContent();
            reminderSelectMenu.addOption(reminderContentShort, String.valueOf(index));
            index++;
        }
        ArrayList<Button> reminderListButtons = new ArrayList<>();
        reminderListButtons.add(Button.success(user.getId() + ":reminder:details", "Show details"));
        reminderListButtons.add(Button.danger(user.getId() + ":reminder:delete", "Delete"));
        if (deleteAllConfirm) {
            reminderListButtons.add(Button.danger(user.getId() + ":reminder:deleteall2", "[!] Confirm delete all [!]"));
        } else {
            reminderListButtons.add(Button.danger(user.getId() + ":reminder:deleteall", "Delete all"));
        }
        return Pair.of(ActionRow.of(reminderSelectMenu.build()), ActionRow.of(reminderListButtons));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reminder")) {
            if (cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
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
                        event.reply("Successfully added a reminder!").setEphemeral(true).queue();
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
                    MessageEmbed remindersEmbed = generateRemindersEmbed(reminders, event.getUser());
                    Pair<ActionRow, ActionRow> actionRows = generateListActionRows(reminders, event.getUser(), false);
                    event.replyEmbeds(remindersEmbed).addActionRows(actionRows.getLeft(), actionRows.getRight()).setEphemeral(true).queue();
                }
//                case "delete" -> {
//                    int id = event.getOption("id", -1, OptionMapping::getAsInt);
//                    if (id == -1) {
//                        event.reply("Wrong ID!").setEphemeral(true).queue();
//                        return;
//                    }
//                    if (id == 0) { // delete all
//                        if (Cashew.remindersManager.deleteAllReminders(event.getUser().getId())) {
//                            event.reply("All reminders successfully deleted!").setEphemeral(true).queue();
//                        } else {
//                            event.reply("Something went wrong while deleting all your reminders").setEphemeral(true).queue();
//                        }
//                    } else {
//                        int result = Cashew.remindersManager.deleteReminder(id, event.getUser().getId());
//                        if (result == 1) {
//                            event.reply("Reminder successfully deleted!").setEphemeral(true).queue();
//                        } else if (result == -1) {
//                            event.reply("Something went wrong while deleting the reminder (Error 1)").setEphemeral(true).queue();
//                        } else {
//                            event.reply("Reminder with this ID doesn't exist").setEphemeral(true).queue();
//                        }
//                    }
//                }
            }
        }
    }

    /**
     * Returns the index of the selected item from the reminders list embed
     *
     * @param remindersListEmbed {@link MessageEmbed MessageEmbed} with the list of user's reminders
     * @return index of the selected (underlined) item, counting from zero, or -1 if no items were selected
     */
    private int getSelectedItemIndex(MessageEmbed remindersListEmbed) {
        int selectedItemIndex = -1, index = 0;
        for (MessageEmbed.Field field : remindersListEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).startsWith("__")) {
                selectedItemIndex = index;
                break;
            }
            index++;
        }
        return selectedItemIndex;
    }

    /**
     * Generates an Instant from the provided time string and returns the toEpochMilli() output of it
     *
     * @param dateTimeString execution time set by the user who created the {@link ReminderRunnable reminder}
     * @return result of executing toEpochMilli() on the calculated Instant
     */
    private long calculateMillisFromDateTimeString(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime timeOfExecution = LocalDateTime.parse(dateTimeString, dateTimeFormatter).atZone(ZoneId.of("Europe/Warsaw"));
        Instant instantOfExecution = timeOfExecution.toInstant();
        return instantOfExecution.toEpochMilli();
    }

    /**
     * Generates an embed showing details of the given reminder
     *
     * @param reminder {@link ReminderRunnable ReminderRunnable} to show the details off
     * @return a {@link MessageEmbed MessageEmbed}
     */
    private MessageEmbed generateReminderDetailsEmbed(ReminderRunnable reminder) {
        EmbedBuilder reminderDetailsEmbed = new EmbedBuilder();
        reminderDetailsEmbed.setTitle("Reminder details");
        reminderDetailsEmbed.addField("Full content", reminder.getContent(), false);
        reminderDetailsEmbed.addField("Will arrive on", TimeFormat.DATE_TIME_LONG.format(calculateMillisFromDateTimeString(reminder.getDateTime())), false);
        reminderDetailsEmbed.addField("Ping", reminder.isPing() ? "Yes" : "No", false);
        return reminderDetailsEmbed.build();
    }

    /**
     * Swaps the reminders list embed for the one showing the selected embed's details
     *
     * @param event {@link ButtonInteractionEvent event} triggered by the user clicking the "Show details" button
     */
    private void showDetails(ButtonInteractionEvent event) {
        MessageEmbed remindersListEmbed = event.getMessage().getEmbeds().get(0);
        int selectedItemIndex = getSelectedItemIndex(remindersListEmbed);
        if (selectedItemIndex == -1) { // nothing was selected
            event.reply("Select a reminder first").setEphemeral(true).queue();
            return;
        }
        RemindersDatabase database = RemindersDatabase.getInstance();
        ReminderRunnable reminder = database.getUsersReminderByIndex(event.getUser().getId(), selectedItemIndex);
        if(reminder == null) {
            event.reply("This reminder does not exist").setEphemeral(true).queue();
            return;
        }
        MessageEmbed reminderDetailsEmbed = generateReminderDetailsEmbed(reminder);
        ActionRow reminderDetailsButtons = ActionRow.of(
                Button.secondary(event.getUser().getId() + ":reminder:back", "Back"),
                Button.danger(event.getUser().getId() + ":reminder:delete:" + selectedItemIndex, "Delete"));
        event.editMessageEmbeds(reminderDetailsEmbed).setActionRows(reminderDetailsButtons).queue();
    }

    /**
     * Returns to the reminders list after an action was performed
     *
     * @param event {@link ButtonInteractionEvent event} that triggered refreshing the list
     */
    private void showRemindersList(ButtonInteractionEvent event, boolean deleteAllConfirm) {
        RemindersDatabase database = RemindersDatabase.getInstance();
        ArrayList<ReminderRunnable> reminders = database.getUserReminders(event.getUser().getId());
        if (reminders == null) {
            event.editMessage("Something went wrong while checking your reminders (Error 3)").setEmbeds().setActionRow().queue();
            return;
        }
        if (reminders.isEmpty()) {
            event.editMessage("You don't have any reminders set").setEmbeds().setActionRows().queue();
            return;
        }
        MessageEmbed remindersEmbed = generateRemindersEmbed(reminders, event.getUser());
        Pair<ActionRow, ActionRow> actionRows = generateListActionRows(reminders, event.getUser(), deleteAllConfirm);
        event.editMessageEmbeds(remindersEmbed).setActionRows(actionRows.getLeft(), actionRows.getRight()).queue();
    }

    /**
     * Deletes a reminder and returns to the reminders list
     *
     * @param event {@link ButtonInteractionEvent event} being a button press that triggered the deletion
     * @param index if set to anything else than -1, will be used as the index of the reminder to delete
     */
    private void deleteReminder(ButtonInteractionEvent event, int index) {
        int selectedItemIndex = index;
        if (selectedItemIndex == -1) {
            MessageEmbed remindersListEmbed = event.getMessage().getEmbeds().get(0);
            selectedItemIndex = getSelectedItemIndex(remindersListEmbed);
        }
        if (selectedItemIndex == -1) { // nothing was selected
            event.reply("Select a reminder first").setEphemeral(true).queue();
            return;
        }
        RemindersDatabase database = RemindersDatabase.getInstance();
        ReminderRunnable reminder = database.getUsersReminderByIndex(event.getUser().getId(), selectedItemIndex);
        if(reminder == null) {
            event.reply("This reminder does not exist").setEphemeral(true).queue();
            return;
        }
        if (Cashew.remindersManager.deleteReminder(reminder.getId(), event.getUser().getId()) != 1) {
            event.reply("Something went wrong while deleting the reminder").setEphemeral(true).queue();
            return;
        }
        showRemindersList(event, false);
    }

    /**
     * Deletes all reminders and removes the message
     *
     * @param event {@link ButtonInteractionEvent event} triggered by pressing the "Delete all" button twice
     */
    private void deleteAllReminders(ButtonInteractionEvent event) {
        if(Cashew.remindersManager.deleteAllReminders(event.getUser().getId())) {
            event.editMessage("Successfully removed all reminders!").setEmbeds().setActionRows().queue();
        } else {
            event.reply("Something went wrong while removing all reminders").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if (buttonID.length < 3) return;
        if (buttonID[1].equals("reminder")) {
            if (!event.getUser().getId().equals(buttonID[0])) {
                event.reply("You can't interact with this button").setEphemeral(true).queue();
                return;
            }
            switch (buttonID[2]) {
                case "details" -> showDetails(event);
                case "delete" -> {
                    int index = buttonID.length == 4 ? Integer.parseInt(buttonID[3]) : -1;
                    deleteReminder(event, index);
                }
                case "deleteall" -> showRemindersList(event, true);
                case "deleteall2" -> deleteAllReminders(event);
                case "back" -> showRemindersList(event, false);
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String[] menuID = event.getComponentId().split(":");
        if (menuID.length < 3) return;
        if (menuID[1].equals("reminder")) {
            if (!event.getUser().getId().equals(menuID[0])) {
                event.reply("You can't interact with this select menu").setEphemeral(true).queue();
                return;
            }
            MessageEmbed remindersListEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder selectedReminderEmbed = new EmbedBuilder();
            selectedReminderEmbed.setTitle(remindersListEmbed.getTitle());
            selectedReminderEmbed.setThumbnail(Objects.requireNonNull(remindersListEmbed.getThumbnail()).getUrl());
            selectedReminderEmbed.setFooter(Objects.requireNonNull(remindersListEmbed.getFooter()).getText());
            int index = 0;
            for (MessageEmbed.Field field : remindersListEmbed.getFields()) {
                String fieldName = field.getName();
                assert fieldName != null;
                if (fieldName.startsWith("__")) {
                    fieldName = fieldName.substring(2, fieldName.length() - 2);
                }
                if (index == Integer.parseInt(event.getSelectedOptions().get(0).getValue())) {
                    selectedReminderEmbed.addField("__" + fieldName + "__", Objects.requireNonNull(field.getValue()), field.isInline());
                } else {
                    selectedReminderEmbed.addField(fieldName, Objects.requireNonNull(field.getValue()), field.isInline());
                }
                index++;
            }
            event.editMessageEmbeds(selectedReminderEmbed.build()).queue();
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
