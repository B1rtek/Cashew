package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class OpenCollection extends BaseCommand {

    Permission[] openCollectionCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencollection") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencol")) {
            if(checkPermissions(event, openCollectionCommandPermissions)) {
                if(args.length>=2) {
                    StringBuilder collectionNameBuilder = new StringBuilder(args[1]);
                    for(int i=2; i<args.length; i++) {
                        collectionNameBuilder.append(" ").append(args[i]);
                    }
                    String collectionChoice = collectionNameBuilder.toString().toLowerCase(Locale.ROOT);
                    if(collectionChoice.equals("collections") || collectionChoice.equals("list")) {
                        displayCollectionsCommandEmbed(event, "Here's the list of available collections:");
                        return;
                    }
                    Database database = Database.getInstance();
                    ResultSet collections = database.getCollections();
                    int selectedCollectionID = 0;
                    String selectedCollectionName = "", selectedCollectionURL = "", selectedCollectionIconURL = "";
                    if(collections!=null) {
                        try {
                            while(collections.next()) {
                                String collectionName = collections.getString("collectionName").toLowerCase(Locale.ROOT);
                                String collectionID = String.valueOf(collections.getInt("collectionID"));
                                if(isNumeric(collectionChoice)) {
                                    if(collectionChoice.equalsIgnoreCase(collectionID)) {
                                        selectedCollectionID = Integer.parseInt(collectionID);
                                        selectedCollectionName = collections.getString("collectionName");
                                        selectedCollectionURL = collections.getString("collectionURL");
                                        selectedCollectionIconURL = collections.getString("collectionIconURL");
                                        break;
                                    }
                                } else if(collectionName.contains(collectionChoice)) {
                                    selectedCollectionID = Integer.parseInt(collectionID);
                                    selectedCollectionName = collections.getString("collectionName");
                                    selectedCollectionURL = collections.getString("collectionURL");
                                    selectedCollectionIconURL = collections.getString("collectionIconURL");
                                    break;
                                }
                            }
                        } catch (SQLException e) {
                            event.getMessage().reply("[1] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        event.getMessage().reply("[2] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                        return;
                    }
                    String condition, rarity, itemName;
                    int embedColor;
                    if(selectedCollectionID!=0) {
                        List<String> availableRarities = database.getCollectionRarities(selectedCollectionID);
                        List<TwoStringsPair> collectionContents = database.getCollectionItems(selectedCollectionID);
                        if(availableRarities==null) {
                            event.getMessage().reply("[9] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                        if(collectionContents!=null) {
                            condition = getCaseItemCondition();
                            Random random = new Random();
                            int rarityRandom = random.nextInt(998936);
//                            rarityRandom = 998935;
                            if(rarityRandom<799200) {
                                rarity = "Consumer";
                                embedColor = 0xafafaf;
                            } else if(rarityRandom<959000) {
                                rarity = "Industrial";
                                embedColor = 0x2196f3;
                            } else if(rarityRandom<991000) {
                                rarity = "Mil-Spec";
                                embedColor = 0x4b69ff;
                            } else if(rarityRandom<997400) {
                                rarity = "Restricted";
                                embedColor = 0x8847ff;
                            } else if(rarityRandom<998680) {
                                rarity = "Classified";
                                embedColor = 0xd32ce6;
                            } else {
                                rarity = "Covert";
                                embedColor = 0xeb4b4b;
                            }
                            if(!availableRarities.contains("Covert") && rarity.equals("Covert")) {
                                rarity = "Classified";
                                embedColor = 0xd32ce6;
                            }
                            if(!availableRarities.contains("Classified") && rarity.equals("Classified")) {
                                rarity = "Restricted";
                                embedColor = 0x8847ff;
                            }
                            if(!availableRarities.contains("Restricted") && rarity.equals("Restricted")) {
                                rarity = "Mil-Spec";
                                embedColor = 0x4b69ff;
                            }
                            if(!availableRarities.contains("Mil-Spec") && rarity.equals("Mil-Spec")) {
                                rarity = "Industrial";
                                embedColor = 0x2196f3;
                            }
                            if(!availableRarities.contains("Industrial") && rarity.equals("Industrial")) {
                                rarity = "Consumer";
                                embedColor = 0xafafaf;
                            }

                            if(!availableRarities.contains("Consumer") && rarity.equals("Consumer")) {
                                rarity = "Industrial";
                                embedColor = 0x2196f3;
                            }
                            if(!availableRarities.contains("Industrial") && rarity.equals("Industrial")) {
                                rarity = "Mil-Spec";
                                embedColor = 0x4b69ff;
                            }
                            List<String> correspondingSkins = new ArrayList<>();
                            for(TwoStringsPair item:collectionContents) {
                                if(item.getSecond().equals(rarity)) {
                                    correspondingSkins.add(item.getFirst());
                                }
                            }
                            if(correspondingSkins.size()>0) {
                                itemName = correspondingSkins.get(random.nextInt(correspondingSkins.size()));
                            } else {
                                event.getMessage().reply("[5] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                return;
                            }
                        } else {
                            event.getMessage().reply("[6] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                    } else {
                        displayCollectionsCommandEmbed(event, "The collection ID/name that you specified is invalid.");
                        return;
                    }
                    ResultSet item = database.getCollectionItem(itemName);
                    String flavorText = "(if you see this then something went wrong...)", fn = "", mw = "", ft = "", ww = "", bs = "";
                    if(item!=null) {
                        try {
                            while(item.next()) {
                                flavorText = item.getString("flavorText");
                                fn = item.getString("fn");
                                mw = item.getString("mw");
                                ft = item.getString("ft");
                                ww = item.getString("ww");
                                bs = item.getString("bs");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            event.getMessage().reply("[7] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                        TwoStringsPair conditions = processCondition(condition, fn, mw, ft, ww, bs);
                        condition = conditions.getFirst();
                        String imageURL = conditions.getSecond();
                        sendDroppedItemEmbed(event, selectedCollectionName, selectedCollectionURL, selectedCollectionIconURL, condition, itemName, embedColor, flavorText, imageURL);
                    } else {
                        event.getMessage().reply("[8] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                    }
                } else {
                    displayCollectionsCommandEmbed(event, "You haven't specified which case you want to open.");
                }
            }
        }
    }

    private void displayCollectionsCommandEmbed(MessageReceivedEvent event, String titleMessage) {
        Database database = Database.getInstance();
        ResultSet collections = database.getCollections();
        if(collections!=null) {
            try {
                StringBuilder availableCollections = new StringBuilder();
                while(collections.next()) {
                    String name = collections.getString("collectionName");
                    int number = collections.getInt("collectionID");
                    availableCollections.append(number).append(" - ").append(name).append('\n');
                }
                EmbedBuilder collectionOpeningInfo = new EmbedBuilder();
                collectionOpeningInfo.setTitle(titleMessage);
                collectionOpeningInfo.addField("Available collections:", availableCollections.toString(), false);
                event.getMessage().replyEmbeds(collectionOpeningInfo.build()).mentionRepliedUser(false).queue();
            } catch (SQLException e) {
                event.getMessage().reply(titleMessage + " (and an error occurred while executing this command)").mentionRepliedUser(false).queue();
            }
        } else {
            event.getMessage().reply(titleMessage + " (and an error occurred while executing this command)").mentionRepliedUser(false).queue();
        }
    }
}