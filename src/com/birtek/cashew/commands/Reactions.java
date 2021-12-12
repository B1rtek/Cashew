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
            if(checkPermissions(event, reactionsCommandPermissions)) {
                if(event.isWebhookMessage()) return;
                if(args.length<2) {
                    event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                } else {
                    args[1] = args[1].toLowerCase(Locale.ROOT);
                    switch (args[1]) {
                        case "off": {
                            Database database = Database.getInstance();
                            try {
                                if(database.updateChannelActivity(event.getChannel().getId(), 0)) {
                                    event.getMessage().reply("Reactions in this channel have been turned off.").mentionRepliedUser(false).queue();
                                } else {
                                    event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                event.getMessage().reply("An error occurred while executing SQL query. Try again in a few minutes.").mentionRepliedUser(false).queue();
                            }
                            break;
                        }
                        case "on": {
                            Database database = Database.getInstance();
                            try {
                                if(database.updateChannelActivity(event.getChannel().getId(), 1)) {
                                    event.getMessage().reply("Reactions in this channel have been turned on.").mentionRepliedUser(false).queue();
                                } else {
                                    event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                event.getMessage().reply("An error occurred while executing SQL query. Try again in a few minutes.").mentionRepliedUser(false).queue();
                            }
                            break;
                        }
                        case "all": {
                            Database database = Database.getInstance();
                            try {
                                if(database.updateChannelActivity(event.getChannel().getId(), 2)) {
                                    event.getMessage().reply("Reactions in this channel (including the @ann0y1ng ones) have been turned on.").mentionRepliedUser(false).queue();
                                } else {
                                    event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                event.getMessage().reply("An error occurred while executing SQL query. Try again in a few minutes.").mentionRepliedUser(false).queue();
                            }
                            break;
                        }
                        default:
                            event.getMessage().reply("Incorrect syntax. Please specify the argument [off|on|all].").mentionRepliedUser(false).queue();
                            break;
                    }
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }
}