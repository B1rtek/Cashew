package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OpenCollection extends BaseCommand {

    Permission[] openCollectionCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
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
                        List<Pair<String,String>> collectionContents = database.getCollectionItems(selectedCollectionID);
                        if(availableRarities==null) {
                            event.getMessage().reply("[9] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                        if(collectionContents!=null) {
                            Random random = new Random();
                            int skinFloat = random.nextInt(10000);
                            if(skinFloat<1471) {
                                condition = "fn";
                            } else if(skinFloat<3939) {
                                condition = "mw";
                            } else if(skinFloat<8257) {
                                condition = "ft";
                            } else if(skinFloat<9049) {
                                condition = "ww";
                            } else {
                                condition = "bs";
                            }
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
                            for(Pair<String,String> item:collectionContents) {
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
                        Pair<String, String> conditions = processCondition(condition, fn, mw, ft, ww, bs);
                        condition = conditions.getFirst();
                        String imageURL = conditions.getSecond();
                        EmbedBuilder drop = new EmbedBuilder();
                        drop.setAuthor(selectedCollectionName, selectedCollectionURL, selectedCollectionIconURL);
                        drop.addField(itemName, condition, false);
                        drop.setImage(imageURL);
                        drop.setColor(embedColor);
                        if(!flavorText.equals("emptyFlavorLOL")) {
                            drop.setFooter(flavorText);
                        }
                        event.getChannel().sendTyping().queue();
                        event.getMessage().replyEmbeds(drop.build()).mentionRepliedUser(false).queueAfter(250, TimeUnit.MILLISECONDS);
                    } else {
                        event.getMessage().reply("[8] An error occurred while executing this command.").mentionRepliedUser(false).queue();
                    }
                } else {
                    displayCollectionsCommandEmbed(event, "You haven't specified which case you want to open.");
                }
            }
        }
    }

    private Pair<String, String> processCondition(String cond, String fn, String mw, String ft, String ww, String bs) {
        String condition = cond;
        if(condition.equals("fn") && fn.equals("empty")) {
            condition = "mw";
            if(mw.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "ww";
                    if(ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if(condition.equals("mw") && mw.equals("empty")) {
            condition = "fn";
            if(fn.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "ww";
                    if(ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if(condition.equals("ft") && ft.equals("empty")) {
            condition = "ww";
            if(ww.equals("empty")) {
                condition = "mw";
                if(mw.equals("empty")) {
                    condition = "bs";
                    if(bs.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if(condition.equals("ww") && ww.equals("empty")) {
            condition = "bs";
            if(bs.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "mw";
                    if(mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if(condition.equals("bs") && bs.equals("empty")) {
            condition = "ww";
            if(ww.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "mw";
                    if(mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        }
        String imageURL = "";
        switch (condition) {
            case "fn":
                condition = "Factory New";
                imageURL = fn;
                break;
            case "mw":
                condition = "Minimal Wear";
                imageURL = mw;
                break;
            case "ft":
                condition = "Field-Tested";
                imageURL = ft;
                break;
            case "ww":
                condition = "Well-Worn";
                imageURL = ww;
                break;
            case "bs":
                condition = "Battle-Scarred";
                imageURL = bs;
                break;
        }
        return new Pair<>(condition, imageURL);
    }

    private void displayCollectionsCommandEmbed(GuildMessageReceivedEvent event, String titleMessage) {
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