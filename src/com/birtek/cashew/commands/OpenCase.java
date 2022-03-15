package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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

public class OpenCase extends BaseOpeningCommand {

    Permission[] openCaseCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    ArrayList<String> availableCases = new ArrayList<>();

    public OpenCase() {
        Database database = Database.getInstance();
        ResultSet cases = database.getCases();
        if (cases != null) {
            try {
                while (cases.next()) {
                    availableCases.add(cases.getString("caseName"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    MessageEmbed openCase(String caseChoice) {
        if (caseChoice.equals("cases") || caseChoice.equals("list")) {
            return generateCasesCommandEmbed("Here's the list of available cases:");
        }
        Database database = Database.getInstance();
        ResultSet cases = database.getCases();
        int selectedCaseID = 0, selectedCaseKnifeGroup = 0;
        String selectedCaseName = "", selectedCaseURL = "", selectedCaseIconURL = "";
        if (cases != null) {
            try {
                while (cases.next()) {
                    String caseName = cases.getString("caseName").toLowerCase(Locale.ROOT);
                    String caseID = String.valueOf(cases.getInt("caseID"));
                    if (isNumeric(caseChoice)) {
                        if (caseChoice.equalsIgnoreCase(caseID)) {
                            selectedCaseID = Integer.parseInt(caseID);
                            selectedCaseName = cases.getString("caseName");
                            selectedCaseURL = cases.getString("caseURL");
                            selectedCaseIconURL = cases.getString("caseIconURL");
                            selectedCaseKnifeGroup = cases.getInt("knifeGroup");
                            break;
                        }
                    } else if (caseName.contains(caseChoice.toLowerCase(Locale.ROOT))) {
                        selectedCaseID = Integer.parseInt(caseID);
                        selectedCaseName = cases.getString("caseName");
                        selectedCaseURL = cases.getString("caseURL");
                        selectedCaseIconURL = cases.getString("caseIconURL");
                        selectedCaseKnifeGroup = cases.getInt("knifeGroup");
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
        int embedColor = 0;
        if (selectedCaseID != 0) {
            ResultSet caseContents = database.getCaseItems(selectedCaseID);
            if (caseContents != null) {
                condition = getCaseItemCondition();
                Random random = new Random();
                int rarityRandom = random.nextInt(10000);
//                            rarityRandom = 9999;
                if (rarityRandom < 7992) {
                    rarity = "Mil-Spec";
                    embedColor = 0x4b69ff;
                } else if (rarityRandom < 9590) {
                    rarity = "Restricted";
                    embedColor = 0x8847ff;
                } else if (rarityRandom < 9910) {
                    rarity = "Classified";
                    embedColor = 0xd32ce6;
                } else if (rarityRandom < 9974) {
                    rarity = "Covert";
                    embedColor = 0xeb4b4b;
                } else {
                    rarity = "Rare Special Item";
                }
                if (rarity.equals("Rare Special Item")) {
                    return handleKnives(selectedCaseKnifeGroup, condition, selectedCaseName, selectedCaseURL, selectedCaseIconURL);
                }
                List<String> correspondingSkins = new ArrayList<>();
                try {
                    while (caseContents.next()) {
                        if (caseContents.getString("rarity").equals(rarity)) {
                            correspondingSkins.add(caseContents.getString("itemName"));
                        }
                    }
                } catch (SQLException e) {
                    return null;
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
            return generateCasesCommandEmbed("The case ID/name that you specified is invalid.");
        }
        ResultSet item = database.getCaseItem(itemName);
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
            Random random = new Random();
            if (random.nextInt(10) == 0) {
                itemName = "(StatTrak) " + itemName;
            }
            return generateDroppedItemEmbed(selectedCaseName, selectedCaseURL, selectedCaseIconURL, condition, itemName, embedColor, flavorText, imageURL);
        }
        return null;
    }

    private MessageEmbed handleKnives(int knifeGroup, String condition, String selectedCaseName, String selectedCaseURL, String selectedCaseIconURL) {
        if (knifeGroup == 0) {
            return null;
        }
        Database database = Database.getInstance();
        int knifeCount = database.getKnifeCount(knifeGroup);
        if (knifeCount == 0) {
            return null;
        }
        Random random = new Random();
        int knifeNumber = random.nextInt(knifeCount) + 1;
        ResultSet knives = database.getKnives(knifeGroup);
        if (knives != null) {
            String itemName = "", flavorText = "", fn = "", mw = "", ft = "", ww = "", bs = "";
            try {
                int index = 1;
                while (knives.next()) {
                    if (index == knifeNumber) {
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
                return null;
            }
            TwoStringsPair conditions = processCondition(condition, fn, mw, ft, ww, bs);
            condition = conditions.getFirst();
            String imageURL = conditions.getSecond();
            if (random.nextInt(10) == 0) {
                itemName = "(StatTrak) " + itemName;
            }
            EmbedBuilder drop = new EmbedBuilder();
            drop.setAuthor(selectedCaseName, selectedCaseURL, selectedCaseIconURL);
            drop.addField(itemName, condition, false);
            drop.setImage(imageURL);
            drop.setColor(0xe4ae33);
            if (!flavorText.equals("emptyFlavorLOL")) {
                drop.setFooter(flavorText);
            }
            return drop.build();
        } else {
            return null;
        }
    }

    private MessageEmbed generateCasesCommandEmbed(String titleMessage) {
        StringBuilder availableCasesString = new StringBuilder();
        for (int i = 0; i < availableCases.size(); i++) {
            availableCasesString.append(i + 1).append(" - ").append(availableCases.get(i)).append('\n');
        }
        EmbedBuilder caseOpeningInfo = new EmbedBuilder();
        caseOpeningInfo.setTitle(titleMessage);
        caseOpeningInfo.addField("Available cases:", availableCasesString.toString(), false);
        return caseOpeningInfo.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "opencase")) {
            if (checkPermissions(event, openCaseCommandPermissions)) {
                if (args.length >= 2) {
                    StringBuilder caseNameBuilder = new StringBuilder(args[1]);
                    for (int i = 2; i < args.length; i++) {
                        caseNameBuilder.append(" ").append(args[i]);
                    }
                    String caseChoice = caseNameBuilder.toString().toLowerCase(Locale.ROOT);
                    event.getChannel().sendTyping().queue();
                    MessageEmbed caseOpeningEmbed = openCase(caseChoice);
                    if (caseOpeningEmbed == null) {
                        event.getMessage().reply("Something went wrong while executing this command").mentionRepliedUser(false).queue();
                    } else {
                        event.getMessage().replyEmbeds(caseOpeningEmbed).mentionRepliedUser(false).queueAfter(250, TimeUnit.MILLISECONDS);
                    }
                } else {
                    event.getMessage().replyEmbeds(generateCasesCommandEmbed("Here's the list of available cases:")).mentionRepliedUser(false).queue();
                }
            } else {
                event.getMessage().reply("For some reason, you can't open cases :(").mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("opencase")) {
            if (checkSlashCommandPermissions(event, openCaseCommandPermissions)) {
                String caseChoice = event.getOption("case", "", OptionMapping::getAsString);
                int caseID = event.getOption("id", -1, OptionMapping::getAsInt);
                if (caseID == -1 && caseChoice.isEmpty()) {
                    event.replyEmbeds(Objects.requireNonNull(generateCasesCommandEmbed("Here's the list of available cases:"))).setEphemeral(true).queue();
                    return;
                }
                if (caseID != -1) {
                    caseChoice = String.valueOf(caseID);
                }
                MessageEmbed caseOpeningEmbed = openCase(caseChoice);
                if (caseOpeningEmbed == null) {
                    event.reply("Something went wrong while executing this command").mentionRepliedUser(false).queue();
                } else {
                    event.replyEmbeds(caseOpeningEmbed).queueAfter(250, TimeUnit.MILLISECONDS);
                }
            } else {
                event.reply("For some reason, you can't open cases :(").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("opencase")) {
            if (event.getFocusedOption().getName().equals("case")) {
                String typed = event.getOption("case", "", OptionMapping::getAsString);
                ArrayList<String> matching = new ArrayList<>();
                for (String caseName : availableCases) {
                    if(caseName.toLowerCase().contains(typed.toLowerCase(Locale.ROOT))) {
                        matching.add(caseName);
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