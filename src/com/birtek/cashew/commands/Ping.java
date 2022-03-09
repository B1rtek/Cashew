package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Ping extends BaseCommand {

    Permission[] pingCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    private long lastMeasured;

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String pingMessage = "Pong!";
        if(event.getMessage().getContentRaw().contentEquals(pingMessage) && event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            long time = Math.round((System.nanoTime() - lastMeasured)/1000000.0);
            event.getMessage().editMessage("Pong! Time = "+time+" ms").queue();
            return;
        }
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "ping")) {
            if(checkPermissions(event, pingCommandPermissions)) {
                event.getMessage().reply(pingMessage).mentionRepliedUser(false).queue();
                lastMeasured = System.nanoTime();
            }
        }
    }
}
