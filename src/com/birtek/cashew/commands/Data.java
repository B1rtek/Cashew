package com.birtek.cashew.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Data extends BaseCommand {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("data")) {
            if (!event.isFromGuild()) {
                event.reply("Data doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            event.reply("Not implemented yet").setEphemeral(true).queue();
        }
    }
}
