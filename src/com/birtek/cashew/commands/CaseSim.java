package com.birtek.cashew.commands;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
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

    private ArrayList<Integer> findPosibleRarities(ArrayList<SkinInfo> skins) {
        ArrayList<Integer> rarities = new ArrayList<>();
        for(SkinInfo skin : skins) {
            if(!rarities.contains(skin.rarity())) {
                rarities.add(skin.rarity());
            }
        }
        return rarities;
    }

    private void openCase(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("case", "", OptionMapping::getAsString);
        ArrayList<String> matchingCases = autocompleteFromList(casesChoices, typed);
        if(matchingCases.isEmpty()) {
            event.reply("No case matching the entered name found.").setEphemeral(true).queue();
            return;
        }
        String selectedName = autocompleteFromList(casesChoices, typed).get(0);

        // Get all skins from the case
        Database database = Database.getInstance();
        ArrayList<SkinInfo> caseSkins = database.getCaseSkins(selectedName);
        if(caseSkins == null) {
            LOGGER.error("Query for " + selectedName + " in casesimCases.Skins returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }

        // Find all possible rarities


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
                event.replyChoiceStrings(autocompletions).queue();
            }
        }
    }
}
