package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.ScheduledMessagesDatabase;
import com.birtek.cashew.timings.ScheduledMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A {@link net.dv8tion.jda.api.hooks.ListenerAdapter listener} for the /scheduler command, which lets server moderators
 * set up messages that will be sent every day at the given hour, remove them and list them
 */
public class Scheduler extends BaseCommand {

    Permission[] timedMessageCommandPermissions = {
            Cashew.moderatorPermission
    };

    /**
     * Returns a table listing selected {@link ScheduledMessage ScheduledMessages}
     *
     * @param server             {@link Guild Server} object representing the server from which the request came
     * @param scheduledMessageID ID of the message to show, if set to 0 will show all of them
     * @return a String containing a table with all messages selected to be shown, or an error message explaining why
     * that failed
     */
    private String schedulerListMessages(Guild server, int scheduledMessageID) {
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ArrayList<ScheduledMessage> scheduledMessages;
        if (scheduledMessageID == 0) {
            scheduledMessages = database.getScheduledMessages(0, server.getId());
        } else {
            scheduledMessages = database.getScheduledMessages(scheduledMessageID, server.getId());
        }
        if (scheduledMessages == null) return "Something went wrong while fetching the messages, try again later";
        if (scheduledMessages.isEmpty()) {
            if (scheduledMessageID == 0) {
                return "There are no defined Scheduled messages yet.";
            } else {
                return "Message with this ID doesn't exist.";
            }
        }
        Table messagesTable = new Table(4, BorderStyle.UNICODE_BOX, ShownBorders.HEADER_AND_COLUMNS);
        messagesTable.setColumnWidth(0, 2, 10);
        messagesTable.setColumnWidth(1, 8, 8);
        messagesTable.setColumnWidth(2, 7, 20);
        messagesTable.setColumnWidth(3, 7, 80);
        messagesTable.addCell("ID");
        messagesTable.addCell("Time");
        messagesTable.addCell("Channel");
        messagesTable.addCell("Message");
        for (ScheduledMessage message : scheduledMessages) {
            messagesTable.addCell(String.valueOf(message.getId()));
            messagesTable.addCell(message.getExecutionTime());
            GuildChannel channel = server.getGuildChannelById(message.getDestinationChannelID());
            messagesTable.addCell(channel == null ? message.getDestinationChannelID() : channel.getName());
            messagesTable.addCell(message.getMessageContent());
        }
        return "```prolog\n" + messagesTable.render() + "\n```";
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
            if (isPrivateChannel(event)) {
                event.reply("Scheduler doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (checkSlashCommandPermissions(event, timedMessageCommandPermissions)) {
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
                    int id = event.getOption("id", 0, OptionMapping::getAsInt);
                    String response = schedulerListMessages(event.getGuild(), id);
                    event.reply(response).queue();
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
            } else {
                event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            }
        }
    }
}