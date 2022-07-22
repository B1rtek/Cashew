package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.ChannelActivityDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public class Reactions extends BaseCommand {

    Permission[] reactionsCommandPermissions = {
            Permission.MANAGE_CHANNEL
    };

    Map<String, Integer> mapToggleToActivity = Map.of(
            "off", 0,
            "on", 1,
            "all", 2
    );

    String setActivity(String channelID, int newActivitySetting, boolean differentChannelSpecified) {
        ChannelActivityDatabase database = ChannelActivityDatabase.getInstance();
        if (database.updateChannelActivity(channelID, newActivitySetting)) {
            String target = "this";
            String annoying = "";
            String state = "off";
            if (newActivitySetting > 0) {
                state = "on";
            }
            if (newActivitySetting == 2) {
                annoying = "(including the ann0y1ng ones) ";
            }
            if (differentChannelSpecified) {
                target = "the specified";
            }
            return "Reactions in " + target + " channel " + annoying + "have been turned " + state + ".";
        } else {
            return "An error occurred while executing this command.";
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "reactions")) {
            if (checkPermissions(event, reactionsCommandPermissions)) {
                int newActivitySetting;
                String channelID = event.getChannel().getId();
                if (event.isWebhookMessage()) return;
                if (args.length < 2) {
                    event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                    return;
                } else if (args.length == 2) {
                    args[1] = args[1].toLowerCase(Locale.ROOT);
                    switch (args[1]) {
                        case "off" -> newActivitySetting = 0;
                        case "on" -> newActivitySetting = 1;
                        case "all" -> newActivitySetting = 2;
                        default -> {
                            event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                            return;
                        }
                    }
                } else if (args.length == 3) {
                    try {
                        channelID = args[1].substring(2, args[1].length() - 1);
                    } catch (StringIndexOutOfBoundsException e) {
                        event.getMessage().reply("Incorrect syntax. Type `" + Cashew.COMMAND_PREFIX + "help` for help.").mentionRepliedUser(false).queue();
                        return;
                    }
                    try {
                        if (event.getGuild().getGuildChannelById(channelID) == null) {
                            event.getMessage().reply("Invalid channel specified").mentionRepliedUser(false).queue();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        event.getMessage().reply("Invalid channel specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    args[2] = args[2].toLowerCase(Locale.ROOT);
                    switch (args[2]) {
                        case "off" -> newActivitySetting = 0;
                        case "on" -> newActivitySetting = 1;
                        case "all" -> newActivitySetting = 2;
                        default -> {
                            event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                            return;
                        }
                    }
                } else {
                    event.getMessage().reply("Incorrect syntax. Type `" + Cashew.COMMAND_PREFIX + "help` for help.").mentionRepliedUser(false).queue();
                    return;
                }
                //tutaj rzeczy
                String creationMessage = setActivity(channelID, newActivitySetting, args.length >= 3);
                event.getMessage().reply(creationMessage).mentionRepliedUser(false).queue();
            } else {
                event.getMessage().delete().complete();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reactions")) {
            if (checkSlashCommandPermissions(event, reactionsCommandPermissions)) {
                String toggle = event.getOption("toggle", "", OptionMapping::getAsString);
                MessageChannel channel = (MessageChannel) event.getOption("channel", null, OptionMapping::getAsChannel);
                if (!toggle.isEmpty()) {
                    int newActivity = mapToggleToActivity.get(toggle);
                    String channelID = event.getChannel().getId();
                    if (channel != null) {
                        channelID = channel.getId();
                    }
                    String creationMessage = setActivity(channelID, newActivity, channel != null);
                    event.reply(creationMessage).queue();
                }
            } else {
                event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals("reactions")) {
            if(event.getFocusedOption().getName().equals("toggle")) {
                event.replyChoiceStrings("on", "off", "all").queue();
            }
        }
    }
}