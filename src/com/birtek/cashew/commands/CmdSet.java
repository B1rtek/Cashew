package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class CmdSet extends BaseCommand {

    private final ArrayList<String> availableCommands = new ArrayList<>(Help.commands.subList(2, Help.commands.size() - 1));

    private final ArrayList<String> toggleOptions = new ArrayList<>() {
        {
            add("on");
            add("off");
        }
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("cmdset")) {
            if(cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            boolean state = !event.getOption("toggle", "", OptionMapping::getAsString).equals("off");
            String command = event.getOption("command", "", OptionMapping::getAsString);
            if (!command.isEmpty() && !availableCommands.contains(command)) {
                event.reply("Invalid command specified").setEphemeral(true).queue();
                return;
            }
            command = command.isEmpty() ? "all" : command;
            GuildChannel channel = event.getOption("channel", null, OptionMapping::getAsChannel);
            String channelID = "all";
            if (channel != null) {
                channelID = channel.getId();
            }
            String serverID = Objects.requireNonNull(event.getGuild()).getId();
            if(Cashew.commandsSettingsManager.updateCommandSettings(serverID, channelID, command, state)) {
                event.reply("Successfully updated commands settings!").setEphemeral(true).queue();
            } else {
                event.reply("Something went wrong while changing the settings").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals("cmdset")) {
            if(event.getFocusedOption().getName().equals("toggle")) {
                String typed = event.getOption("toggle", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(toggleOptions, typed)).queue();
            } else if (event.getFocusedOption().getName().equals("command")) {
                String typed = event.getOption("command", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(availableCommands, typed)).queue();
            }
        }
    }
}
