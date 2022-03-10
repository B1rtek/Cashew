package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import com.birtek.cashew.messagereactions.CountingInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Counting extends BaseCommand {

    Permission[] helpCommandPermissions = {
            Permission.MESSAGE_MANAGE
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "counting")) {
            if (checkPermissions(event, helpCommandPermissions)) {
                String argument = parseInput(args);
                if(argument.equalsIgnoreCase("reset")) {
                    Database database = Database.getInstance();
                    CountingInfo countingInfo = database.getCountingData(event.getChannel().getId());
                    database.setCount(new CountingInfo(true, countingInfo.getUserID(), 0, " "), event.getChannel().getId());
                    event.getMessage().reply("Counter has been reset.").mentionRepliedUser(false).queue();
                } else if(argument.equalsIgnoreCase("setcount")) {
                    int newCount = parseSecondArgument(args);
                    Database database = Database.getInstance();
                    database.setCount(new CountingInfo(true, " ", newCount, " "), event.getChannel().getId());
                    event.getMessage().reply("Counter has been set to ` " + newCount+ " `. The next number is ` "+(newCount+1)+" `!").mentionRepliedUser(false).queue();
                }
                else {
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

    private int parseSecondArgument(String[] args) {
        if(args.length >= 3) {
            return Integer.parseInt(args[2]);
        } else {
            return 0;
        }
    }

    private boolean saveToDatabase(String argument, String channelID) {
        Database database = Database.getInstance();
        return database.setCountingStatus(argument.equals("on"), channelID);
    }
}
