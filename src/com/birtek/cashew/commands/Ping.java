package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Ping extends BaseCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String pingMessage = "Pong!";
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "ping")) {
            long lastMeasured = System.nanoTime();
            event.getMessage().reply(pingMessage).mentionRepliedUser(false).queue(response ->
                    response.editMessage(pingMessage + " Time = " + Math.round((System.nanoTime() - lastMeasured) / 1000000.0) + " ms").queue());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String pingMessage = "Pong!";
        if (event.getName().equals("ping")) {
            long lastMeasured = System.nanoTime();
            event.reply(pingMessage).flatMap(v ->
                            event.getHook().editOriginal(pingMessage + " Time = " + Math.round((System.nanoTime() - lastMeasured) / 1000000.0) + " ms"))
                    .queue();
        }
    }
}
