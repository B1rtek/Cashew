package com.birtek.cashew.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Trivia extends BaseCommand {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("trivia")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            event.reply("Not implemented yet").setEphemeral(true).queue();
        }
    }
}
