package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Ping extends BaseCommand {

    Permission[] pingCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String pingMessage = "Pong!";
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "ping")) {
            if (checkPermissions(event, pingCommandPermissions)) {
                long lastMeasured = System.nanoTime();
                event.getMessage().reply(pingMessage).mentionRepliedUser(false).queue(response ->
                        response.editMessage(pingMessage + " Time = " + Math.round((System.nanoTime() - lastMeasured) / 1000000.0) + " ms").queue());
            }
        }
    }
}
