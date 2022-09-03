package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.WhenRule;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class When extends BaseCommand {

    private ArrayList<String> availableTriggers = new ArrayList<>() {
        {
            add("member joins the server");
            add("member leaves the server");
            add("member reacts to a message <sourceMessageID> <sourceReactionEmote>");
            add("member removes a reaction <sourceMessageID> <sourceReactionEmote>");
            add("member edits a message [#sourceChannel, optional]");
            add("member deletes a message [#sourceChannel, optional]");
        }
    };

    private ArrayList<String> availableActions = new ArrayList<>() {
        {
            add("send a message <messageContent> <#targetChannel>");
            add("add a role to the interacting user <@targetRole>");
            add("remove a role from the interacting user <@targetRole>");
            add("pass the information about the event to your DM");
            add("pass the information to a channel <#targetChannel>");
        }
    };

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
                event.reply("//TODO not implemented yet").setEphemeral(true).queue();
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
