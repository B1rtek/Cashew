package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import com.birtek.cashew.messagereactions.CountingInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableCellRenderer;
import java.util.Locale;

public class Counting extends BaseCommand {

    Permission[] helpCommandPermissions = {
            Permission.MESSAGE_MANAGE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "counting")) {
            if (checkPermissions(event, helpCommandPermissions)) {
                String argument = parseInput(args);
                if(argument.equalsIgnoreCase("reset")) {
                    Database database = Database.getInstance();
                    database.setCount(new CountingInfo(true, " ", 0), event.getChannel().getId());
                } else {
                    if (saveToDatabase(argument.toLowerCase(Locale.ROOT), event.getChannel().getId())) {
                        event.getMessage().reply("Counting has been turned " + args[1] + " in this channel.").mentionRepliedUser(false).queue();
                    }
                }
            }
        }
    }

    private String parseInput(String[] args) {
        if (args.length >= 2) {
            return args[1];
        } else {
            return "";
        }
    }

    private boolean saveToDatabase(String argument, String channelID) {
        Database database = Database.getInstance();
        return database.setCountingStatus(argument.equals("on"), channelID);
    }
}
