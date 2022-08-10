package com.birtek.cashew.commands;

import com.birtek.cashew.database.CountingDatabase;
import com.birtek.cashew.database.CountingInfo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Counting extends BaseCommand {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("counting")) {
            if (!event.isFromGuild()) {
                event.reply("Counting doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (checkSlashCommandPermissions(event, modPermissions)) {
                if (Objects.equals(event.getSubcommandName(), "toggle")) {
                    String toggle = event.getOption("toggle", "", OptionMapping::getAsString);
                    if (!toggle.isEmpty() && (toggle.equals("on") || toggle.equals("off"))) {
                        if (saveToDatabase(toggle, event.getChannel().getId())) {
                            event.reply("Counting has been turned " + toggle + " in this channel.").queue();
                        } else {
                            event.reply("Something went wrong while toggling the counting game in this channel").setEphemeral(true).queue();
                        }
                    }
                } else if (Objects.equals(event.getSubcommandName(), "setcount")) {
                    int newCount = event.getOption("count", -2147483647, OptionMapping::getAsInt);
                    if (newCount != -2147483647) {
                        CountingDatabase database = CountingDatabase.getInstance();
                        CountingInfo countingInfo = database.getCountingData(event.getChannel().getId());
                        if (countingInfo != null) {
                            if (database.setCount(new CountingInfo(true, " ", newCount, " ", countingInfo.typosLeft()), event.getChannel().getId())) {
                                event.reply("Counter has been set to ` " + newCount + " `. The next number is ` " + (newCount + 1) + " `!").queue();
                            } else {
                                event.reply("Something went wrong while saving the new count").setEphemeral(true).queue();
                            }
                        } else {
                            event.reply("Something went wrong while getting data from the database").setEphemeral(true).queue();
                        }
                    } else {
                        event.reply("No new count specified").setEphemeral(true).queue();
                    }
                } else if (Objects.equals(event.getSubcommandName(), "reset")) {
                    CountingDatabase database = CountingDatabase.getInstance();
                    CountingInfo countingInfo = database.getCountingData(event.getChannel().getId());
                    if (countingInfo != null) {
                        if (database.setCount(new CountingInfo(true, countingInfo.userID(), 0, " ", 3), event.getChannel().getId())) {
                            event.reply("Counter has been reset.").queue();
                        } else {
                            event.reply("Something went wrong while resetting the count").setEphemeral(true).queue();
                        }
                    } else {
                        event.reply("Something went wrong while getting data from the database").setEphemeral(true).queue();
                    }
                }
            } else {
                event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("counting")) {
            if (event.getFocusedOption().getName().equals("toggle")) {
                event.replyChoiceStrings("on", "off").queue();
            }
        }
    }

    private boolean saveToDatabase(String argument, String channelID) {
        CountingDatabase database = CountingDatabase.getInstance();
        return database.setCountingStatus(argument.equals("on"), channelID);
    }
}
