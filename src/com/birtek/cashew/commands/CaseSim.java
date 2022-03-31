package com.birtek.cashew.commands;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class CaseSim extends BaseCommand {

    private ArrayList<String> casesChoices = new ArrayList<>(), collectionsChoices = new ArrayList<>(), capsulesChoices = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseSim.class);

    public CaseSim() {
        cacheContainers();
    }

    private void cacheContainers() {
        Database database = Database.getInstance();
        casesChoices = database.getCasesimCases();
        if (casesChoices.isEmpty()) LOGGER.warn("Failed to cache case choices!");
        collectionsChoices = database.getCasesimCollections();
        if (collectionsChoices.isEmpty()) LOGGER.warn("Failed to cache collection choices!");
        capsulesChoices = database.getCasesimCapsules();
        if (capsulesChoices.isEmpty()) LOGGER.warn("Failed to cache capsule choices!");
    }

    private void openCase(SlashCommandInteractionEvent event) {
        event.reply("This doesn't work yet").setEphemeral(true).queue();
    }

    private void openCollection(SlashCommandInteractionEvent event) {
        event.reply("This doesn't work yet").setEphemeral(true).queue();
    }

    private void openCapsule(SlashCommandInteractionEvent event) {
        event.reply("This doesn't work yet").setEphemeral(true).queue();
    }

    private void inventory(SlashCommandInteractionEvent event) {
        event.reply("This doesn't work yet").setEphemeral(true).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("casesim")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "opencase" -> openCase(event);
                case "opencollection" -> openCollection(event);
                case "opencapsule" -> openCapsule(event);
                case "inventory" -> inventory(event);
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals("casesim")) {
            ArrayList<String> autocompletions = new ArrayList<>();
            String optionToAutocomplete = event.getFocusedOption().getName();
            String typed = event.getOption(optionToAutocomplete, "", OptionMapping::getAsString);
            switch (event.getFocusedOption().getName()) {
                case "case" -> autocompletions = autocompleteFromList(casesChoices, typed);
                case "collection" -> autocompletions = autocompleteFromList(collectionsChoices, typed);
                case "capsule" -> autocompletions = autocompleteFromList(capsulesChoices, typed);
            }
            if(!autocompletions.isEmpty()) {
                if(autocompletions.size() > 25) {
                    event.replyChoiceStrings("There's more than 25 matching options").queue();
                } else {
                    event.replyChoiceStrings(autocompletions).queue();
                }
            }
        }
    }
}
