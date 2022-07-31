package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.timings.PollSummarizer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A {@link net.dv8tion.jda.api.hooks.ListenerAdapter listener} for the /poll command, used to create polls
 */
public class Poll extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Poll.class);

    private static final ArrayList<Emoji> optionEmoji = new ArrayList<>() {
        {
            add(Emoji.fromUnicode("1️⃣"));
            add(Emoji.fromUnicode("2️⃣"));
            add(Emoji.fromUnicode("3️⃣"));
            add(Emoji.fromUnicode("4️⃣"));
            add(Emoji.fromUnicode("5️⃣"));
        }
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("poll") && checkSlashCommandPermissions(event, modPermissions)) {
            EmbedBuilder pollEmbed = new EmbedBuilder();
            String pollTitle = event.getOption("title", null, OptionMapping::getAsString);
            if (pollTitle == null) {
                event.reply("Poll must have a title").setEphemeral(true).queue();
                return;
            }
            int time = event.getOption("timetovote", 24, OptionMapping::getAsInt);
            if (time == 0) {
                event.reply("Time to vote must not be zero").setEphemeral(true).queue();
                return;
            }
            String unit = event.getOption("unit", "hours", OptionMapping::getAsString);
            if (!timeUnits.contains(unit)) {
                event.reply("Invalid time unit specified").setEphemeral(true).queue();
                return;
            }
            String timeString = calculateTargetTime(time, unit);
            ArrayList<String> options = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String optionName = "option" + i;
                String option = event.getOption(optionName, null, OptionMapping::getAsString);
                if (option != null) options.add(option);
            }
            if (options.size() < 2) {
                event.reply("Poll must have at least two options").setEphemeral(true).queue();
                return;
            }
            pollEmbed.setTitle("Poll: " + pollTitle);
            for (int i = 0; i < options.size(); i++) {
                pollEmbed.addField(optionEmoji.get(i).getName(), options.get(i), false);
            }
            pollEmbed.setFooter("Ends at");
            pollEmbed.setTimestamp(calculateInstantTargetTime(time, unit));
            event.getChannel().sendMessageEmbeds(pollEmbed.build()).queue(pollMessage -> {
                for (int i = 0; i < options.size(); i++) {
                    pollMessage.addReaction(optionEmoji.get(i)).queue();
                }
                PollSummarizer poll = new PollSummarizer(0, event.getChannel().getId(), pollMessage.getId(), timeString);
                if (Cashew.pollManager.addPoll(poll)) {
                    event.reply("Poll created!").setEphemeral(true).queue();
                } else {
                    event.reply("Something went wrong while setting up the poll").setEphemeral(true).queue();
                    event.getChannel().deleteMessageById(pollMessage.getId()).queue();
                }
            });
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("poll")) {
            if (event.getFocusedOption().getName().equals("unit")) {
                event.replyChoiceStrings(autocompleteFromList(timeUnits, event.getOption("unit", "", OptionMapping::getAsString))).queue();
            }
        }
    }
}
