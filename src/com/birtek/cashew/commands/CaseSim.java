package com.birtek.cashew.commands;

import com.birtek.cashew.database.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
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
        CasesimCasesDatabase casesDatabase = CasesimCasesDatabase.getInstance();
        CasesimCollectionsDatabase collectionsDatabase = CasesimCollectionsDatabase.getInstance();
        CasesimCapsulesDatabase database = CasesimCapsulesDatabase.getInstance();
        casesChoices = casesDatabase.getAllCasesNames();
        if (casesChoices == null) LOGGER.warn("Failed to cache case choices!");
        collectionsChoices = collectionsDatabase.getAllCollectionsNames();
        if (collectionsChoices == null) LOGGER.warn("Failed to cache collection choices!");
        capsulesChoices = database.getAllCapsulesNames();
        if (capsulesChoices.isEmpty()) LOGGER.warn("Failed to cache capsule choices!");
    }

    private ArrayList<SkinRarity> findPossibleRarities(ArrayList<SkinInfo> skins) {
        ArrayList<SkinRarity> rarities = new ArrayList<>();
        for (SkinInfo skin : skins) {
            if (!rarities.contains(skin.rarity())) {
                rarities.add(skin.rarity());
            }
        }
        return rarities;
    }

    private String getSelectedContainerName(ArrayList<String> choicesList, String typed) {
        ArrayList<String> matchingCases = autocompleteFromList(choicesList, typed);
        if (matchingCases.isEmpty()) {
            return null;
        }
        return matchingCases.get(0);
    }

    private ArrayList<Double> calculatePercentages(ArrayList<SkinRarity> rarities, boolean collectionOdds) {
        ArrayList<Double> percentages;
        int rarityIndex;
        if (collectionOdds) {
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
        while (!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(percentages.size() - 2, percentages.get(percentages.size() - 2) + percentages.get(percentages.size() - 1));
            percentages.add(0, 0.0);
            percentages.remove(percentages.size() - 1);
            rarityIndex++;
        }
        // sum all values that go outside existing rarities bounds
        rarityIndex = SkinRarity.values().length - 1;
        while (!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(rarityIndex - 1, percentages.get(rarityIndex) + percentages.get(rarityIndex - 1));
            percentages.set(rarityIndex, 0.0);
            rarityIndex--;
        }
        // calculate prefix sums
        for (rarityIndex = 1; rarityIndex < SkinRarity.values().length; rarityIndex++) {
            percentages.set(rarityIndex, percentages.get(rarityIndex - 1) + percentages.get(rarityIndex));
        }
        // mark the last rarity with 1.0 to prevent weird issues from happening later
        rarityIndex = percentages.size() - 1;
        while (!rarities.contains(SkinRarity.values()[rarityIndex])) {
            percentages.set(rarityIndex, 1.0);
            rarityIndex--;
        }
        percentages.set(rarityIndex, 1.0);
        return percentages;
    }

    private SkinRarity getRarityFromPercent(ArrayList<Double> percentages) {
        double rarityPercent = random.nextDouble();
        int rarityIndex = 0;
        while (rarityPercent > percentages.get(rarityIndex)) rarityIndex++;
        return SkinRarity.values()[rarityIndex];
    }

    private SkinInfo getSkinOfRarity(SkinRarity rarity, ArrayList<SkinInfo> skins) {
        ArrayList<SkinInfo> skinsOfRarity = new ArrayList<>();
        for (SkinInfo skin : skins) {
            if (skin.rarity() == rarity) skinsOfRarity.add(skin);
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
        while (qualityInt > preorderQualityPercentages.get(qualityIndex)) qualityIndex++;
        // roll base float
        float baseFloat = random.nextFloat(floatRanges.get(qualityIndex).getLeft(), floatRanges.get(qualityIndex).getRight());
        // scale it to skin's float cap
        return baseFloat * (skin.maxFloat() - skin.minFloat()) + skin.minFloat();
    }

    private String getConditionFromFloat(float floatValue) {
        if (floatValue <= 0.07) return "Factory New";
        else if (floatValue <= 0.15) return "Minimal Wear";
        else if (floatValue <= 0.38) return "Field-Tested";
        else if (floatValue <= 0.45) return "Well-Worn";
        else return "Battle-Scarred";
    }

    private String getImageUrlFromFloat(float floatValue, SkinInfo skin) {
        if (floatValue <= 0.15) return skin.wearImg1();
        else if (floatValue <= 0.45) return skin.wearImg2();
        else return skin.wearImg3();
    }

    private String getInspectUrlFromFloat(float floatValue, SkinInfo skin) {
        if (floatValue <= 0.07) return skin.inspectFN();
        else if (floatValue <= 0.15) return skin.inspectMW();
        else if (floatValue <= 0.38) return skin.inspectFT();
        else if (floatValue <= 0.45) return skin.inspectWW();
        else return skin.inspectBS();
    }

    private int getColorFromRarity(SkinRarity rarity) {
        return switch (rarity) {
            case CONSUMER_GRADE -> 0xAFAFAF;
            case INDUSTRIAL_GRADE -> 0x6496E1;
            case MIL_SPEC -> 0x4B69CD;
            case RESTRICTED -> 0x8847FF;
            case CLASSIFIED -> 0xD32CE6;
            case COVERT -> 0xEB4B4B;
            case EXTRAORDINARY -> 0xFFD700;
        };
    }

    private String createInspectButtonID(String userID, float floatValue, SkinInfo skin) {
        String buttonID = userID + ":casesim:inspect:";
        buttonID += getInspectUrlFromFloat(floatValue, skin).substring(67);
        return buttonID;
    }

    private String createSaveToInvButtonID(String userID, String itemOrigin, float floatValue, SkinInfo skin, boolean statTrak) {
        String buttonID = userID + ":casesim:savetoinv:" + itemOrigin + ":";
        buttonID += skin.id() + ":" + floatValue + ":" + (statTrak?"1":"0");
        return buttonID;
    }

    private void sendSkinOpeningEmbed(SlashCommandInteractionEvent event, CaseInfo caseInfo, float floatValue, SkinInfo skin, String itemOrigin, boolean statTrak) {
        EmbedBuilder containerUnboxEmbed = new EmbedBuilder();
        containerUnboxEmbed.setAuthor(caseInfo.caseName(), caseInfo.caseUrl(), caseInfo.imageUrl());
        containerUnboxEmbed.setTitle((statTrak?"StatTrak™ ":"")+skin.name());
        containerUnboxEmbed.setFooter(skin.description());
        containerUnboxEmbed.addField("Condition: " + getConditionFromFloat(floatValue), String.valueOf(floatValue), true);
        containerUnboxEmbed.addField("Finish style", skin.finishStyle(), true);
        containerUnboxEmbed.setImage(getImageUrlFromFloat(floatValue, skin));
        containerUnboxEmbed.setDescription(skin.flavorText());
        containerUnboxEmbed.setColor(getColorFromRarity(skin.rarity()));

        event.replyEmbeds(containerUnboxEmbed.build()).addActionRow(
                Button.link(skin.stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), floatValue, skin), "Inspect URL"),
                Button.success(createSaveToInvButtonID(event.getUser().getId(), itemOrigin, floatValue, skin, statTrak), "Save to inventory")
        ).queue();
    }

    private void sendItemOpeningEmbed(SlashCommandInteractionEvent event, CaseInfo capsuleInfo, SkinInfo item) {
        EmbedBuilder capsuleUnboxEmbed = new EmbedBuilder();
        capsuleUnboxEmbed.setAuthor(capsuleInfo.caseName(), capsuleInfo.caseUrl(), capsuleInfo.imageUrl());
        capsuleUnboxEmbed.setTitle(item.name());
        capsuleUnboxEmbed.setImage(getImageUrlFromFloat(0.0f, item));
        capsuleUnboxEmbed.setColor(getColorFromRarity(item.rarity()));


        event.replyEmbeds(capsuleUnboxEmbed.build()).addActionRow(
                Button.link(item.stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), 0.0f, item), "Inspect URL"),
                Button.success(createSaveToInvButtonID(event.getUser().getId(), "capsule", 0.0f, item, false), "Save to inventory")
        ).queue();
    }

    private void openCase(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("case", "", OptionMapping::getAsString);
        String selectedCase = getSelectedContainerName(casesChoices, typed);
        if (selectedCase == null) {
            event.reply("No matching cases found.").setEphemeral(true).queue();
            return;
        }

        // Get all skins from the case
        CasesimCasesDatabase database = CasesimCasesDatabase.getInstance();
        CaseInfo caseInfo = database.getCaseInfo(selectedCase);
        if (caseInfo == null) {
            LOGGER.error("Query for case info of " + selectedCase + " in casesimCases.Cases returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }
        ArrayList<SkinInfo> caseSkins = database.getCaseSkins(caseInfo);
        if (caseSkins == null) {
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
        if (rarity == SkinRarity.EXTRAORDINARY) {
            ArrayList<SkinInfo> caseKnives = database.getCaseKnives(caseInfo);
            if (caseKnives == null) {
                LOGGER.error("Query for knives from " + selectedCase + " in casesimCases.Knives returned null");
                event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
                return;
            }
            skin = getSkinOfRarity(SkinRarity.COVERT, caseKnives);
        } else {
            skin = getSkinOfRarity(rarity, caseSkins);
        }
        float floatValue = getSkinFloat(skin);
        boolean statTrak = random.nextInt(10) == 0;

        // at this point all values are known, send back the result
        sendSkinOpeningEmbed(event, caseInfo, floatValue, skin, "case", statTrak);
    }

    private void openCollection(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("collection", "", OptionMapping::getAsString);
        String selectedCollection = getSelectedContainerName(collectionsChoices, typed);
        if (selectedCollection == null) {
            event.reply("No matching collections found.").setEphemeral(true).queue();
            return;
        }

        // Get all skins from the collection
        CasesimCollectionsDatabase database = CasesimCollectionsDatabase.getInstance();
        CaseInfo collectionInfo = database.getCollectionInfo(selectedCollection);
        if (collectionInfo == null) {
            LOGGER.error("Query for collection info of " + selectedCollection + " in casesimCollections.Collections returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }
        ArrayList<SkinInfo> collectionSkins = database.getCollectionSkins(collectionInfo);
        if (collectionSkins == null) {
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
        sendSkinOpeningEmbed(event, collectionInfo, floatValue, skin, "collection", false);
    }

    private void openCapsule(SlashCommandInteractionEvent event) {
        // Find the selected case name
        String typed = event.getOption("capsule", "", OptionMapping::getAsString);
        String selectedCapsule = getSelectedContainerName(capsulesChoices, typed);
        if (selectedCapsule == null) {
            event.reply("No matching capsules found.").setEphemeral(true).queue();
            return;
        }

        // Get all items from the capsule
        CasesimCapsulesDatabase database = CasesimCapsulesDatabase.getInstance();
        CaseInfo capsuleInfo = database.getCapsuleInfo(selectedCapsule);
        if (capsuleInfo == null) {
            LOGGER.error("Query for capsule info of " + selectedCapsule + " in casesimCapsules.Capsules returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }
        ArrayList<SkinInfo> capsuleItems = database.getCapsuleItems(capsuleInfo);
        if (capsuleItems == null) {
            LOGGER.error("Query for items from " + selectedCapsule + " in casesimCapsules.Stickers returned null");
            event.reply("Something went wrong while executing the command").setEphemeral(true).queue();
            return;
        }

        // Find all possible rarities
        ArrayList<SkinRarity> rarities = findPossibleRarities(capsuleItems);
        ArrayList<Double> percentages = calculatePercentages(rarities, false);

        // roll all random values
        SkinRarity rarity = getRarityFromPercent(percentages);
        SkinInfo skin = getSkinOfRarity(rarity, capsuleItems);

        // at this point all values are known, send back the result
        sendItemOpeningEmbed(event, capsuleInfo, skin);
    }

    private void inventory(SlashCommandInteractionEvent event) {
        EmbedBuilder inventoryEmbed = new EmbedBuilder();
        inventoryEmbed.setTitle(event.getUser().getName() + "'s inventory");
        inventoryEmbed.setFooter("Page 1 out of 10");
        for(int i=0; i<10; i++) {
            inventoryEmbed.addField("Item name", "Condition (Float)", false);
        }
        SelectMenu.Builder menuBuilder = SelectMenu.create(event.getUser().getId() + ":casesim:inventory")
                .setPlaceholder("Choose the weapon to interact with") // shows the placeholder indicating what this menu is for
                .setRequiredRange(1, 1); // only one can be selected
        for(int i=1; i<=10; i++) {
            menuBuilder.addOption(String.valueOf(i), String.valueOf(i));
        }
        event.replyEmbeds(inventoryEmbed.build()).addActionRow(menuBuilder.build()).addActionRow(
                Button.success(event.getUser().getId() + ":casesim:inventory:show", "Show"),
                Button.danger(event.getUser().getId() + ":casesim:inventory:delete", "Delete")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("casesim")) {
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
        if (event.getName().equals("casesim")) {
            ArrayList<String> autocompletions = new ArrayList<>();
            String optionToAutocomplete = event.getFocusedOption().getName();
            String typed = event.getOption(optionToAutocomplete, "", OptionMapping::getAsString);
            switch (event.getFocusedOption().getName()) {
                case "case" -> autocompletions = autocompleteFromList(casesChoices, typed);
                case "collection" -> autocompletions = autocompleteFromList(collectionsChoices, typed);
                case "capsule" -> autocompletions = autocompleteFromList(capsulesChoices, typed);
            }
            if (!autocompletions.isEmpty()) {
                event.replyChoiceStrings(autocompletions).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //userID:casesim:inspect:inspectURL
        //userID:casesim:savetoinv:origin:skinID:float:stattrak(0/1)
        String[] buttonID = event.getComponentId().split(":");
        if (buttonID.length < 4) return;
        if (buttonID[1].equals("casesim")) {
            switch (buttonID[2]) {
                case "inspect" -> {
                    String inspectURL = event.getComponentId().substring(35);
                    inspectURL = "steam://rungame/730/76561202255233023/+csgo_econ_action_preview%20M" + inspectURL;
                    event.reply(inspectURL).setEphemeral(true).queue();
                }
                case "savetoinv" -> {
                    if (buttonID[0].equals(event.getUser().getId())) {
                        event.reply("Saving to inventory doesn't work yet, come back later!").setEphemeral(true).queue();
                    } else {
                        event.reply("This is not your item!").setEphemeral(true).queue();
                    }
                }
                case "inventory" -> {
                    switch(buttonID[3]) {
                        case "show" -> {
                            event.reply("throw new NotImplementedException();").setEphemeral(true).queue();
                        }
                        case "delete" -> {
                            event.reply("throw new NotImplementedException();").setEphemeral(true).queue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String[] menuID = event.getComponentId().split(":");
        if (menuID.length < 3) return;
        if (menuID[1].equals("casesim") && menuID[2].equals("inventory")) {
            if(!event.getUser().getId().equals(menuID[0])) {
                event.reply("It's not your inventory").setEphemeral(true).queue();
                return;
            }
            MessageEmbed inventoryEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder selectedInventoryEmbed = new EmbedBuilder();
            selectedInventoryEmbed.setTitle(inventoryEmbed.getTitle());
            selectedInventoryEmbed.setFooter(Objects.requireNonNull(inventoryEmbed.getFooter()).getText());
            int index = 1;
            for(MessageEmbed.Field field: inventoryEmbed.getFields()) {
                String fieldName = field.getName();
                assert fieldName != null;
                if(fieldName.startsWith("[SEL] ")) {
                    fieldName = fieldName.substring(6);
                }
                if(String.valueOf(index).equals(event.getSelectedOptions().get(0).getValue())) {
                    selectedInventoryEmbed.addField("[SEL] " + fieldName, Objects.requireNonNull(field.getValue()), field.isInline());
                } else {
                    selectedInventoryEmbed.addField(fieldName, Objects.requireNonNull(field.getValue()), field.isInline());
                }
                index++;
            }
            event.editMessageEmbeds(selectedInventoryEmbed.build()).queue();
        }
    }
}
