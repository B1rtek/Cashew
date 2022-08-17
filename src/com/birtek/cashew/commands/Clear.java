package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clear extends BaseCommand {

    /**
     * Removes the given messages from the channel where the purge was requested
     *
     * @param messagesToRemove a {@link List List} of {@link Message Messages} to remove, created with
     *                         {@link #getMessagesToRemove(String, SlashCommandInteractionEvent) getMessagesToRemove()}
     * @param event            {@link SlashCommandInteractionEvent event} that triggered the command, used to get the
     *                         channel
     * @return a {@link Pair Pair} of a boolean and a String, where boolean indicates whether the removal was
     * successful, and the String contains the error or success message
     */
    private Pair<Boolean, String> removeMessages(List<Message> messagesToRemove, SlashCommandInteractionEvent event) {
        try {
            event.getChannel().purgeMessages(messagesToRemove);
            String deleteMessage = " message";
            int count = messagesToRemove.size();
            if (count > 1) {
                deleteMessage += "s";
            }
            deleteMessage += " successfully deleted!";
            return Pair.of(true, count + deleteMessage);
        } catch (IllegalArgumentException e) {
            if (e.toString().startsWith("java.lang.IllegalArgumentException: Message retrieval")) {
                return Pair.of(false, "You can only delete between 1 and 100 messages at once");
            } else {
                return Pair.of(false, "You can't delete messages that are older than 2 weeks");
            }
        } catch (InsufficientPermissionException e) {
            return Pair.of(false, "Missing MESSAGE_MANAGE permission");
        }
    }

    /**
     * Generates a list of messages to remove based on the provided ranges
     *
     * @param ranges a String containing selection ranges separated by spaces. The accepted ranges come in four versions
     *               "n", "-n", "n-m" and "-n-m". The first one selects a single message, the second one deselects a
     *               single message, where n points to the index of the message counting from 1 starting from the most
     *               recent message. The third pattern selects all messages between nth and mth message inclusive, and
     *               the fourth one deselects them. All ranges can be combined in a list, so a String like:
     *               "1-7 9 -1 -3-6" will select 2nd, 7th and 9th message. If any number can't be parsed or is above 100
     *               or lower than 1, it'll be considered invalid
     * @param event  {@link SlashCommandInteractionEvent event} that triggered the command, used to get the message
     *               history
     * @return a {@link List List} of selected {@link Message Messages}, an empty list if the selection was invalid, or
     * null if an error occurred while retrieving the message history
     */
    private List<Message> getMessagesToRemove(String ranges, SlashCommandInteractionEvent event) {
        ArrayList<Boolean> selection = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            selection.add(false);
        }
        String[] splitRanges = ranges.split("\\s+");
        for (String range : splitRanges) {
            // recognize the pattern
            String actualRange = range;
            boolean toSet = true;
            int begin, end;
            if (range.startsWith("-")) {
                toSet = false;
                actualRange = range.substring(1);
            }
            if (actualRange.isEmpty()) continue;
            // get data from the pattern
            if (actualRange.contains("-")) {
                String[] beginAndEnd = actualRange.split("-");
                try {
                    begin = Integer.parseInt(beginAndEnd[0]);
                    end = Integer.parseInt(beginAndEnd[1]);
                } catch (NumberFormatException e) {
                    return new ArrayList<>();
                }
            } else {
                try {
                    begin = Integer.parseInt(actualRange);
                    end = begin;
                } catch (NumberFormatException e) {
                    return new ArrayList<>();
                }
            }
            if (begin < 1 || end > 100) return new ArrayList<>();
            // apply the range
            for (int i = begin; i <= end; i++) {
                selection.set(i - 1, toSet);
            }
        }
        // return the selected messages
        List<Message> messages;
        try {
            messages = event.getChannel().getHistory().retrievePast(100).complete();
        } catch (Exception e) {
            return null;
        }
        List<Message> messagesToRemove = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (selection.get(i)) {
                messagesToRemove.add(messages.get(i));
            }
        }
        return messagesToRemove;
    }

    /**
     * Generates an {@link MessageEmbed embed} containing information about how did the message deletion process go
     *
     * @param successful if set to true, will generate a successful embed, with the message telling about that in the
     *                   title and a description telling that clicking the button below will remove the embed, if set
     *                   to false will generate an embed with the description of the error in the embed description
     * @param message    a message telling about how the deletion process went
     * @return a {@link MessageEmbed MessageEmbed} with an error message or telling about successful removal
     */
    private MessageEmbed generateClearEmbed(boolean successful, String message) {
        EmbedBuilder clearEmbed = new EmbedBuilder();
        if (successful) {
            clearEmbed.setTitle("✅ " + message);
            clearEmbed.setDescription("Click OK to remove this message");
            clearEmbed.setColor(0x77B255);
        } else {
            clearEmbed.setTitle("❌ Clear failed!");
            clearEmbed.setDescription(message);
            clearEmbed.setColor(0xdd2e45);
        }
        return clearEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear")) {
            if (!event.isFromGuild()) {
                event.reply("Clear doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            int recent = event.getOption("recent", -1, OptionMapping::getAsInt);
            String ranges = event.getOption("range", "", OptionMapping::getAsString);
            if (recent != -1) {
                ranges = "1-" + recent;
            }
            MessageEmbed clearEmbed;
            List<Message> messagesToRemove = getMessagesToRemove(ranges, event);
            if (messagesToRemove == null) {
                clearEmbed = generateClearEmbed(false, "Failed to retrieve message history");
            } else if (messagesToRemove.isEmpty()) {
                clearEmbed = generateClearEmbed(false, "Invalid range selection");
            } else {
                Pair<Boolean, String> removalResult = removeMessages(messagesToRemove, event);
                clearEmbed = generateClearEmbed(removalResult.getLeft(), removalResult.getRight());
            }
            if (Objects.requireNonNull(clearEmbed.getTitle()).startsWith("✅")) {
                event.replyEmbeds(clearEmbed).addActionRow(Button.danger(event.getUser().getId() + ":clear", "OK")).queue();
            } else {
                event.replyEmbeds(clearEmbed).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if (buttonID.length != 2) return;
        if (!buttonID[1].equals("clear")) return;
        if (!event.getUser().getId().equals(buttonID[0])) {
            event.reply("You can't interact with this button").setEphemeral(true).queue();
            return;
        }
        event.getMessage().delete().queue();
    }
}