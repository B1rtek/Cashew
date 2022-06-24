package com.birtek.cashew.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Reminder extends BaseCommand {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("reminder")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "set" -> {

                }
                case "list" -> {

                }
                case "delete" -> {

                }
            }
        }
    }
}
