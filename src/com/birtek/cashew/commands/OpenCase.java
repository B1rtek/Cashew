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

public class OpenCase extends BaseCommand {

    Permission[] openCaseCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencase")) {
            if(checkPermissions(event, openCaseCommandPermissions)) {
                if(args.length>=2) {
                    StringBuilder caseNameBuilder = new StringBuilder(args[1]);
                    for(int i=2; i<args.length; i++) {
                        caseNameBuilder.append(" ").append(args[i]);
                    }
                    String caseChoice = caseNameBuilder.toString().toLowerCase(Locale.ROOT);
                    if(caseChoice.equals("cases") || caseChoice.equals("list")) {
                        displayCasesCommandEmbed(event, "Here's the list of available cases:");
                        return;
                    }
                    Database database = Database.getInstance();
                    ResultSet cases = database.getCases();
                    int selectedCaseID = 0, selectedCaseKnifeGroup = 0;
                    String selectedCaseName = "", selectedCaseURL = "", selectedCaseIconURL = "";
                    if(cases!=null) {
                        try {
                            while(cases.next()) {
                                String caseName = cases.getString("caseName").toLowerCase(Locale.ROOT);
                                String caseID = String.valueOf(cases.getInt("caseID"));
                                if(isNumeric(caseChoice)) {
                                    if(caseChoice.equalsIgnoreCase(caseID)) {
                                        selectedCaseID = Integer.parseInt(caseID);
                                        selectedCaseName = cases.getString("caseName");
                                        selectedCaseURL = cases.getString("caseURL");
                                        selectedCaseIconURL = cases.getString("caseIconURL");
                                        selectedCaseKnifeGroup = cases.getInt("knifeGroup");
                                        break;
                                    }
                                } else if(caseName.contains(caseChoice)) {
                                    selectedCaseID = Integer.parseInt(caseID);
                                    selectedCaseName = cases.getString("caseName");
                                    selectedCaseURL = cases.getString("caseURL");
                                    selectedCaseIconURL = cases.getString("caseIconURL");
                                    selectedCaseKnifeGroup = cases.getInt("knifeGroup");
                                    break;
                                }
                            }
                        } catch (SQLException e) {
                            event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                        return;
                    }
                    String condition, rarity, itemName;
                    int embedColor = 0;
                    if(selectedCaseID!=0) {
                        ResultSet caseContents = database.getCaseItems(selectedCaseID);
                        if(caseContents!=null) {
                            condition = getCaseItemCondition();
                            Random random = new Random();
                            int rarityRandom = random.nextInt(10000);
//                            rarityRandom = 9999;
                            if(rarityRandom<7992) {
                                rarity = "Mil-Spec";
                                embedColor = 0x4b69ff;
                            } else if(rarityRandom<9590) {
                                rarity = "Restricted";
                                embedColor = 0x8847ff;
                            } else if(rarityRandom<9910) {
                                rarity = "Classified";
                                embedColor = 0xd32ce6;
                            } else if(rarityRandom<9974) {
                                rarity = "Covert";
                                embedColor = 0xeb4b4b;
                            } else {
                                rarity = "Rare Special Item";
                            }
                            if(rarity.equals("Rare Special Item")) {
                                handleKnives(event, selectedCaseKnifeGroup, condition, selectedCaseName, selectedCaseURL, selectedCaseIconURL);
                                return;
                            }
                            List<String> correspondingSkins = new ArrayList<>();
                            try {
                                while(caseContents.next()) {
                                    if(caseContents.getString("rarity").equals(rarity)) {
                                        correspondingSkins.add(caseContents.getString("itemName"));
                                    }
                                }
                            } catch (SQLException e) {
                                event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                e.printStackTrace();
                                return;
                            }
                            if(correspondingSkins.size()>0) {
                                itemName = correspondingSkins.get(random.nextInt(correspondingSkins.size()));
                            } else {
                                event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                                return;
                            }
                        } else {
                            event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                    } else {
                        displayCasesCommandEmbed(event, "The case ID/name that you specified is invalid.");
                        return;
                    }
                    ResultSet item = database.getCaseItem(itemName);
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
                            event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                            return;
                        }
                        Pair<String, String> conditions = processCondition(condition, fn, mw, ft, ww, bs);
                        condition = conditions.getFirst();
                        String imageURL = conditions.getSecond();
                        Random random = new Random();
                        if(random.nextInt(10)==0) {
                            itemName = "(StatTrak) "+itemName;
                        }
                        sendDroppedItemEmbed(event, selectedCaseName, selectedCaseURL, selectedCaseIconURL, condition, itemName, embedColor, flavorText, imageURL);
                    } else {
                        event.getMessage().reply("An error occurred while executing this command.").mentionRepliedUser(false).queue();
                    }
                } else {
                    displayCasesCommandEmbed(event, "You haven't specified which case you want to open.");
                }
            }
        }
    }

    private void handleKnives(GuildMessageReceivedEvent event, int knifeGroup, String condition, String selectedCaseName, String selectedCaseURL, String selectedCaseIconURL) {
        if(knifeGroup==0) {
            event.getMessage().reply("An error occurred while executing this command (you got a knife btw but there was an error idk) [1]").mentionRepliedUser(false).queue();
            return;
        }
        Database database = Database.getInstance();
        int knifeCount = database.getKnifeCount(knifeGroup);
        if(knifeCount==0) {
            event.getMessage().reply("An error occurred while executing this command (you got a knife btw but there was an error idk) [2]").mentionRepliedUser(false).queue();
            return;
        }
        Random random = new Random();
        int knifeNumber = random.nextInt(knifeCount)+1;
        ResultSet knives = database.getKnives(knifeGroup);
        if(knives!=null) {
            String itemName="", flavorText="", fn="", mw="", ft="", ww="", bs="";
            try {
                int index = 1;
                while(knives.next()) {
                    if(index==knifeNumber) {
                        itemName = knives.getString("itemName");
                        flavorText = knives.getString("flavorText");
                        fn = knives.getString("fn");
                        mw = knives.getString("mw");
                        ft = knives.getString("ft");
                        ww = knives.getString("ww");
                        bs = knives.getString("bs");
                        break;
                    }
                    index++;
                }
            } catch (SQLException e) {
                event.getMessage().reply("An error occurred while executing this command (you got a knife btw but there was an error idk) [3]").mentionRepliedUser(false).queue();
                e.printStackTrace();
                return;
            }
            Pair<String, String> conditions = processCondition(condition, fn, mw, ft, ww, bs);
            condition = conditions.getFirst();
            String imageURL = conditions.getSecond();
            if(random.nextInt(10)==0) {
                itemName = "(StatTrak) "+itemName;
            }
            EmbedBuilder drop = new EmbedBuilder();
            drop.setAuthor(selectedCaseName, selectedCaseURL, selectedCaseIconURL);
            drop.addField(itemName, condition, false);
            drop.setImage(imageURL);
            drop.setColor(0xe4ae33);
            if(!flavorText.equals("emptyFlavorLOL")) {
                drop.setFooter(flavorText);
            }
            event.getChannel().sendTyping().queue();
            event.getMessage().replyEmbeds(drop.build()).mentionRepliedUser(false).queueAfter(250, TimeUnit.MILLISECONDS);
        } else {
            event.getMessage().reply("An error occurred while executing this command (you got a knife btw but there was an error idk) [4]").mentionRepliedUser(false).queue();
        }
    }

    private void displayCasesCommandEmbed(GuildMessageReceivedEvent event, String titleMessage) {
        Database database = Database.getInstance();
        ResultSet cases = database.getCases();
        if(cases!=null) {
            try {
                StringBuilder availableCases = new StringBuilder();
                while(cases.next()) {
                    String name = cases.getString("caseName");
                    int number = cases.getInt("caseID");
                    availableCases.append(number).append(" - ").append(name).append('\n');
                }
                EmbedBuilder caseOpeningInfo = new EmbedBuilder();
                caseOpeningInfo.setTitle(titleMessage);
                caseOpeningInfo.addField("Available cases:", availableCases.toString(), false);
                event.getMessage().replyEmbeds(caseOpeningInfo.build()).mentionRepliedUser(false).queue();
            } catch (SQLException e) {
                event.getMessage().reply(titleMessage + " (and an error occurred while executing this command)").mentionRepliedUser(false).queue();
            }
        } else {
            event.getMessage().reply(titleMessage + " (and an error occurred while executing this command)").mentionRepliedUser(false).queue();
        }
    }
}