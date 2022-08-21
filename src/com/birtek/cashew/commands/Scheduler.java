package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.ScheduledMessagesDatabase;
import com.birtek.cashew.timings.ScheduledMessage;
import com.birtek.cashew.timings.ScheduledMessagesManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A {@link net.dv8tion.jda.api.hooks.ListenerAdapter listener} for the /scheduler command, which lets server moderators
 * set up messages that will be sent every day at the given hour, remove them and list them
 */
public class Scheduler extends BaseCommand {

    /**
     * Gets a timestamp from Instant's toEpochMilli from the time string being the message send time
     *
     * @param timeString time on which the message is sent every day
     * @return timestamp with the next execution date
     */
    private long getHourTimestamp(String timeString) {
        return ScheduledMessagesManager.getInstantFromExecutionTime(timeString).toEpochMilli();
    }

    /**
     * Generates an {@link MessageEmbed embed} with the provided {@link ScheduledMessage ScheduledMessages} list,
     * showing their basic details and the beginning of the message
     *
     * @param messages   ArrayList of {@link ScheduledMessage ScheduledMessages} representing a whole page of them, a page
     *                   consists of maximum of 10 messages
     * @param server     {@link Guild server} from which the messages are listed
     * @param pageNumber number of the page that will be generated
     * @return a {@link MessageEmbed MessageEmbed} with the title "Scheduled messages on (server)", a list of messages
     * with their beginning in the field name, and the destination channel and the delivery time in the description, the
     * number of the current page in the footer as well as the number of total pages, and the server icon as the
     * thumbnail
     */
    private MessageEmbed generateScheduledMessagesEmbed(ArrayList<ScheduledMessage> messages, Guild server, int pageNumber) {
        EmbedBuilder scheduledMessagesEmbed = new EmbedBuilder();
        scheduledMessagesEmbed.setTitle("Messages scheduled on " + server.getName());
        scheduledMessagesEmbed.setThumbnail(server.getIconUrl());
        for (ScheduledMessage message : messages) {
            String messageContentShort = message.getMessageContent().length() > 64 ? message.getMessageContent().substring(0, 64) + "..." : message.getMessageContent();
            scheduledMessagesEmbed.addField(messageContentShort, "In <#" + message.getDestinationChannelID() + ">, " + TimeFormat.TIME_LONG.format(getHourTimestamp(message.getExecutionTime())), false);
        }
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        int pageCount = database.getScheduledMessagesPageCount(server.getId());
        scheduledMessagesEmbed.setFooter("Page " + pageNumber + " out of " + pageCount);
        return scheduledMessagesEmbed.build();
    }

    /**
     * Creates a {@link Pair Pair} of ActionRows - one of them being a {@link SelectMenu SelectMenu} used to choose
     * messages, and the other one with buttons to interact with them
     *
     * @param messages         ArrayList of {@link ScheduledMessage ScheduledMessages} representing a whole page of them, a page
     *                         consists of maximum of 10 messages
     * @param user             {@link User user} who will be able to interact with the buttons, the user who executed the
     *                         /scheduler list command
     * @param deleteAllConfirm if set to true, will generate with the "delete all confirmation" button instead of the
     *                         "delete all" button
     * @return a {@link Pair Pair} of {@link ActionRow ActionRows} - the first one with a {@link SelectMenu SelectMenu}
     * with which the user selects the message to interact with, and the other one with a set of 5 buttons -
     * "Show details", "Delete", "Delete all/confirm delete all", "<" and ">" (used to switch between pages)
     */
    private Pair<ActionRow, ActionRow> generateScheduledMessagesListActionRows(ArrayList<ScheduledMessage> messages, User user, boolean deleteAllConfirm) {
        SelectMenu.Builder scheduledMessagesSelectMenu = SelectMenu.create(user.getId() + ":scheduler:list")
                .setPlaceholder("Select a message")
                .setRequiredRange(1, 1);
        int index = 0;
        for (ScheduledMessage message : messages) {
            String messageContentShort = message.getMessageContent().length() > 64 ? message.getMessageContent().substring(0, 64) + "..." : message.getMessageContent();
            scheduledMessagesSelectMenu.addOption(messageContentShort, String.valueOf(index));
            index++;
        }
        ArrayList<Button> schedulerListButtons = new ArrayList<>();
        schedulerListButtons.add(Button.success(user.getId() + ":scheduler:details", "Show details"));
        schedulerListButtons.add(Button.danger(user.getId() + ":scheduler:delete", "Delete"));
        if (deleteAllConfirm) {
            schedulerListButtons.add(Button.danger(user.getId() + ":scheduler:deleteall2", "[!] Confirm delete all [!]"));
        } else {
            schedulerListButtons.add(Button.danger(user.getId() + ":scheduler:deleteall", "Delete all"));
        }
        schedulerListButtons.add(Button.primary(user.getId() + ":scheduler:page:0", Emoji.fromUnicode("◀️")));
        schedulerListButtons.add(Button.primary(user.getId() + ":scheduler:page:1", Emoji.fromUnicode("▶️")));
        return Pair.of(ActionRow.of(scheduledMessagesSelectMenu.build()), ActionRow.of(schedulerListButtons));
    }

    /**
     * Deletes selected {@link ScheduledMessage ScheduledMessages} from the database and unschedules them
     *
     * @param server             {@link Guild Server} from which the deletion request came
     * @param scheduledMessageID ID of the message to remove, if set to 0 will delete all of them
     * @return a String with a message telling whether the deletion was successful or not
     */
    private String schedulerDeleteMessages(Guild server, int scheduledMessageID) {
        if (scheduledMessageID == 0) {
            if (Cashew.scheduledMessagesManager.deleteScheduledMessage(0, server.getId())) {
                return "Successfully deleted all Scheduled messages!";
            } else {
                return "Failed to delete the messages";
            }
        } else {
            if (Cashew.scheduledMessagesManager.deleteScheduledMessage(scheduledMessageID, server.getId())) {
                return "Successfully deleted Scheduled message " + scheduledMessageID + "!";
            } else {
                return "Failed to delete the message";
            }
        }
    }

    /**
     * Adds a {@link ScheduledMessage ScheduledMessage} to the database and schedules it
     *
     * @param messageContent content of the {@link ScheduledMessage ScheduledMessage} to add
     * @param timestring     a String containing a timestamp in a HH:MM:SS format with an hour on which the message will be
     *                       sent every day
     * @param channelID      ID of the channel in which the message will be sent
     * @param serverID       ID of the server from which the addition request came
     * @return a String with a message telling whether the addition was successful or not
     */
    private String schedulerAddMessages(String messageContent, String timestring, String channelID, String serverID) {
        int insertID = Cashew.scheduledMessagesManager.addScheduledMessage(new ScheduledMessage(0, messageContent, timestring, channelID), serverID);
        if (insertID != -1) {
            return "Successfully added a new timed message! ID = " + insertID;
        } else {
            return "Something went wrong while adding your message, try again later";
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("scheduler")) {
            if (!event.isFromGuild()) {
                event.reply("Scheduler doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName() == null) {
                event.reply("No command specified (how???)").setEphemeral(true).queue();
            }
            if (event.getSubcommandName().equals("add")) {
                String channelID = event.getOption("channel", "its required bruh", OptionMapping::getAsString);
                String timestamp = event.getOption("time", "its required bruh", OptionMapping::getAsString);
                String message = event.getOption("content", "empty message", OptionMapping::getAsString);
                if (isInvalidTimestamp(timestamp)) {
                    event.reply("Invalid timestamp specified").setEphemeral(true).queue();
                    return;
                }
                String response = schedulerAddMessages(message, timestamp, channelID, Objects.requireNonNull(event.getGuild()).getId());
                event.reply(response).setEphemeral(true).queue();
            } else if (event.getSubcommandName().equals("list")) {
                int page = event.getOption("page", 1, OptionMapping::getAsInt);
                ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
                ArrayList<ScheduledMessage> messages = database.getScheduledMessagesPage(Objects.requireNonNull(event.getGuild()).getId(), page);
                if (messages == null) {
                    event.reply("Something went wrong while fetching the list of scheduled messages, try again later").setEphemeral(true).queue();
                    return;
                }
                if (messages.isEmpty()) {
                    event.reply("This page does not exist").setEphemeral(true).queue();
                    return;
                }
                MessageEmbed scheduledMessagesEmbed = generateScheduledMessagesEmbed(messages, event.getGuild(), page);
                Pair<ActionRow, ActionRow> schedulerListActionRows = generateScheduledMessagesListActionRows(messages, event.getUser(), false);
                event.replyEmbeds(scheduledMessagesEmbed).addActionRows(schedulerListActionRows.getLeft(), schedulerListActionRows.getRight()).setEphemeral(true).queue();
            } else if (event.getSubcommandName().equals("delete")) {
                int id = event.getOption("id", 0, OptionMapping::getAsInt);
                String definitely = event.getOption("all", "", OptionMapping::getAsString);
                if (!definitely.equals("definitely") && id == 0) {
                    event.reply("No messages were deleted").setEphemeral(true).queue();
                    return;
                }
                String response = schedulerDeleteMessages(event.getGuild(), id);
                event.reply(response).setEphemeral(true).queue();
            }
        }
    }

    private void showPage(ButtonInteractionEvent event, int page, boolean deleteAllConfirm) {
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        int pageCount = database.getScheduledMessagesPageCount(Objects.requireNonNull(event.getGuild()).getId());
        page = page < 1 ? 1 : (Math.min(page, pageCount));
        ArrayList<ScheduledMessage> messages = database.getScheduledMessagesPage(Objects.requireNonNull(event.getGuild()).getId(), page);
        if (messages == null) {
            event.reply("Something went wrong while fetching the list of scheduled messages, try again later").setEphemeral(true).queue();
            return;
        }
        if (messages.isEmpty()) {
            event.reply("This page does not exist").setEphemeral(true).queue();
            return;
        }
        MessageEmbed scheduledMessagesEmbed = generateScheduledMessagesEmbed(messages, event.getGuild(), page);
        Pair<ActionRow, ActionRow> schedulerListActionRows = generateScheduledMessagesListActionRows(messages, event.getUser(), deleteAllConfirm);
        event.editMessageEmbeds(scheduledMessagesEmbed).setActionRows(schedulerListActionRows.getLeft(), schedulerListActionRows.getRight()).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if (buttonID.length < 3) return;
        if (buttonID[1].equals("scheduler")) {
            if (!event.getUser().getId().equals(buttonID[0])) {
                event.reply("You can't interact with this embed").setEphemeral(true).queue();
                return;
            }
            switch (buttonID[2]) {
                case "details" -> {

                }
                case "delete" -> {

                }
                case "deleteall" -> {

                }
                case "deleteall2" -> {

                }
                case "page" -> {
                    int page = getPageNumber(event.getMessage().getEmbeds().get(0)) + (buttonID[3].equals("1") ? 1 : -1);
                    showPage(event, page, false);
                }
            }
        }
    }
}