package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.ScheduledMessagesDatabase;
import com.birtek.cashew.timings.ScheduledMessage;
import com.birtek.cashew.timings.ScheduledMessagesManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A {@link net.dv8tion.jda.api.hooks.ListenerAdapter listener} for the /scheduler command, which lets server moderators
 * set up messages that will be sent every day at the given hour, remove them and list them
 */
public class Scheduler extends BaseCommand {

    private long getHourTimestamp(String timeString) {
        return ScheduledMessagesManager.getInstantFromExecutionTime(timeString).toEpochMilli();
    }

    private MessageEmbed generateScheduledMessagesEmbed(ArrayList<ScheduledMessage> messages, User user, Guild server) {
        EmbedBuilder scheduledMessagesEmbed = new EmbedBuilder();
        scheduledMessagesEmbed.setTitle("Messages scheduled on " + server.getName());
        scheduledMessagesEmbed.setThumbnail(server.getIconUrl());
        for (ScheduledMessage message : messages) {
            String messageContentShort = message.getMessageContent().length() > 64 ? message.getMessageContent().substring(0, 64) + "..." : message.getMessageContent();
            scheduledMessagesEmbed.addField(messageContentShort, "In <#" + message.getDestinationChannelID() + ">, " + TimeFormat.TIME_LONG.format(getHourTimestamp(message.getExecutionTime())), false);
        }
        scheduledMessagesEmbed.setFooter("Select a message with the dropdown menu to see it's full content or delete it");
        return scheduledMessagesEmbed.build();
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
                    event.reply("There are no scheduled messages on this server").setEphemeral(true).queue();
                    return;
                }
                MessageEmbed scheduledMessagesEmbed = generateScheduledMessagesEmbed(messages, event.getUser(), event.getGuild());
                event.replyEmbeds(scheduledMessagesEmbed).setEphemeral(true).queue();
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
}