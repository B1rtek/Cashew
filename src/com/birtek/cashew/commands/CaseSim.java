package com.birtek.cashew.commands;

import com.birtek.cashew.database.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
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
        buttonID += skin.id() + ":" + floatValue + ":" + (statTrak ? "1" : "0");
        return buttonID;
    }

    private void sendSkinOpeningEmbed(SlashCommandInteractionEvent event, CaseInfo caseInfo, SkinData skinData, SkinInfo skin) {
        MessageEmbed containerUnboxEmbed = generateItemEmbed(skinData, skin, caseInfo);
        event.replyEmbeds(containerUnboxEmbed).addActionRow(
                Button.link(skin.stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), skinData.floatValue(), skin), "Inspect URL"),
                Button.success(createSaveToInvButtonID(event.getUser().getId(), String.valueOf(skinData.containterType()), skinData.floatValue(), skin, skinData.statTrak()), "Save to inventory")
        ).queue();
    }

    private void sendItemOpeningEmbed(SlashCommandInteractionEvent event, CaseInfo capsuleInfo, SkinInfo item) {
        MessageEmbed capsuleUnboxEmbed = generateItemEmbed(new SkinData("", 4, 0, 0, false), item, capsuleInfo);

        event.replyEmbeds(capsuleUnboxEmbed).addActionRow(
                Button.link(item.stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), 0.0f, item), "Inspect URL"),
                Button.success(createSaveToInvButtonID(event.getUser().getId(), "4", 0.0f, item, false), "Save to inventory")
        ).queue();
    }

    private MessageEmbed generateItemEmbed(SkinData skinData, SkinInfo skinInfo, CaseInfo caseInfo) {
        EmbedBuilder containerUnboxEmbed = new EmbedBuilder();
        containerUnboxEmbed.setAuthor(caseInfo.caseName(), caseInfo.caseUrl(), caseInfo.imageUrl());
        containerUnboxEmbed.setTitle((skinData.statTrak() ? "StatTrak™ " : "") + skinInfo.name());
        containerUnboxEmbed.setImage(getImageUrlFromFloat(skinData.floatValue(), skinInfo));
        containerUnboxEmbed.setColor(getColorFromRarity(skinInfo.rarity()));
        if (skinData.containterType() < 4) {
            containerUnboxEmbed.setFooter(skinInfo.description());
            containerUnboxEmbed.addField("Condition: " + getConditionFromFloat(skinData.floatValue()), String.valueOf(skinData.floatValue()), true);
            containerUnboxEmbed.addField("Finish style", skinInfo.finishStyle(), true);
            containerUnboxEmbed.setDescription(skinInfo.flavorText());
        }
        return containerUnboxEmbed.build();
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
        SkinData skinData = new SkinData("", rarity == SkinRarity.EXTRAORDINARY ? 2 : 1, skin.id(), floatValue, statTrak);
        sendSkinOpeningEmbed(event, caseInfo, skinData, skin);
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
        sendSkinOpeningEmbed(event, collectionInfo, new SkinData("", 3, 0, floatValue, false), skin);
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

    private MessageEmbed getInventoryEmbed(User requestedUser, User requestingUser, int pageNumber, String userName, String requestedUserID, String requestedThumbnail) {
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        ArrayList<Pair<SkinData, SkinInfo>> inventory = new ArrayList<>();
        requestedUserID = requestedUserID != null ? requestedUserID : requestedUser.getId();
        while (inventory.isEmpty() && pageNumber > 0) {
            inventory = database.getInventoryPage(requestingUser.getId(), requestedUserID, pageNumber);
            if (inventory == null) return null;
            pageNumber--;
        }
        pageNumber++;
        String userString = userName == null ? requestedUser.getId().equals(requestingUser.getId()) ? "Your" : requestedUser.getName() + "'s" : userName;
        EmbedBuilder inventoryEmbed = new EmbedBuilder();
        if (inventory.isEmpty()) {
            inventoryEmbed.setTitle(userString + " inventory is empty!");
            return inventoryEmbed.build();
        } else if (inventory.size() == 1 && inventory.get(0).getLeft() == null) {
            inventoryEmbed.setTitle(userString + " inventory is private!");
            return inventoryEmbed.build();
        }
        CasesimInvStats inventoryStats = database.getInventoryStats(requestingUser.getId(), requestedUserID);
        int pageCount = inventoryStats.itemsInInventory() / 10 + (inventoryStats.itemsInInventory() % 10 == 0 ? 0 : 1);
        inventoryEmbed.setTitle(userString + " inventory");
        if (requestedUser != null) {
            inventoryEmbed.setThumbnail(requestedUser.getEffectiveAvatarUrl());
        } else if (requestedThumbnail != null){
            inventoryEmbed.setThumbnail(requestedThumbnail);
        }
        inventoryEmbed.setFooter("Page " + pageNumber + " out of " + pageCount);
        for (Pair<SkinData, SkinInfo> item : inventory) {
            inventoryEmbed.addField(item.getRight().name(), getConditionFromFloat(item.getLeft().floatValue()) + " (" + item.getLeft().floatValue() + ")", false);
        }
        return inventoryEmbed.build();
    }

    private Pair<ActionRow, ActionRow> getInventoryEmbedActionRows(User requestingUser, User requestedUser, MessageEmbed inventoryEmbed, String requestedUserName, String requestedUserID) {
        requestedUserID = requestedUser == null ? requestedUserID : requestedUser.getId();
        SelectMenu.Builder itemSelectMenu = SelectMenu.create(requestingUser.getId() + ":casesim:inventory")
                .setPlaceholder("Choose the weapon to interact with") // shows the placeholder indicating what this menu is for
                .setRequiredRange(1, 1); // only one can be selected
        int index = 0;
        for (MessageEmbed.Field item : inventoryEmbed.getFields()) {
            itemSelectMenu.addOption(Objects.requireNonNull(item.getName()), String.valueOf(index));
            index++;
        }
        ArrayList<Button> invControls = new ArrayList<>();
        invControls.add(Button.success(requestingUser.getId() + ":casesim:inventory:show:" + requestedUserID + ":" + requestedUserName, "Show"));
        if (requestedUserID.equals(requestingUser.getId())) {
            invControls.add(Button.danger(requestingUser.getId() + ":casesim:inventory:delete", "Delete"));
        }
        invControls.add(Button.primary(requestingUser.getId() + ":casesim:inventory:pageprev:" + requestedUserID + ":" + requestedUserName, Emoji.fromUnicode("◀️")));
        invControls.add(Button.primary(requestingUser.getId() + ":casesim:inventory:pagenext:" + requestedUserID + ":" + requestedUserName, Emoji.fromUnicode("▶️")));
        return Pair.of(ActionRow.of(itemSelectMenu.build()), ActionRow.of(invControls));
    }

    private void inventory(SlashCommandInteractionEvent event) {
        User requestedUser = event.getOption("user", event.getUser(), OptionMapping::getAsUser);
        MessageEmbed inventoryEmbed = getInventoryEmbed(requestedUser, event.getUser(), 1, null, null, null);
        if (inventoryEmbed == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        } else if (inventoryEmbed.getFields().isEmpty()) {
            event.reply(Objects.requireNonNull(inventoryEmbed.getTitle())).setEphemeral(true).queue();
            return;
        }
        String userName = requestedUser.getId().equals(event.getUser().getId()) ? "Your" : requestedUser.getName() + "'s";
        Pair<ActionRow, ActionRow> actionRows = getInventoryEmbedActionRows(event.getUser(), requestedUser, inventoryEmbed, userName, null);
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        CasesimInvStats inventoryStats = database.getInventoryStats(event.getUser().getId(), requestedUser.getId());
        event.replyEmbeds(inventoryEmbed).addActionRows(actionRows.getLeft(), actionRows.getRight()).setEphemeral(!inventoryStats.isPublic()).queue();
    }

    private void stats(SlashCommandInteractionEvent event) {
        User requestedUser = event.getOption("user", event.getUser(), OptionMapping::getAsUser);
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        CasesimInvStats stats = database.getInventoryStats(event.getUser().getId(), requestedUser.getId());
        if (stats == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        }
        String userString = requestedUser.getId().equals(event.getUser().getId()) ? "Your" : requestedUser.getName() + "'s";
        if (stats.itemsInInventory() == -1) {
            event.reply(userString + " inventory is private!").setEphemeral(true).queue();
            return;
        }
        MessageEmbed statsEmbed = generateInventoryStatsEmbed(event.getUser(), stats, userString, requestedUser.getId().equals(event.getUser().getId()));
        if (requestedUser.getId().equals(event.getUser().getId())) {
            event.replyEmbeds(statsEmbed).addActionRow(Button.primary(event.getUser().getId() + ":casesim:inventory:makepublic", "Make " + (!stats.isPublic() ? "public" : "private"))).setEphemeral(!stats.isPublic()).queue();
        } else {
            event.replyEmbeds(statsEmbed).queue();
        }
    }

    private MessageEmbed generateInventoryStatsEmbed(User user, CasesimInvStats stats, String userString, boolean asPrivate) {
        EmbedBuilder statsEmbed = new EmbedBuilder();
        statsEmbed.setTitle(userString + " Casesim stats");
        statsEmbed.setThumbnail(user.getEffectiveAvatarUrl());
        statsEmbed.addField("Cases opened", String.valueOf(stats.casesOpened()), false);
        statsEmbed.addField("Collections opened", String.valueOf(stats.collectionsOpened()), false);
        statsEmbed.addField("Capsules opened", String.valueOf(stats.capsulesOpened()), false);
        statsEmbed.addField("Items in the inventory", String.valueOf(stats.itemsInInventory()), false);
        if (asPrivate) {
            statsEmbed.setFooter("Inventory visibility: " + (stats.isPublic() ? "public" : "private"));
        }
        return statsEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("casesim")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "opencase" -> openCase(event);
                case "opencollection" -> openCollection(event);
                case "opencapsule" -> openCapsule(event);
                case "inventory" -> inventory(event);
                case "stats" -> stats(event);
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
        //userID:casesim:inventory:show:inventoryUserID:fullusername, might be more than one segment
        //userID:casesim:inventory:delete
        //userID:casesim:inventory:pagenext:inventoryUserID:fullusername, might be more than one segment
        //userID:casesim:inventory:pageprev:inventoryUserID:fullusername, might be more than one segment
        //userID:casesim:inventory:makepublic
        //userID:casesim:inventory:back:inventoryUserID:page:fullusername, might be more than one segment
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
                        saveToInventory(event, buttonID);
                    } else {
                        event.reply("This is not your item!").setEphemeral(true).queue();
                    }
                }
                case "inventory" -> {
                    if (!event.getUser().getId().equals(buttonID[0])) {
                        event.reply("It's not your inventory").setEphemeral(true).queue();
                        return;
                    }
                    switch (buttonID[3]) {
                        case "show" -> inventoryShow(event, buttonID);
                        case "delete" -> inventoryDelete(event, buttonID);
                        case "pagenext" -> inventorySwitchPage(event, buttonID, true);
                        case "pageprev" -> inventorySwitchPage(event, buttonID, false);
                        case "makepublic" -> inventoryMakePublic(event, buttonID);
                        case "back" -> inventoryBack(event, buttonID);
                    }
                }
            }
        }
    }

    private void saveToInventory(ButtonInteractionEvent event, String[] buttonID) {
        int itemOrigin;
        if (isNumeric(buttonID[3])) {
            itemOrigin = Integer.parseInt(buttonID[3]);
        } else {
            itemOrigin = switch (buttonID[3]) {
                case "case" -> 1;
                case "knife" -> 2;
                case "collection" -> 3;
                case "capsule" -> 4;
                default -> -1;
            };
        }
        if (itemOrigin == -1) {
            event.reply("An error occurred while determining the item type. If you see this, contact the bot creator through /feedback").setEphemeral(true).queue();
        }
        SkinData addedSkinData = new SkinData(event.getMessageId(), itemOrigin, Integer.parseInt(buttonID[4]), Float.parseFloat(buttonID[5]), buttonID[6].equals("1"));
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        int result = database.addToInventory(event.getUser().getId(), addedSkinData);
        String message = switch (result) {
            case 1 -> "Item successfully obtained!";
            case 0 -> "Your inventory is full!";
            case -2 -> "You have already obtained this item";
            default -> "Something went wrong, try again later";
        };
        event.reply(message).setEphemeral(true).queue();
    }

    private int getPageNumber(MessageEmbed inventoryEmbed) {
        return Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(inventoryEmbed.getFooter()).getText()).split("\\s+")[1]);
    }

    private int getSelectedItemIndex(MessageEmbed inventoryEmbed) {
        int selectedItemIndex = -1, index = 0;
        for (MessageEmbed.Field field : inventoryEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).startsWith("__")) {
                selectedItemIndex = index;
                break;
            }
            index++;
        }
        if (selectedItemIndex != -1) {
            int pageNumber = getPageNumber(inventoryEmbed);
            selectedItemIndex = (pageNumber - 1) * 10 + selectedItemIndex;
        }
        return selectedItemIndex;
    }

    private void inventoryShow(ButtonInteractionEvent event, String[] buttonID) {
        // get the selected item index
        MessageEmbed inventoryEmbed = event.getMessage().getEmbeds().get(0);
        int selectedItemIndex = getSelectedItemIndex(inventoryEmbed);
        if (selectedItemIndex == -1) {
            event.reply("Select an item first").setEphemeral(true).queue();
            return;
        }
        // get the item
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        Pair<SkinData, SkinInfo> item = database.getItemByIndex(buttonID[0], buttonID[4], selectedItemIndex);
        if (item == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        }
        if (item.getRight() == null) {
            if (item.getLeft() == null) {
                event.reply("This inventory is private").setEphemeral(true).queue();
            } else {
                event.reply("This item doesn't exist").setEphemeral(true).queue();
            }
            return;
        }
        // get the item CaseInfo
        CaseInfo caseInfo = null;
        switch (item.getLeft().containterType()) {
            case 1 -> {
                CasesimCasesDatabase casesDatabase = CasesimCasesDatabase.getInstance();
                caseInfo = casesDatabase.getCaseByID(item.getRight().caseId());
            }
            case 2 -> {
                CasesimCasesDatabase casesDatabase = CasesimCasesDatabase.getInstance();
                caseInfo = casesDatabase.getCaseByKnifeGroup(item.getRight().caseId());
            }
            case 3 -> {
                CasesimCollectionsDatabase collectionsDatabase = CasesimCollectionsDatabase.getInstance();
                caseInfo = collectionsDatabase.getCollectionByID(item.getRight().caseId());
            }
            case 4 -> {
                CasesimCapsulesDatabase capsulesDatabase = CasesimCapsulesDatabase.getInstance();
                caseInfo = capsulesDatabase.getCapsuleByID(item.getRight().caseId());
            }
        }
        if (caseInfo == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        }
        // display the item
        int pageNumber = getPageNumber(inventoryEmbed);
        MessageEmbed itemEmbed = generateItemEmbed(item.getLeft(), item.getRight(), caseInfo);
        StringBuilder requestedUserName = new StringBuilder(buttonID[5]);
        for (int i = 6; i < buttonID.length; i++) {
            requestedUserName.append(":").append(buttonID[i]);
        }
        event.editMessageEmbeds(itemEmbed).setActionRow(
                Button.link(item.getRight().stashUrl(), "CSGO Stash"),
                Button.primary(createInspectButtonID(event.getUser().getId(), item.getLeft().floatValue(), item.getRight()), "Inspect URL"),
                Button.secondary(event.getUser().getId() + ":casesim:inventory:back:" + buttonID[4] + ":" + pageNumber + ":" + requestedUserName, "Back")
        ).queue();
    }

    private void inventoryDelete(ButtonInteractionEvent event, String[] buttonID) {
        // get the selected item index
        MessageEmbed inventoryEmbed = event.getMessage().getEmbeds().get(0);
        int selectedItemIndex = getSelectedItemIndex(inventoryEmbed);
        if (selectedItemIndex == -1) {
            event.reply("Select an item first").setEphemeral(true).queue();
            return;
        }
        // remove it
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        if (!database.removeItemByIndex(event.getUser().getId(), selectedItemIndex)) {
            event.reply("Something went wrong").setEphemeral(true).queue();
            return;
        }
        // load the inventory again
        int pageNumber = getPageNumber(inventoryEmbed);
        inventoryEmbed = getInventoryEmbed(event.getUser(), event.getUser(), pageNumber, null, null, null);
        if (inventoryEmbed == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        } else if (inventoryEmbed.getFields().isEmpty()) {
            event.editMessage(Objects.requireNonNull(inventoryEmbed.getTitle())).setEmbeds().setActionRows().queue();
            return;
        }
        Pair<ActionRow, ActionRow> actionRows = getInventoryEmbedActionRows(event.getUser(), event.getUser(), inventoryEmbed, "Your", null);
        event.editMessageEmbeds(inventoryEmbed).setActionRows(actionRows.getLeft(), actionRows.getRight()).queue();
    }

    private void inventorySwitchPage(ButtonInteractionEvent event, String[] buttonID, boolean next) {
        String thumbnail = Objects.requireNonNull(event.getMessage().getEmbeds().get(0).getThumbnail()).getUrl();
        StringBuilder requestedUserName = new StringBuilder(buttonID[5]);
        for (int i = 6; i < buttonID.length; i++) {
            requestedUserName.append(":").append(buttonID[i]);
        }
        MessageEmbed inventoryEmbed = event.getMessage().getEmbeds().get(0);
        int pageNumber = getPageNumber(inventoryEmbed) + (next ? 1 : -1);
        if (pageNumber <= 0) {
            event.reply("You're already on the first page").setEphemeral(true).queue();
            return;
        }
        inventoryEmbed = getInventoryEmbed(null, event.getUser(), pageNumber, requestedUserName.toString(), buttonID[4], thumbnail);
        if (inventoryEmbed == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        } else if (inventoryEmbed.getFields().isEmpty()) {
            event.editMessage(Objects.requireNonNull(inventoryEmbed.getTitle())).setEmbeds().setActionRows().queue();
            return;
        }
        Pair<ActionRow, ActionRow> actionRows = getInventoryEmbedActionRows(event.getUser(), null, inventoryEmbed, requestedUserName.toString(), buttonID[4]);
        event.editMessageEmbeds(inventoryEmbed).setActionRows(actionRows.getLeft(), actionRows.getRight()).queue();
    }

    private void inventoryMakePublic(ButtonInteractionEvent event, String[] buttonID) {
        CasesimInventoryDatabase database = CasesimInventoryDatabase.getInstance();
        CasesimInvStats stats = database.getInventoryStats(event.getUser().getId(), event.getUser().getId());
        if(stats == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        }
        // change the setting
        if(!database.setInventoryVisibility(event.getUser().getId(), !stats.isPublic())) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        }
        stats.setPublic(!stats.isPublic());
        // edit the embed accordingly and send it back
        MessageEmbed statsEmbed = generateInventoryStatsEmbed(event.getUser(), stats, "Your", true);
        event.editMessageEmbeds(statsEmbed).setActionRow(Button.primary(event.getUser().getId() + ":casesim:inventory:makepublic", "Make " + (!stats.isPublic() ? "public" : "private"))).queue();
    }

    private void inventoryBack(ButtonInteractionEvent event, String[] buttonID) {
        StringBuilder requestedUserName = new StringBuilder(buttonID[6]);
        for (int i = 7; i < buttonID.length; i++) {
            requestedUserName.append(":").append(buttonID[i]);
        }
        MessageEmbed inventoryEmbed = getInventoryEmbed(null, event.getUser(), Integer.parseInt(buttonID[5]), requestedUserName.toString(), buttonID[4], event.getUser().getEffectiveAvatarUrl());
        if (inventoryEmbed == null) {
            event.reply("Something went wrong, try again later").setEphemeral(true).queue();
            return;
        } else if (inventoryEmbed.getFields().isEmpty()) {
            event.editMessage(Objects.requireNonNull(inventoryEmbed.getTitle())).setEmbeds().setActionRows().queue();
            return;
        }
        Pair<ActionRow, ActionRow> actionRows = getInventoryEmbedActionRows(event.getUser(), null, inventoryEmbed, requestedUserName.toString(), buttonID[4]);
        event.editMessageEmbeds(inventoryEmbed).setActionRows(actionRows.getLeft(), actionRows.getRight()).queue();
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String[] menuID = event.getComponentId().split(":");
        if (menuID.length < 3) return;
        if (menuID[1].equals("casesim") && menuID[2].equals("inventory")) {
            if (!event.getUser().getId().equals(menuID[0])) {
                event.reply("It's not your inventory").setEphemeral(true).queue();
                return;
            }
            MessageEmbed inventoryEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder selectedInventoryEmbed = new EmbedBuilder();
            selectedInventoryEmbed.setTitle(inventoryEmbed.getTitle());
            MessageEmbed.Thumbnail thumbnail = inventoryEmbed.getThumbnail();
            if(thumbnail != null) {
                selectedInventoryEmbed.setThumbnail(thumbnail.getUrl());
            } else {
                selectedInventoryEmbed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
            }
            selectedInventoryEmbed.setFooter(Objects.requireNonNull(inventoryEmbed.getFooter()).getText());
            int index = 0;
            for (MessageEmbed.Field field : inventoryEmbed.getFields()) {
                String fieldName = field.getName();
                assert fieldName != null;
                if (fieldName.startsWith("__")) {
                    fieldName = fieldName.substring(2, fieldName.length() - 2);
                }
                if (index == Integer.parseInt(event.getSelectedOptions().get(0).getValue())) {
                    selectedInventoryEmbed.addField("__" + fieldName + "__", Objects.requireNonNull(field.getValue()), field.isInline());
                } else {
                    selectedInventoryEmbed.addField(fieldName, Objects.requireNonNull(field.getValue()), field.isInline());
                }
                index++;
            }
            event.editMessageEmbeds(selectedInventoryEmbed.build()).queue();
        }
    }
}
