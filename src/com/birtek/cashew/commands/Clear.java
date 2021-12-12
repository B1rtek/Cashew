package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Clear extends BaseCommand {

    Permission[] clearCommandPermissions = {
            Permission.MESSAGE_MANAGE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "clear")) {
            if (checkPermissions(event, clearCommandPermissions)) {
                if (event.isWebhookMessage()) return;
                if (args.length < 2) {
                    event.getChannel().sendMessage("Invalid argument: please provide the number of messages that you want to delete.").queue();
                } else {
                    try {
                        int count = Integer.parseInt(args[1]) + 1;
                        List<Message> recentMessages = event.getChannel().getHistory().retrievePast(count).complete();
                        count--;
                        event.getChannel().deleteMessages(recentMessages).queue();
                        EmbedBuilder success = new EmbedBuilder();
                        String deleteMessage = " message";
                        if (count > 1) {
                            deleteMessage += "s";
                        }
                        deleteMessage += " successfully deleted!";
                        success.setTitle("✅ " + count + deleteMessage);
                        success.setDescription("React with ❌ to delete this message");
                        success.setColor(0x77B255);
                        event.getChannel().sendMessageEmbeds(success.build()).queue(message -> message.addReaction("❌").queue());
                        success.clear();
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("Invalid argument: the number of messages to delete that you provided is likely way too big or not a number.").queue();
                    } catch (IllegalArgumentException e) {
                        if (e.toString().startsWith("java.lang.IllegalArgumentException: Message retrieval")) {
                            event.getChannel().sendMessage("Invalid argument: you can only delete between 1 and 99 messages at once!").queue();
                        } else {
                            event.getChannel().sendMessage("You can't delete messages that are older than 2 weeks. Change the amount and try again.").queue();
                        }
                    }
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }
}