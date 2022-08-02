package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clear extends BaseCommand {

    private MessageEmbed removeRecentMessages(MessageChannel channel, int count, boolean slashCommand) {
        EmbedBuilder clearEmbed = new EmbedBuilder();
        try {
            List<Message> recentMessages = channel.getHistory().retrievePast(count).complete();
            if (!slashCommand) {
                count--;
            }
            channel.purgeMessages(recentMessages);
            String deleteMessage = " message";
            if (count > 1) {
                deleteMessage += "s";
            }
            deleteMessage += " successfully deleted!";
            clearEmbed.setTitle("✅ " + count + deleteMessage);
            clearEmbed.setColor(0x77B255);
        } catch (NumberFormatException e) {
            clearEmbed.setColor(0xdd2e45);
            clearEmbed.setTitle("❌ Clear failed! Invalid argument: the number of messages to delete that you provided is likely way too big or not a number.");
        } catch (IllegalArgumentException e) {
            clearEmbed.setColor(0xdd2e45);
            if (e.toString().startsWith("java.lang.IllegalArgumentException: Message retrieval")) {
                clearEmbed.setTitle("❌ Clear failed! Invalid argument: you can only delete between 1 and 99 messages at once!");
            } else {
                clearEmbed.setTitle("❌ Clear failed! You can't delete messages that are older than 2 weeks. Change the amount and try again.");
            }
        } catch (InsufficientPermissionException e) {
            clearEmbed.setColor(0xdd2e45);
            clearEmbed.setTitle("❌ Clear failed! Missing MESSAGE_MANAGE permission");
        }
        clearEmbed.setDescription("Click OK to remove this message");
        return clearEmbed.build();
    }

    private MessageEmbed removeMessagesByRange(MessageChannel channel, String rangesInput, boolean slashCommand) {
        EmbedBuilder clearEmbed = new EmbedBuilder();
        Boolean[] toDelete = new Boolean[100];
        for (int i = 0; i < 100; i++) {
            toDelete[i] = false;
        }
        String[] ranges = rangesInput.split("\\s+");
        boolean rangeSplittingFailed = false;
        for (String s : ranges) {
            boolean include = true;
            String range = s;
            if (s.charAt(0) == '-') {
                include = false;
                range = s.substring(1);
            }
            if (range.contains("-")) {
                String[] ends = range.split("-");
                int begin, end;
                try {
                    begin = Integer.parseInt(ends[0]);
                    end = Integer.parseInt(ends[1]);
                } catch (NumberFormatException e) {
                    clearEmbed.setTitle("❌ Clear failed! Invalid range argument: " + range);
                    rangeSplittingFailed = true;
                    break;
                }
                if (begin < 1 || end < 1 || end > 99 || begin > end) {
                    clearEmbed.setTitle("❌ Clear failed! Invalid range: " + range);
                    rangeSplittingFailed = true;
                    break;
                }
                for (int markForDeletion = begin; markForDeletion <= end; markForDeletion++) {
                    toDelete[markForDeletion] = include;
                }
            } else {
                int markForDeletion;
                try {
                    markForDeletion = Integer.parseInt(range);
                } catch (NumberFormatException e) {
                    clearEmbed.setTitle("❌ Clear failed! Invalid range argument: " + range);
                    rangeSplittingFailed = true;
                    break;
                }
                if (markForDeletion < 1 || markForDeletion > 99) {
                    clearEmbed.setTitle("❌ Clear failed! Invalid range - range has to be positive and smaller than 100: " + range);
                    rangeSplittingFailed = true;
                    break;
                }
                toDelete[markForDeletion] = include;
            }
        }
        if (rangeSplittingFailed) {
            clearEmbed.setColor(0xdd2e45);
        } else {
            List<Message> recentMessages = channel.getHistory().retrievePast(100).complete();
            List<Message> toDeleteMessages = new ArrayList<>();
            if (!slashCommand) {
                toDeleteMessages.add(recentMessages.get(0));
            }
            int amount = 0;
            for (int i = 1; i < 100; i++) {
                if (toDelete[i]) {
                    if(slashCommand) {
                        toDeleteMessages.add(recentMessages.get(i-1));
                    } else {
                        toDeleteMessages.add(recentMessages.get(i));
                    }
                    amount++;
                }
            }
            if (amount == 0) {
                clearEmbed.setTitle("✖️ This range does not cover any messages.");
            } else {
                try {
                    channel.purgeMessages(toDeleteMessages);
                    String deleteMessage = " message";
                    if (amount > 1) {
                        deleteMessage += "s";
                    }
                    deleteMessage += " successfully deleted!";
                    clearEmbed.setTitle("✅ " + amount + deleteMessage);
                    clearEmbed.setColor(0x77B255);
                } catch (IllegalArgumentException e) {
                    clearEmbed.setTitle("❌ Clear failed! You can't delete messages that are older than 2 weeks.");
                    clearEmbed.setColor(0xdd2e45);
                } catch (InsufficientPermissionException e) {
                    clearEmbed.setTitle("❌ Clear failed! Missing MESSAGE_MANAGE permission");
                    clearEmbed.setColor(0xdd2e45);
                }

            }
        }
        clearEmbed.setDescription("Click OK to remove this message");
        return clearEmbed.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "clear")) {
            if (checkPermissions(event, modPermissions)) {
                if (event.isWebhookMessage()) return;
                if (args.length < 2) {
                    event.getChannel().sendMessageEmbeds(removeRecentMessages(event.getChannel(), 2, false)).queue(message -> message.addReaction(Emoji.fromUnicode("❌")).queue());
                } else if (args.length == 2) {
                    try {
                        event.getChannel().sendMessageEmbeds(removeRecentMessages(event.getChannel(), Integer.parseInt(args[1]) + 1, false)).queue(message -> message.addReaction(Emoji.fromUnicode("❌")).queue());
                    } catch (NumberFormatException e) {
                        EmbedBuilder clearEmbed = new EmbedBuilder();
                        clearEmbed.setTitle("❌ Clear failed! Invalid argument: recent messages amount was not a number");
                        clearEmbed.setDescription("React with ❌ to delete this message");
                        clearEmbed.setColor(0xdd2e45);
                        event.getChannel().sendMessageEmbeds(clearEmbed.build()).queue(message -> message.addReaction(Emoji.fromUnicode("❌")).queue());
                    }
                } else {
                    StringBuilder ranges = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        ranges.append(args[i]).append(" ");
                    }
                    event.getChannel().sendMessageEmbeds(removeMessagesByRange(event.getChannel(), ranges.toString(), false)).queue(message -> message.addReaction(Emoji.fromUnicode("❌")).queue());
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear")) {
            if(!event.isFromGuild()) {
                event.reply("Clear doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (checkSlashCommandPermissions(event, modPermissions)) {
                int recent = event.getOption("recent", 0, OptionMapping::getAsInt);
                String range = event.getOption("range", "", OptionMapping::getAsString);
                MessageEmbed clearEmbed;
                if (recent == 0 && range.isEmpty()) {
                    clearEmbed = removeRecentMessages(event.getChannel(), 1, true);
                } else if (recent != 0) {
                    clearEmbed = removeRecentMessages(event.getChannel(), recent, true);
                } else {
                    clearEmbed = removeMessagesByRange(event.getChannel(), range, true);
                }
                if(Objects.requireNonNull(clearEmbed.getTitle()).startsWith("✅")) {
                    String buttonID = event.getUser().getId() + ":clear";
                    event.replyEmbeds(clearEmbed).addActionRow(Button.danger(buttonID, "OK")).queue();
                } else {
                    event.replyEmbeds(clearEmbed).setEphemeral(true).queue();
                }
            } else {
                event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if(buttonID.length != 2) {
            return;
        }
        String type = buttonID[1];
        if(!type.equals("clear")) {
            return;
        }
        String userID = buttonID[0];
        if(!event.getUser().getId().equals(userID)) {
            event.reply("You don't have permission to remove this embed.").setEphemeral(true).queue();
            return;
        }
        //success
        event.getMessage().delete().queue();
    }
}