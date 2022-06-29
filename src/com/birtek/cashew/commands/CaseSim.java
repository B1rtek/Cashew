package com.birtek.cashew.commands;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class CaseSim extends BaseCommand {

    private ArrayList<String> casesChoices = new ArrayList<>(), collectionsChoices = new ArrayList<>(), capsulesChoices = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseSim.class);
    private static final Random random = new Random();
    public enum SkinRarity {
        CONSUMER_GRADE,
        INDUSTRIAL_GRADE,
        MIL_SPEC,
        RESTRICTED,
        CLASSIFIED,
        COVERT,
        EXTRAORDINARY
    }

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

    private ArrayList<SkinRarity> findPossibleRarities(ArrayList<SkinInfo> skins) {
        ArrayList<SkinRarity> rarities = new ArrayList<>();
        for(SkinInfo skin : skins) {
            if(!rarities.contains(skin.rarity())) {
                rarities.add(skin.rarity());
            }
        }
        return rarities;
    }

    private String getSelectedContainerName(ArrayList<String> choicesList, String typed) {
        ArrayList<String> matchingCases = autocompleteFromList(choicesList, typed);
        if(matchingCases.isEmpty()) {
            return null;
        }
        return matchingCases.get(0);
    }

    private ArrayList<Double> calculatePercentages(ArrayList<SkinRarity> rarities, boolean collectionOdds) {
        ArrayList<Double> percentages;
        int rarityIndex;
        if(collectionOdds) {
            percentages = new ArrayList<>() {
                {
                    add(0.800064);
                    add(0.16);
                    add(0.032);
                    add(0.0064);
                    add(0.00128);
                    add(0.000256);
                    add(0.0); // extraordinary doesn't exist in collections
                }
            };
            rarityIndex = 0;

        } else {
            percentages = new ArrayList<>() {
                {
                    add(0.0);
                    add(0.0);
                    add(0.7992);
                    add(0.1598);
                    add(0.032);
                    add(0.0064);
                    add(0.0026);
                }
            };
            rarityIndex = 2;
        }
        // "shift right" values to match the existing rarities
        while(!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(percentages.size()-2, percentages.get(percentages.size()-2)+percentages.get(percentages.size()-1));
            percentages.add(0, 0.0);
            percentages.remove(percentages.size()-1);
            rarityIndex++;
        }
        // sum all values that go outside existing rarities bounds
        rarityIndex = SkinRarity.values().length-1;
        while(!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(rarityIndex-1, percentages.get(rarityIndex)+percentages.get(rarityIndex-1));
            percentages.set(rarityIndex, 0.0);
            rarityIndex--;
        }
        // calculate prefix sums
        for(rarityIndex = 1; rarityIndex < SkinRarity.values().length; rarityIndex++) {
            percentages.set(rarityIndex, percentages.get(rarityIndex-1)+percentages.get(rarityIndex));
        }
        // mark the last rarity with 1.0 to prevent weird issues from happening later
        rarityIndex = percentages.size()-1;
        while(!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(rarityIndex, 1.0);
            rarityIndex--;
        }
        percentages.set(rarityIndex, 1.0);
        return percentages;
    }

    private SkinRarity getRarityFromPercent(ArrayList<Double> percentages) {
        double rarityPercent = random.nextDouble();
        int rarityIndex = 0;
        while(rarityPercent > percentages.get(rarityIndex)) rarityIndex++;
        return SkinRarity.values()[rarityIndex];
    }

    private SkinInfo getSkinOfRarity(SkinRarity rarity, ArrayList<SkinInfo> skins) {
        ArrayList<SkinInfo> skinsOfRarity = new ArrayList<>();
        for(SkinInfo skin: skins) {
            if(skin.rarity() == rarity) skinsOfRarity.add(skin);
        }
        return skinsOfRarity.get(random.nextInt(skinsOfRarity.size()));
    }

    private static final ArrayList<Integer> preorderQualityPercentages = new ArrayList<>() {
        {
            add(2);
            add(26);
            add(59);
            add(83);
            add(99);
        }
    };

    private static final ArrayList<Pair<Float, Float>> floatRanges = new ArrayList<>() {
        {
            add(Pair.of(0.0f, 0.07f));
            add(Pair.of(0.08f, 0.15f));
            add(Pair.of(0.16f, 0.38f));
            add(Pair.of(0.39f, 0.45f));
            add(Pair.of(0.46f, 1.0f));
        }
    };

    private float getSkinFloat(SkinInfo skin) {
        // roll quality value
        int qualityInt = random.nextInt(100);
        int qualityIndex = 0;
        while(qualityInt > preorderQualityPercentages.get(qualityIndex)) qualityIndex++;
        // roll base float
        float baseFloat = random.nextFloat(floatRanges.get(qualityIndex).getLeft(), floatRanges.get(qualityIndex).getRight());
        // scale it to skin's float cap
        return baseFloat * (skin.maxFloat() - skin.minFloat()) + skin.minFloat();
    }

    private String getConditionFromFloat(float floatValue) {
        if(floatValue <= 0.07) return "Factory New";
        else if(floatValue <= 0.15) return "Minimal Wear";
        else if(floatValue <= 0.38) return "Field-Tested";
        else if(floatValue <= 0.45) return "Well-Worn";
        else return "Battle-Scarred";
    }

    private String getImageUrlFromFloat(float floatValue, SkinInfo skin) {
        if(floatValue <= 0.15) return skin.wearImg1();
        else if(floatValue <= 0.45) return skin.wearImg2();
        else return skin.wearImg3();
    }

    private String getInspectUrlFromFloat(float floatValue, SkinInfo skin) {
        if(floatValue <= 0.07) return skin.inspectFN();
        else if(floatValue <= 0.15) return skin.inspectMW();
        else if(floatValue <= 0.38) return skin.inspectFT();
        else if(floatValue <= 0.45) return skin.inspectWW();
        else return skin.inspectBS();
    }

    private String createInspectButtonID(String userID, float floatValue, SkinInfo skin) {
        String buttonID = userID + ":casesim:inspect:";
        buttonID += getInspectUrlFromFloat(floatValue, skin).substring(67);
        return buttonID;
    }

    private String createSaveToInvButtonID(String userID, String itemOrigin, float floatValue, SkinInfo skin) {
        String buttonID = userID + ":casesim:savetoinv:" + itemOrigin + ":";
        buttonID += skin.id() + ":" + floatValue;
        return buttonID;
    }

    private void sendSkinOpeningEmbed(SlashCommandInteractionEvent event, CaseInfo caseInfo, float floatValue, SkinInfo skin, String itemOrigin) {
        EmbedBuilder caseUnboxEmbed = new EmbedBuilder();
        caseUnboxEmbed.setAuthor(caseInfo.caseName(), caseInfo.caseUrl(), caseInfo.imageUrl());
        caseUnboxEmbed.setTitle(skin.name());
        caseUnboxEmbed.setFooter(skin.description());
        caseUnboxEmbed.addField("Condition: " + getConditionFromFloat(floatValue), String.valueOf(floatValue), true);
        caseUnboxEmbed.addField("Finish style", skin.finishStyle(), true);
        caseUnboxEmbed.setImage(getImageUrlFromFloat(floatValue, skin));
        caseUnboxEmbed.setDescription(skin.flavorText());

        event.replyEmbeds(caseUnboxEmbed.build()).addActionRow(
                Button.link(skin.stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), floatValue, skin), "Inspect URL"),
                Button.success(createSaveToInvButtonID(event.getUser().getId(), itemOrigin, floatValue, skin), "Save to inventory")
        ).queue();
    }

    private void openCase(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("case", "", OptionMapping::getAsString);
        String selectedCase = getSelectedContainerName(casesChoices, typed);
        if(selectedCase == null) {
            event.reply("No matching cases found.").setEphemeral(true).queue();
            return;
        }

        // Get all skins from the case
        Database database = Database.getInstance();
        CaseInfo caseInfo = database.getCaseInfo(selectedCase);
        if(caseInfo == null) {
            LOGGER.error("Query for case info of " + selectedCase + " in casesimCases.Cases returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }
        ArrayList<SkinInfo> caseSkins = database.getCaseSkins(caseInfo);
        if(caseSkins == null) {
            LOGGER.error("Query for skins from " + selectedCase + " in casesimCases.Skins returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }

        // Find all possible rarities
        ArrayList<SkinRarity> rarities = findPossibleRarities(caseSkins);
        rarities.add(SkinRarity.EXTRAORDINARY); // knives are possible to get in cases
        ArrayList<Double> percentages = calculatePercentages(rarities, false);

        // roll all random values
        SkinRarity rarity = getRarityFromPercent(percentages);
        SkinInfo skin;
        if(rarity == SkinRarity.EXTRAORDINARY) {
            ArrayList<SkinInfo> caseKnives = database.getCaseKnives(caseInfo);
            if(caseKnives == null) {
                LOGGER.error("Query for knives from " + selectedCase + " in casesimCases.Knives returned null");
                event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
                return;
            }
            skin = getSkinOfRarity(SkinRarity.COVERT, caseKnives);
        } else {
            skin = getSkinOfRarity(rarity, caseSkins);
        }
        float floatValue = getSkinFloat(skin);

        // at this point all values are known, send back the result
        sendSkinOpeningEmbed(event, caseInfo, floatValue, skin, "case");
    }

    private void openCollection(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("collection", "", OptionMapping::getAsString);
        String selectedCollection = getSelectedContainerName(collectionsChoices, typed);
        if(selectedCollection == null) {
            event.reply("No matching collections found.").setEphemeral(true).queue();
            return;
        }

        // Get all skins from the collection
        Database database = Database.getInstance();
        CaseInfo collectionInfo = database.getCollectionInfo(selectedCollection);
        if(collectionInfo == null) {
            LOGGER.error("Query for collection info of " + selectedCollection + " in casesimCollections.Collections returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }
        ArrayList<SkinInfo> collectionSkins = database.getCollectionSkins(collectionInfo);
        if(collectionSkins == null) {
            LOGGER.error("Query for skins from " + selectedCollection + " in casesimCollections.Skins returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }

        // Find all possible rarities
        ArrayList<SkinRarity> rarities = findPossibleRarities(collectionSkins);
        ArrayList<Double> percentages = calculatePercentages(rarities, true);

        // roll all random values
        SkinRarity rarity = getRarityFromPercent(percentages);
        SkinInfo skin = getSkinOfRarity(rarity, collectionSkins);
        float floatValue = getSkinFloat(skin);

        // at this point all values are known, send back the result
        sendSkinOpeningEmbed(event, collectionInfo, floatValue, skin, "collection");
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //userID:casesim:inspect:inspectURL
        //userID:casesim:savetoinv:skinID:float
        String[] buttonID = event.getComponentId().split(":");
        if(buttonID.length < 4) return;
        if(buttonID[1].equals("casesim")) {
            switch(buttonID[2]) {
                case "inspect" -> {
                    String inspectURL = event.getComponentId().substring(35);
                    inspectURL = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20M" + inspectURL;
                    event.reply(inspectURL).setEphemeral(true).queue();
                }
                case "savetoinv" -> {
                    if(buttonID[0].equals(event.getUser().getId())) {
                        event.reply("Saving to inventory doesn't work yet, come back later!").setEphemeral(true).queue();
                    } else {
                        event.reply("This is not your item!").setEphemeral(true).queue();
                    }
                }
            }
        }
    }
}
