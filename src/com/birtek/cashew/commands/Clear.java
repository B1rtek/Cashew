package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
                } else if (args.length == 2) {
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
                } else {
                    Boolean[] toDelete = new Boolean[100];
                    for(int i=0; i<100; i++) {
                        toDelete[i] = false;
                    }
                    for(int i=2; i<args.length; i++) {
                        boolean include = true;
                        String range = args[i];
                        if (args[i].charAt(0) == '-') {
                            include = false;
                            range = args[i].substring(1);
                        }
                        if(range.contains("-")) {
                            String[] ends = range.split("-");
                            int begin, end;
                            try {
                                begin = Integer.parseInt(ends[0]);
                                end = Integer.parseInt(ends[1]);
                            } catch (NumberFormatException e) {
                                event.getMessage().reply("Invalid range argument: "+range).mentionRepliedUser(false).queue();
                                return;
                            }
                            if (begin < 1 || end < 1 || end > 99 || begin > end) {
                                event.getMessage().reply("Invalid range: "+range).mentionRepliedUser(false).queue();
                                return;
                            }
                            for(int markForDeletion=begin; markForDeletion<=end; markForDeletion++) {
                                toDelete[markForDeletion] = include;
                            }
                        } else {
                            int markForDeletion;
                            try {
                                markForDeletion = Integer.parseInt(range);
                            } catch (NumberFormatException e) {
                                event.getMessage().reply("Invalid range argument: "+range).mentionRepliedUser(false).queue();
                                return;
                            }
                            if(markForDeletion < 1 || markForDeletion > 99) {
                                event.getMessage().reply("Invalid range - range has to be positive and smaller than 100: "+range).mentionRepliedUser(false).queue();
                                return;
                            }
                            toDelete[markForDeletion] = include;
                        }
                    }
                    List<Message> recentMessages = event.getChannel().getHistory().retrievePast(100).complete();
                    List<Message> toDeleteMessages = new ArrayList<>();
                    toDeleteMessages.add(recentMessages.get(0));
                    int amount = 0;
                    for(int i=1; i<100; i++) {
                        if(toDelete[i]) {
                            toDeleteMessages.add(recentMessages.get(i));
                            amount++;
                        }
                    }
                    if(amount==0) {
                        event.getMessage().reply("This range does not cover any messages.").mentionRepliedUser(false).queue();
                        return;
                    }
                    try {
                        event.getChannel().deleteMessages(toDeleteMessages).queue();
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage("You can't delete messages that are older than 2 weeks.").queue();
                        return;
                    }
                    EmbedBuilder success = new EmbedBuilder();
                    success.setTitle("✅ Messages successfully deleted!");
                    success.setDescription("React with ❌ to delete this message");
                    success.setColor(0x77B255);
                    event.getChannel().sendMessageEmbeds(success.build()).queue(message -> message.addReaction("❌").queue());
                    success.clear();
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }
}