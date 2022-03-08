package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Locale;

public class Reactions extends BaseCommand {

    Permission[] reactionsCommandPermissions = {
            Permission.MANAGE_CHANNEL
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
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
                        case "off": {
                            newActivitySetting = 0;
                            break;
                        }
                        case "on": {
                            newActivitySetting = 1;
                            break;
                        }
                        case "all": {
                            newActivitySetting = 2;
                            break;
                        }
                        default: {
                            event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                            return;
                        }
                    }
                } else if (args.length == 3) {
                    try {
                        channelID = args[1].substring(2, args[1].length()-1);
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
                        case "off": {
                            newActivitySetting = 0;
                            break;
                        }
                        case "on": {
                            newActivitySetting = 1;
                            break;
                        }
                        case "all": {
                            newActivitySetting = 2;
                            break;
                        }
                        default: {
                            event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                            return;
                        }
                    }
                } else {
                    event.getMessage().reply("Incorrect syntax. Type `" + Cashew.COMMAND_PREFIX + "help` for help.").mentionRepliedUser(false).queue();
                    return;
                }
                Database database = Database.getInstance();
                try {
                    if(database.updateChannelActivity(channelID, newActivitySetting)) {
                        String target = "this";
                        String annoying = "";
                        String state = "off";
                        if (newActivitySetting > 0) {
                            state = "on";
                        }
                        if (newActivitySetting == 2) {
                            annoying = "(including the ann0y1ng ones) ";
                        }
                        if (args.length == 3) {
                            target = "the specified";
                        }
                        event.getMessage().reply("Reactions in "+target+" channel "+annoying+"have been turned "+state+".").mentionRepliedUser(false).queue();
                    } else {
                        event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    event.getMessage().reply("An error occurred while updating the setting. Try again in a few minutes.").mentionRepliedUser(false).queue();
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }
}