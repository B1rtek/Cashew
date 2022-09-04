package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.WhenRule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class When extends BaseCommand {

    private ArrayList<String> availableTriggers = new ArrayList<>() {
        {
            add("a user joins the server");
            add("a user leaves the server");
            add("a member reacts to the message <sourceMessageID> with <sourceReactionEmote>");
            add("a member removes reaction <sourceReactionEmote> from message <sourceMessageID>");
            add("a member edits a message [#sourceChannel, optional]");
            add("a member deletes a message [#sourceChannel, optional]");
        }
    };

    private ArrayList<String> availableActions = new ArrayList<>() {
        {
            add("send a message <messageContent> in <#targetChannel>");
            add("add <@targetRole> to the interacting user");
            add("remove <@targetRole> from the interacting user");
            add("pass the information about the event to your DM");
            add("pass the information to a channel <#targetChannel>");
        }
    };

    private Pair<String, String> generateRuleContents(WhenRule rule, Guild server) {
        String triggerDescription = "When ";
        switch (rule.getTriggerType()) {
            case 1, 2 -> triggerDescription += availableTriggers.get(rule.getTriggerType() - 1);
            case 3, 4 -> {
                String sourceMessageID = rule.getSourceMessageID();
                String sourceReactionEmote = rule.getSourceReaction();
                triggerDescription += availableTriggers.get(rule.getTriggerType() - 1).replace("<sourceReactionEmote>", sourceReactionEmote).replace("<sourceMessageID>", sourceMessageID);
            }
            case 5, 6 -> {
                String sourceChannelID = rule.getSourceChannelID();
                String sourceChannelName = "";
                if (sourceChannelID != null) {
                    TextChannel sourceChannel = server.getChannelById(TextChannel.class, sourceChannelID);
                    if (sourceChannel != null) sourceChannelName = "in #" + sourceChannel.getName();
                }
                triggerDescription += availableTriggers.get(rule.getTriggerType() - 1).replace("[#sourceChannel, optional]", "in " + sourceChannelName);
            }
        }
        String actionDescription = "";
        switch (rule.getActionType()) {
            case 1 -> {
                String targetMessageContent = "\"" + rule.getTargetMessageContent() + "\"";
                String targetChannelName = rule.getTargetChannelID();
                TextChannel targetChannel = server.getChannelById(TextChannel.class, targetChannelName);
                if (targetChannel != null) {
                    targetChannelName = "#" + targetChannel.getName();
                }
                actionDescription = availableActions.get(rule.getActionType() - 1).replace("<messageContent>", targetMessageContent).replace("<#targetChannel>", targetChannelName);
            }
            case 2, 3 -> {
                String targetRoleName = rule.getTargetRoleID();
                Role targetRole = server.getRoleById(targetRoleName);
                if (targetRole != null) {
                    targetRoleName = targetRole.getAsMention();
                }
                actionDescription = availableActions.get(rule.getActionType() - 1).replace("<@targetRole>", targetRoleName);
            }
            case 4 -> {
                String targetUserName = rule.getTargetUserID();
                Member targetMember = server.retrieveMemberById(targetUserName).complete();
                if (targetMember != null) {
                    targetUserName = targetMember.getAsMention();
                }
                actionDescription = "pass the information about the event to " + targetUserName + " 's DM";
            }
            case 5 -> {
                String targetChannelName = rule.getTargetChannelID();
                TextChannel targetChannel = server.getChannelById(TextChannel.class, targetChannelName);
                if (targetChannel != null) {
                    targetChannelName = "#" + targetChannel.getName();
                }
                actionDescription = availableActions.get(rule.getActionType() - 1).replace("<#targetChannel>", targetChannelName);
            }
        }
        return Pair.of(triggerDescription, actionDescription);
    }

    /**
     * Generates an {@link MessageEmbed embed} with the provided {@link WhenRule WhenRules} list, showing their contents
     *
     * @param rules  ArrayList of {@link WhenRule WhenRules} representing a page of the list, with no more than 10 rules
     * @param server {@link Guild server} from which the rules are listed
     * @param page   number of the page to display
     * @return a {@link MessageEmbed MessageEmbed} with the title "WhenRules on (server)", with a page of rules below,
     * with the trigger in the title and the action in the description, number of the current page in the footer as well
     * as the number of total pages, and the server icon in the thumbnail
     */
    private MessageEmbed generateRulesListEmbed(ArrayList<WhenRule> rules, Guild server, int page) {
        EmbedBuilder rulesListEmbed = new EmbedBuilder();
        rulesListEmbed.setTitle("WhenRules set on " + server.getName());
        rulesListEmbed.setThumbnail(server.getIconUrl());
        int index = 1;
        for (WhenRule rule : rules) {
            Pair<String, String> ruleContents = generateRuleContents(rule, server);
            rulesListEmbed.addField((index + (page - 1) * 10) + ". " + ruleContents.getLeft(), ruleContents.getRight(), false);
            index++;
        }
        int pageCount = Cashew.whenSettingsManager.getWhenRulesPageCount(server.getId());
        rulesListEmbed.setFooter("Page " + page + " out of " + pageCount);
        return rulesListEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("when")) {
            if (cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName() == null) {
                event.reply("No subcommand chosen (how????)").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName().equals("when")) {
                WhenRule newRule = new WhenRule(Objects.requireNonNull(event.getGuild()).getId());
                int triggerType = availableTriggers.indexOf(event.getOption("trigger", "", OptionMapping::getAsString)) + 1;
                switch (triggerType) {
                    case 1 -> newRule.memberJoinTrigger();
                    case 2 -> newRule.memberLeaveTrigger();
                    case 3, 4 -> {
                        String sourceMessageID = event.getOption("sourcemessageid", null, OptionMapping::getAsString);
                        String sourceReaction = event.getOption("sourcereactionemote", null, OptionMapping::getAsString);
                        if (sourceMessageID == null || sourceReaction == null) {
                            event.reply("Both `sourcemessageid` and `sourcereactionemote` are required").setEphemeral(true).queue();
                            return;
                        }
                        if (triggerType == 3) {
                            newRule.memberReactsTrigger(sourceMessageID, sourceReaction);
                        } else {
                            newRule.memberRemovesReactionTrigger(sourceMessageID, sourceReaction);
                        }
                    }
                    case 5, 6 -> {
                        Channel sourceChannel = event.getOption("sourcechannel", null, OptionMapping::getAsChannel);
                        if (sourceChannel != null && !sourceChannel.getType().equals(ChannelType.TEXT)) {
                            event.reply("Invaild `sourcechannel` selected").setEphemeral(true).queue();
                            return;
                        }
                        String sourceChannelID = sourceChannel == null ? null : sourceChannel.getId();
                        if (triggerType == 5) {
                            newRule.memberEditsMessageTrigger(sourceChannelID);
                        } else {
                            newRule.memberDeletesMessageTrigger(sourceChannelID);
                        }
                    }
                    default -> {
                        event.reply("Invalid trigger choice").setEphemeral(true).queue();
                        return;
                    }
                }
                int actionType = availableActions.indexOf(event.getOption("action", "", OptionMapping::getAsString)) + 1;
                switch (actionType) {
                    case 1 -> {
                        String targetMessageContent = event.getOption("targetmessage", null, OptionMapping::getAsString);
                        if (targetMessageContent == null) {
                            event.reply("`targetmessage` cannot be empty!").setEphemeral(true).queue();
                            return;
                        }
                        Channel targetChannel = event.getOption("targetchannel", null, OptionMapping::getAsChannel);
                        if (targetChannel == null || !targetChannel.getType().equals(ChannelType.TEXT)) {
                            event.reply("Invalid `targetchannal` selected").setEphemeral(true).queue();
                            return;
                        }
                        newRule.sendMessageAction(targetMessageContent, targetChannel.getId());
                    }
                    case 2, 3 -> {
                        Role targetRole = event.getOption("targetrole", null, OptionMapping::getAsRole);
                        if (targetRole == null) {
                            event.reply("`targetrole` is required").setEphemeral(true).queue();
                            return;
                        }
                        if (actionType == 2) {
                            newRule.addRoleAction(targetRole.getId());
                        } else {
                            newRule.removeRoleAction(targetRole.getId());
                        }
                    }
                    case 4 -> newRule.passToDMAction(event.getUser().getId());
                    case 5 -> {
                        Channel targetChannel = event.getOption("targetchannel", null, OptionMapping::getAsChannel);
                        if (targetChannel == null || !targetChannel.getType().equals(ChannelType.TEXT)) {
                            event.reply("Invalid `targetchannal` selected").setEphemeral(true).queue();
                            return;
                        }
                        newRule.passToChannel(targetChannel.getId());
                    }
                    default -> {
                        event.reply("Invalid action choice").setEphemeral(true).queue();
                        return;
                    }
                }
                if (Cashew.whenSettingsManager.addWhenRule(event.getGuild().getId(), newRule)) {
                    event.reply("Successfully created a new rule!").setEphemeral(true).queue();
                } else {
                    event.reply("Something went wrong while saving the new rule, try again later").setEphemeral(true).queue();
                }
            } else if (event.getSubcommandName().equals("list")) {
                int page = event.getOption("page", 1, OptionMapping::getAsInt);
                int pageCount = Cashew.whenSettingsManager.getWhenRulesPageCount(Objects.requireNonNull(event.getGuild()).getId());
                if (pageCount == 0) {
                    event.reply("There are no WhenRules set on this server yet").setEphemeral(true).queue();
                    return;
                }
                page = page < 1 ? 1 : (Math.min(page, pageCount));
                ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getWhenRulesPage(event.getGuild().getId(), page);
                if (rules == null) {
                    event.reply("Something went wrong while fetching the list of WhenRules, try again later").setEphemeral(true).queue();
                    return;
                }
                MessageEmbed rulesListEmbed = generateRulesListEmbed(rules, event.getGuild(), page);
                event.replyEmbeds(rulesListEmbed).setEphemeral(true).queue();
//                Pair<ActionRow, ActionRow> rulesListActionRows = generateRulesListActionRows(rules, event.getUser(), false);
//                event.replyEmbeds(rulesListEmbed).addComponents(rulesListActionRows.getLeft(), rulesListActionRows.getRight()).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("when")) {
            ArrayList<String> autocompletions = new ArrayList<>();
            String optionToAutocomplete = event.getFocusedOption().getName();
            String typed = event.getOption(optionToAutocomplete, "", OptionMapping::getAsString);
            switch (event.getFocusedOption().getName()) {
                case "trigger" -> autocompletions = autocompleteFromList(availableTriggers, typed);
                case "action" -> autocompletions = autocompleteFromList(availableActions, typed);
            }
            if (!autocompletions.isEmpty()) {
                event.replyChoiceStrings(autocompletions).queue();
            }
        }
    }
}
