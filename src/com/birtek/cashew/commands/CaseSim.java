package com.birtek.cashew.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CaseSim extends BaseCommand {

    private void openCase(SlashCommandInteractionEvent event) {

    }

    private void openCollection(SlashCommandInteractionEvent event) {

    }

    private void openCapsule(SlashCommandInteractionEvent event) {

    }

    private void inventory(SlashCommandInteractionEvent event) {

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("casesim")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "opencase" -> {
                    openCase(event);
                }
                case "opencollection" -> {
                    openCollection(event);
                }
                case "opencapsule" -> {
                    openCapsule(event);
                }
                case "inventory" -> {
                    inventory(event);
                }
            }
        }
    }
}
