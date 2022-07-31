package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.Reaction;
import com.birtek.cashew.database.ReactionsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class Reactions extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reactions.class);

    private final Permission[] reactionsCommandPermissions = {
            Cashew.moderatorPermission
    };

    private final ArrayList<String> toggleOptions = new ArrayList<>() {
        {
            add("on");
            add("off");
        }
    };

    private HashMap<String, Reaction> reactionsMap;
    private ArrayList<String> availableReactions;

    /**
     * Gets all reactions from the database and adds them to the list of available reactions and a map that assigns an
     * ID to every reaction name. If that fails, bot exits with an error message
     */
    public Reactions() {
        ReactionsDatabase database = ReactionsDatabase.getInstance();
        ArrayList<Reaction> reactions = database.getAllReactions();
        if (reactions == null) {
            LOGGER.error("Failed to obtain the reactions list from the database!");
            System.exit(1);
        }
        for (Reaction reaction : reactions) {
            availableReactions.add(reaction.name());
            reactionsMap.put(reaction.name(), reaction);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reactions")) {
            String reaction = event.getOption("reaction", "", OptionMapping::getAsString);
            if (Objects.equals(event.getSubcommandName(), "set")) {
                int reactionID = 0;
                if (!reaction.isEmpty()) {
                    Reaction chosenReaction = reactionsMap.get(reaction);
                    if (chosenReaction == null) {
                        event.reply("This reaction doesn't exist").setEphemeral(true).queue();
                        return;
                    }
                    reactionID = chosenReaction.id();
                }
                if (!checkSlashCommandPermissions(event, reactionsCommandPermissions)) {
                    event.reply("This command can only be used by server moderators").setEphemeral(true).queue();
                    return;
                }
                GuildChannel channel = event.getOption("channel", null, OptionMapping::getAsChannel);
                if (channel == null) {
                    event.reply("Invalid channel specified").setEphemeral(true).queue();
                    return;
                }
                String channelID = channel.getId();
                String serverID = Objects.requireNonNull(event.getGuild()).getId();
                boolean state = Objects.equals(event.getOption("toggle", "", OptionMapping::getAsString), "on");
                if (Cashew.reactionsSettingsManager.updateActivitySettings(serverID, channelID, reactionID, state)) {
                    event.reply("Reactions settings were successfully updated").setEphemeral(true).queue();
                } else {
                    event.reply("Something went wrong while applying the settings").setEphemeral(true).queue();
                }
            } else if (Objects.equals(event.getSubcommandName(), "info")) {
                Reaction chosenReaction = reactionsMap.get(reaction);
                if (chosenReaction == null) {
                    event.reply("This reaction doesn't exist").setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(createReactionInfoEmbed(chosenReaction)).setEphemeral(true).queue();
                }
            } else {
                event.reply("No subcommand chosen (how????)").setEphemeral(true).queue();
            }
        }
    }

    /**
     * Creates an embed representing a Reaction in a nice form for a user
     *
     * @param chosenReaction {@link Reaction Reaction} which will be described by the embed
     * @return {@link MessageEmbed MessageEmbed} with details about the Reaction
     */
    private MessageEmbed createReactionInfoEmbed(Reaction chosenReaction) {
        EmbedBuilder reactionEmbed = new EmbedBuilder();
        reactionEmbed.setTitle(chosenReaction.name());
        StringBuilder reactionTriggers = new StringBuilder();
        for (String pattern : chosenReaction.patterns()) {
            reactionTriggers.append(pattern).append(", ");
        }
        reactionTriggers.delete(reactionTriggers.length() - 2, 2);
        reactionEmbed.addField("Triggers", reactionTriggers.toString(), false);
        reactionEmbed.setDescription(chosenReaction.description());
        return reactionEmbed.build();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("reactions")) {
            if (event.getFocusedOption().getName().equals("toggle")) {
                String typed = event.getOption("toggle", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(toggleOptions, typed)).queue();
            } else if (event.getFocusedOption().getName().equals("reaction")) {
                String typed = event.getOption("reaction", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(availableReactions, typed)).queue();
            }
        }
    }
}