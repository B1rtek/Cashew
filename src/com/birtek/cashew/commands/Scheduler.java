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
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
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
        StringSelectMenu.Builder scheduledMessagesSelectMenu = StringSelectMenu.create(user.getId() + ":scheduler:list")
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
     * Creates an {@link MessageEmbed embed} with details about a {@link ScheduledMessage ScheduledMessage}
     *
     * @param message {@link ScheduledMessage ScheduledMessage} which this embed will describe in detail
     * @return a {@link MessageEmbed MessageEmbed} with the full content, destination channel and localized arrival time
     * in separate fields of the embed
     */
    private MessageEmbed generateScheduledMessageDetailsEmbed(ScheduledMessage message) {
        EmbedBuilder scheduledMessageDetailsEmbed = new EmbedBuilder();
        scheduledMessageDetailsEmbed.setTitle("Scheduled message details");
        scheduledMessageDetailsEmbed.addField("Full content", message.getMessageContent(), false);
        scheduledMessageDetailsEmbed.addField("Destination channel", "<#" + message.getDestinationChannelID() + ">", false);
        scheduledMessageDetailsEmbed.addField("Arrival time", TimeFormat.TIME_LONG.format(getHourTimestamp(message.getExecutionTime())), false);
        return scheduledMessageDetailsEmbed.build();
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
            return "Successfully added a new scheduled message!";
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
                int pageCount = database.getScheduledMessagesPageCount(Objects.requireNonNull(event.getGuild()).getId());
                page = page < 1 ? 1 : (Math.min(page, pageCount));
                if (messages == null) {
                    event.reply("Something went wrong while fetching the list of scheduled messages, try again later").setEphemeral(true).queue();
                    return;
                }
                if (messages.isEmpty()) {
                    event.reply("There are no scheduled messages on this server").setEphemeral(true).queue();
                    return;
                }
                MessageEmbed scheduledMessagesEmbed = generateScheduledMessagesEmbed(messages, event.getGuild(), page);
                Pair<ActionRow, ActionRow> schedulerListActionRows = generateScheduledMessagesListActionRows(messages, event.getUser(), false);
                event.replyEmbeds(scheduledMessagesEmbed).addComponents(schedulerListActionRows.getLeft(), schedulerListActionRows.getRight()).setEphemeral(true).queue();
            }
        }
    }

    /**
     * Shows the chosen page of the scheduled messages list
     *
     * @param event            {@link ButtonInteractionEvent event} that triggered a refresh of the scheduled messages list embed
     * @param page             number of the page to show
     * @param deleteAllConfirm if set to true, will set the second action row to the version with the
     *                         "delete all confirm" button
     */
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
            event.editMessage("There are no scheduled messages on this server").setEmbeds().setComponents().queue();
            return;
        }
        MessageEmbed scheduledMessagesEmbed = generateScheduledMessagesEmbed(messages, event.getGuild(), page);
        Pair<ActionRow, ActionRow> schedulerListActionRows = generateScheduledMessagesListActionRows(messages, event.getUser(), deleteAllConfirm);
        event.editMessageEmbeds(scheduledMessagesEmbed).setComponents(schedulerListActionRows.getLeft(), schedulerListActionRows.getRight()).queue();
    }

    /**
     * Shows an embed with a scheduled message's full content and all of its details
     *
     * @param event {@link ButtonInteractionEvent event} triggered with the "Show details" button press
     */
    private void showDetails(ButtonInteractionEvent event) {
        MessageEmbed messagesListEmbed = event.getMessage().getEmbeds().get(0);
        int chosenMessageIndex = getSelectedItemIndex(messagesListEmbed);
        if (chosenMessageIndex == -1) {
            event.reply("Select a message first").setEphemeral(true).queue();
            return;
        }
        int pageNumber = getPageNumber(messagesListEmbed);
        chosenMessageIndex = (pageNumber - 1) * 10 + chosenMessageIndex;
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ScheduledMessage messageToShow = database.getScheduledMessageByIndex(Objects.requireNonNull(event.getGuild()).getId(), chosenMessageIndex);
        if (messageToShow == null) {
            event.reply("This message does not exist").setEphemeral(true).queue();
            return;
        }
        MessageEmbed scheduledMessageDetailsEmbed = generateScheduledMessageDetailsEmbed(messageToShow);
        ActionRow scheduledMessageDetailsActionRow = ActionRow.of(
                Button.secondary(event.getUser().getId() + ":scheduler:back:" + pageNumber, "Back"),
                Button.danger(event.getUser().getId() + ":scheduler:delete:" + chosenMessageIndex, "Delete")
        );
        event.editMessageEmbeds(scheduledMessageDetailsEmbed).setComponents(scheduledMessageDetailsActionRow).queue();
    }

    /**
     * Deletes the selected message and refreshes the embed
     *
     * @param event              {@link ButtonInteractionEvent event} that was triggered by clicking the "delete all confirm" button
     * @param chosenMessageIndex if not set to -1, will be used as the index of the message to remove
     */
    private void deleteMessage(ButtonInteractionEvent event, int chosenMessageIndex) {
        int pageNumber;
        if (chosenMessageIndex == -1) {
            MessageEmbed messagesListEmbed = event.getMessage().getEmbeds().get(0);
            chosenMessageIndex = getSelectedItemIndex(messagesListEmbed);
            pageNumber = getPageNumber(messagesListEmbed);
        } else {
            pageNumber = chosenMessageIndex / 10 + 1;
        }
        if (chosenMessageIndex == -1) {
            event.reply("Select a message first").setEphemeral(true).queue();
            return;
        }
        ScheduledMessagesDatabase database = ScheduledMessagesDatabase.getInstance();
        ScheduledMessage messageToDelete = database.getScheduledMessageByIndex(Objects.requireNonNull(event.getGuild()).getId(), chosenMessageIndex);
        if (messageToDelete == null) {
            event.reply("This message does not exist").setEphemeral(true).queue();
            return;
        }
        if (Cashew.scheduledMessagesManager.deleteScheduledMessage(messageToDelete.getId(), event.getGuild().getId())) {
            showPage(event, pageNumber, false);
        } else {
            event.reply("Something went wrong while removing the message").setEphemeral(true).queue();
        }
    }

    /**
     * Deletes all messages and refreshes the embed
     *
     * @param event {@link ButtonInteractionEvent event} that was triggered by clicking the "delete all confirm" button
     */
    private void deleteAll(ButtonInteractionEvent event) {
        if (Cashew.scheduledMessagesManager.deleteScheduledMessage(0, Objects.requireNonNull(event.getGuild()).getId())) {
            showPage(event, 1, true);
        } else {
            event.reply("Something went wrong while removing the scheduled messages, try again later").setEphemeral(true).queue();
        }
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
                case "details" -> showDetails(event);
                case "delete" -> {
                    int index = buttonID.length == 4 ? Integer.parseInt(buttonID[3]) : -1;
                    deleteMessage(event, index);
                }
                case "deleteall" -> showPage(event, getPageNumber(event.getMessage().getEmbeds().get(0)), true);
                case "deleteall2" -> deleteAll(event);
                case "page" -> {
                    int page = getPageNumber(event.getMessage().getEmbeds().get(0)) + (buttonID[3].equals("1") ? 1 : -1);
                    showPage(event, page, false);
                }
                case "back" -> showPage(event, Integer.parseInt(buttonID[3]), false);
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String[] menuID = event.getComponentId().split(":");
        if (menuID.length < 3) return;
        if (menuID[1].equals("scheduler")) {
            if (!event.getUser().getId().equals(menuID[0])) {
                event.reply("You can't interact with this select menu").setEphemeral(true).queue();
                return;
            }
            MessageEmbed scheduledMessagesListEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder selectedScheduledMessageListEmbed = new EmbedBuilder();
            selectedScheduledMessageListEmbed.setTitle(scheduledMessagesListEmbed.getTitle());
            selectedScheduledMessageListEmbed.setThumbnail(Objects.requireNonNull(scheduledMessagesListEmbed.getThumbnail()).getUrl());
            selectedScheduledMessageListEmbed.setFooter(Objects.requireNonNull(scheduledMessagesListEmbed.getFooter()).getText());
            int index = 0;
            for (MessageEmbed.Field field : scheduledMessagesListEmbed.getFields()) {
                String fieldName = field.getName();
                assert fieldName != null;
                if (fieldName.startsWith("__")) {
                    fieldName = fieldName.substring(2, fieldName.length() - 2);
                }
                if (index == Integer.parseInt(event.getSelectedOptions().get(0).getValue())) {
                    selectedScheduledMessageListEmbed.addField("__" + fieldName + "__", Objects.requireNonNull(field.getValue()), field.isInline());
                } else {
                    selectedScheduledMessageListEmbed.addField(fieldName, Objects.requireNonNull(field.getValue()), field.isInline());
                }
                index++;
            }
            event.editMessageEmbeds(selectedScheduledMessageListEmbed.build()).queue();
        }
    }
}