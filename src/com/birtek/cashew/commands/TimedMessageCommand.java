package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.ScheduledMessagesDatabase;
import com.birtek.cashew.timings.ScheduledMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class TimedMessageCommand extends BaseCommand {

    Permission[] timedMessageCommandPermissions = {
            Permission.MANAGE_CHANNEL
    };

    private String schedulerListMessages(Guild server, String sscheduledMessageID, int scheduledMessageID) {
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ArrayList<ScheduledMessage> scheduledMessages;
        if (sscheduledMessageID.isEmpty() && scheduledMessageID == 0) {
            scheduledMessages = database.getScheduledMessages(0, server.getId());
        } else {
            if (sscheduledMessageID.isEmpty()) {
                scheduledMessages = database.getScheduledMessages(Integer.parseInt(sscheduledMessageID), server.getId());
            } else {
                if (sscheduledMessageID.equalsIgnoreCase("all")) {
                    scheduledMessages = database.getScheduledMessages(0, server.getId());
                } else {
                    if (!isNumeric(sscheduledMessageID)) {
                        return "The provided ID is not a number.";
                    }
                    scheduledMessages = database.getScheduledMessages(Integer.parseInt(sscheduledMessageID), server.getId());
                }
            }
        }
        if (scheduledMessages != null) {
            StringBuilder table = new StringBuilder("```prolog\n");
            table.append("ID        |Time    |Channel             |Message                       \n");
            int rowCount = 0;
            for (ScheduledMessage scheduledMessage : scheduledMessages) {
                rowCount++;
                StringBuilder id = new StringBuilder(String.valueOf(scheduledMessage.getId()));
                id.setLength(10);
                String stringId = id.toString().replace('\u0000', ' ');
                table.append(stringId).append("|");
                table.append(scheduledMessage.getExecutionTime()).append("|");
                String channelID = scheduledMessage.getDestinationChannelID();
                StringBuilder channel = new StringBuilder(Objects.requireNonNull(server.getGuildChannelById(channelID)).getName());
                String stringChannel;
                if (channel.length() > 20) {
                    channel.setLength(17);
                    stringChannel = channel.toString().replace('\u0000', ' ') + "...";
                    //channel.append("...");
                } else {
                    channel.setLength(20);
                    stringChannel = channel.toString().replace('\u0000', ' ');
                }
                table.append(stringChannel).append("|");
                StringBuilder message = new StringBuilder(scheduledMessage.getMessageContent());
                String stringMessage;
                if (message.length() > 80) {
                    message.setLength(77);
                    stringMessage = message.toString().replace('\u0000', ' ') + "...";
                    //message.append("...");
                } else {
                    message.setLength(80);
                    stringMessage = message.toString().replace('\u0000', ' ');
                }
                stringMessage = stringMessage.replace("\n", "\\n");
                table.append(stringMessage).append("\n");
            }
            table.append("```");
            if (rowCount == 0) {
                if (sscheduledMessageID.equals("all") || (sscheduledMessageID.isEmpty() && scheduledMessageID == 0)) {
                    return "There are no defined TimedMessages yet.";
                } else {
                    return "Message with this ID doesn't exist.";
                }
            } else {
                return table.toString();
            }
        } else {
            return "[3] Something went wrong...";
        }
    }

    private String schedulerDeleteMessages(Guild server, String sscheduledMessageID, int scheduledMessageID) {
        if (sscheduledMessageID.equalsIgnoreCase("all") || (sscheduledMessageID.isEmpty() && scheduledMessageID == 0)) {
            if (Cashew.scheduledMessagesManager.deleteScheduledMessage(0, server.getId())) {
                return "Successfully deleted all timed messages!";
            } else {
                return "Failed to delete the messages";
            }
        } else {
            if (scheduledMessageID == 0) {
                if (!isNumeric(sscheduledMessageID)) {
                    return "The provided ID is not a number.";
                }
                scheduledMessageID = Integer.parseInt(sscheduledMessageID);
            }
            if (Cashew.scheduledMessagesManager.deleteScheduledMessage(scheduledMessageID, server.getId())) {
                return "Successfully deleted timed message " + scheduledMessageID + "!";
            } else {
                return "Failed to delete the message";
            }
        }
    }

    private String schedulerAddMessages(String messageContent, String timestring, String channelID, String serverID) {
        int insertID = Cashew.scheduledMessagesManager.addScheduledMessage(new ScheduledMessage(0, messageContent, timestring, channelID), serverID);
        if (insertID != -1) {
            return "Successfully added a new timed message! ID = " + insertID;
        } else {
            return "[5] Something went wrong...";
        }
    }

    private String compileMessage(String[] args) {
        StringBuilder messageContent = new StringBuilder(args[3]);
        for (int i = 4; i < args.length; i++) {
            messageContent.append(" ").append(args[i]);
        }
        String messageContentString = messageContent.toString().replace("\"", "''");
        messageContentString = messageContentString.replace("\\n", "\n");
        return messageContentString;
    }

    private boolean isValidMessage(String messageContentString) {
        return messageContentString.length() <= 2000;
    }

    private String compileChannel(String channelIDString) {
        return channelIDString.substring(2, channelIDString.length() - 1);
    }

    private boolean isValidChannel(String channelIDString, Guild server) {
        if (channelIDString.length() < 18) {
            return false;
        }
        String channelID = compileChannel(channelIDString);
        if (channelID.length() != 18 || !isNumeric(channelID)) {
            return false;
        }
        return server.getGuildChannelById(channelID) != null;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "scheduler")) {
            if (checkPermissions(event, timedMessageCommandPermissions)) {
                if (event.isWebhookMessage()) return;
                if (args.length < 4 && args.length != 3 && args.length != 2) {
                    event.getMessage().reply("Incorrect syntax. Please specify all the arguments (<destination channel> <time> <message> or <action> <id>).").mentionRepliedUser(false).queue();
                } else if (args.length == 3 || args.length == 2) {
                    if (args[1].equalsIgnoreCase("show") || args[1].equalsIgnoreCase("list")) {
                        String response = schedulerListMessages(event.getGuild(), args.length == 3 ? args[2] : "", 0);
                        event.getMessage().reply(response).mentionRepliedUser(false).queue();
                    } else if ((args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove")) && args.length == 3) {
                        String response = schedulerDeleteMessages(event.getGuild(), args[2], 0);
                        event.getMessage().reply(response).mentionRepliedUser(false).queue();
                    } else {
                        event.getMessage().reply("Incorrect syntax. Please specify all the arguments (<action> <id>).").mentionRepliedUser(false).queue();
                    }
                } else {
                    String scheduledMessage = compileMessage(args);
                    if (!isValidMessage(scheduledMessage)) {
                        event.getMessage().reply("Invalid message content specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    if (!isValidChannel(args[1], event.getGuild())) {
                        event.getMessage().reply("Invalid channel specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    if (isInvalidTimestamp(args[2])) {
                        event.getMessage().reply("Invalid timestamp specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    String response = schedulerAddMessages(scheduledMessage, args[2], compileChannel(args[1]), event.getGuild().getId());
                    event.getMessage().reply(response).mentionRepliedUser(false).queue();
                }
            } else {
                event.getMessage().delete().complete();
            }
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
                    event.reply(response).queue();
                } else if (event.getSubcommandName().equals("list")) {
                    int id = event.getOption("id", 0, OptionMapping::getAsInt);
                    String response = schedulerListMessages(event.getGuild(), "", id);
                    event.reply(response).queue();
                } else if (event.getSubcommandName().equals("delete")) {
                    int id = event.getOption("id", 0, OptionMapping::getAsInt);
                    String definitely = event.getOption("all", "", OptionMapping::getAsString);
                    if (!definitely.equals("definitely") && id == 0) {
                        event.reply("No messages were deleted").setEphemeral(true).queue();
                        return;
                    }
                    String response = schedulerDeleteMessages(event.getGuild(), "", id);
                    event.reply(response).queue();
                }
            } else {
                event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            }
        }
    }
}