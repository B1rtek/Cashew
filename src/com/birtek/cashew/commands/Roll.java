package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Roll extends BaseCommand {

    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("roll")) {
            int sides = event.getOption("sides", 6, OptionMapping::getAsInt);
            int rolls = Math.min(event.getOption("rolls", 1, OptionMapping::getAsInt), 100);
            long sum = 0;
            StringBuilder stringResults = new StringBuilder();
            for (int i = 0; i < rolls; i++) {
                int rand = random.nextInt(sides) + 1;
                sum += rand;
                stringResults.append(rand).append(", ");
            }
            stringResults.delete(stringResults.length() - 2, stringResults.length());
            double mean = Math.round((double) sum / rolls * 100.0) / 100.0;
            EmbedBuilder rollEmbed = new EmbedBuilder();
            rollEmbed.setTitle("Roll");
            rollEmbed.setDescription(sides + " sides, " + rolls + " rolls");
            rollEmbed.addField("Results", stringResults.toString(), false);
            rollEmbed.setFooter("Sum: " + sum, "mean: " + mean);
            event.replyEmbeds(rollEmbed.build()).queue();
        }
    }
}
