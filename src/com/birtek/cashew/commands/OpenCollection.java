package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class OpenCollection extends BaseOpeningCommand {

    ArrayList<String> availableCollections = new ArrayList<>();

    public OpenCollection() {
        Database database = Database.getInstance();
        ResultSet collections = database.getCollections();
        if (collections != null) {
            try {
                while (collections.next()) {
                    availableCollections.add(collections.getString("collectionName"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    MessageEmbed openCollection(String collectionChoice) {
        if (collectionChoice.equals("collections") || collectionChoice.equals("list")) {
            return generateCollectionsCommandEmbed("Here's the list of available collections:");
        }
        Database database = Database.getInstance();
        ResultSet collections = database.getCollections();
        int selectedCollectionID = 0;
        String selectedCollectionName = "", selectedCollectionURL = "", selectedCollectionIconURL = "";
        if (collections != null) {
            try {
                while (collections.next()) {
                    String collectionName = collections.getString("collectionName").toLowerCase(Locale.ROOT);
                    String collectionID = String.valueOf(collections.getInt("collectionID"));
                    if (isNumeric(collectionChoice)) {
                        if (collectionChoice.equalsIgnoreCase(collectionID)) {
                            selectedCollectionID = Integer.parseInt(collectionID);
                            selectedCollectionName = collections.getString("collectionName");
                            selectedCollectionURL = collections.getString("collectionURL");
                            selectedCollectionIconURL = collections.getString("collectionIconURL");
                            break;
                        }
                    } else if (collectionName.contains(collectionChoice.toLowerCase(Locale.ROOT))) {
                        selectedCollectionID = Integer.parseInt(collectionID);
                        selectedCollectionName = collections.getString("collectionName");
                        selectedCollectionURL = collections.getString("collectionURL");
                        selectedCollectionIconURL = collections.getString("collectionIconURL");
                        break;
                    }
                }
            } catch (SQLException e) {
                return null;
            }
        } else {
            return null;
        }
        String condition, rarity, itemName;
        int embedColor;
        if (selectedCollectionID != 0) {
            List<String> availableRarities = database.getCollectionRarities(selectedCollectionID);
            List<TwoStringsPair> collectionContents = database.getCollectionItems(selectedCollectionID);
            if (availableRarities == null) {
                return null;
            }
            if (collectionContents != null) {
                condition = getCaseItemCondition();
                Random random = new Random();
                int rarityRandom = random.nextInt(998936);
//                            rarityRandom = 998935;
                if (rarityRandom < 799200) {
                    rarity = "Consumer";
                    embedColor = 0xafafaf;
                } else if (rarityRandom < 959000) {
                    rarity = "Industrial";
                    embedColor = 0x2196f3;
                } else if (rarityRandom < 991000) {
                    rarity = "Mil-Spec";
                    embedColor = 0x4b69ff;
                } else if (rarityRandom < 997400) {
                    rarity = "Restricted";
                    embedColor = 0x8847ff;
                } else if (rarityRandom < 998680) {
                    rarity = "Classified";
                    embedColor = 0xd32ce6;
                } else {
                    rarity = "Covert";
                    embedColor = 0xeb4b4b;
                }
                if (!availableRarities.contains("Covert") && rarity.equals("Covert")) {
                    rarity = "Classified";
                    embedColor = 0xd32ce6;
                }
                if (!availableRarities.contains("Classified") && rarity.equals("Classified")) {
                    rarity = "Restricted";
                    embedColor = 0x8847ff;
                }
                if (!availableRarities.contains("Restricted") && rarity.equals("Restricted")) {
                    rarity = "Mil-Spec";
                    embedColor = 0x4b69ff;
                }
                if (!availableRarities.contains("Mil-Spec") && rarity.equals("Mil-Spec")) {
                    rarity = "Industrial";
                    embedColor = 0x2196f3;
                }
                if (!availableRarities.contains("Industrial") && rarity.equals("Industrial")) {
                    rarity = "Consumer";
                    embedColor = 0xafafaf;
                }

                if (!availableRarities.contains("Consumer") && rarity.equals("Consumer")) {
                    rarity = "Industrial";
                    embedColor = 0x2196f3;
                }
                if (!availableRarities.contains("Industrial") && rarity.equals("Industrial")) {
                    rarity = "Mil-Spec";
                    embedColor = 0x4b69ff;
                }
                List<String> correspondingSkins = new ArrayList<>();
                for (TwoStringsPair item : collectionContents) {
                    if (item.getSecond().equals(rarity)) {
                        correspondingSkins.add(item.getFirst());
                    }
                }
                if (correspondingSkins.size() > 0) {
                    itemName = correspondingSkins.get(random.nextInt(correspondingSkins.size()));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return generateCollectionsCommandEmbed("The collection ID/name that you specified is invalid.");
        }
        ResultSet item = database.getCollectionItem(itemName);
        String flavorText = "(if you see this then something went wrong...)", fn = "", mw = "", ft = "", ww = "", bs = "";
        if (item != null) {
            try {
                while (item.next()) {
                    flavorText = item.getString("flavorText");
                    fn = item.getString("fn");
                    mw = item.getString("mw");
                    ft = item.getString("ft");
                    ww = item.getString("ww");
                    bs = item.getString("bs");
                }
            } catch (SQLException e) {
                return null;
            }
            TwoStringsPair conditions = processCondition(condition, fn, mw, ft, ww, bs);
            condition = conditions.getFirst();
            String imageURL = conditions.getSecond();
            return generateDroppedItemEmbed(selectedCollectionName, selectedCollectionURL, selectedCollectionIconURL, condition, itemName, embedColor, flavorText, imageURL);
        } else {
            return null;
        }
    }

    private MessageEmbed generateCollectionsCommandEmbed(String titleMessage) {
        StringBuilder availableCasesString = new StringBuilder();
        for (int i = 0; i < availableCollections.size(); i++) {
            availableCasesString.append(i + 1).append(" - ").append(availableCollections.get(i)).append('\n');
        }
        EmbedBuilder caseOpeningInfo = new EmbedBuilder();
        caseOpeningInfo.setTitle(titleMessage);
        caseOpeningInfo.addField("Available collections:", availableCasesString.toString(), false);
        return caseOpeningInfo.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencollection") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencol")) {
            if (args.length >= 2) {
                StringBuilder collectionNameBuilder = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    collectionNameBuilder.append(" ").append(args[i]);
                }
                String collectionChoice = collectionNameBuilder.toString().toLowerCase(Locale.ROOT);
                event.getChannel().sendTyping().queue();
                MessageEmbed collectionOpeningEmbed = openCollection(collectionChoice);
                if (collectionOpeningEmbed == null) {
                    event.getMessage().reply("Something went wrong while executing this command").mentionRepliedUser(false).queue();
                } else {
                    event.getMessage().replyEmbeds(collectionOpeningEmbed).mentionRepliedUser(false).queueAfter(250, TimeUnit.MILLISECONDS);
                }
            } else {
                event.getMessage().replyEmbeds(generateCollectionsCommandEmbed("Here's the list of available collections:")).mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("opencollection")) {
            String collectionChoice = event.getOption("collection", "", OptionMapping::getAsString);
            int collectionID = event.getOption("id", -1, OptionMapping::getAsInt);
            if (collectionID == -1 && collectionChoice.isEmpty()) {
                event.replyEmbeds(Objects.requireNonNull(generateCollectionsCommandEmbed("Here's the list of available collections:"))).setEphemeral(true).queue();
                return;
            }
            if (collectionID != -1) {
                collectionChoice = String.valueOf(collectionID);
            }
            MessageEmbed collectionOpeningEmbed = openCollection(collectionChoice);
            if (collectionOpeningEmbed == null) {
                event.reply("Something went wrong while executing this command").mentionRepliedUser(false).queue();
            } else {
                event.replyEmbeds(collectionOpeningEmbed).queueAfter(250, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("opencollection")) {
            if (event.getFocusedOption().getName().equals("collection")) {
                String typed = event.getOption("collection", "", OptionMapping::getAsString);
                ArrayList<String> matching = new ArrayList<>();
                for (String collectionName : availableCollections) {
                    if(collectionName.toLowerCase().contains(typed.toLowerCase(Locale.ROOT))) {
                        matching.add(collectionName);
                    }
                }
                if(matching.size() > 25) {
                    event.replyChoiceStrings("There's more than 25 matching choices").queue();
                } else {
                    event.replyChoiceStrings(matching).queue();
                }
            }
        }
    }
}