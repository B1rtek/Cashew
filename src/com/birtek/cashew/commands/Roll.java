package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class Roll extends BaseCommand {

    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("roll")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            int sides = event.getOption("sides", 6, OptionMapping::getAsInt);
            int rolls = Math.min(event.getOption("rolls", 1, OptionMapping::getAsInt), 10000);
            long sum = 0;
            StringBuilder stringResults = new StringBuilder();
            for (int i = 0; i < rolls; i++) {
                int rand = random.nextInt(sides) + 1;
                sum += rand;
                stringResults.append(rand).append(", ");
            }
            stringResults.delete(stringResults.length() - 2, stringResults.length());
            double mean = Math.round((double) sum / rolls * 100.0) / 100.0;
            if (stringResults.length() > 1024) {
                stringResults = new StringBuilder(stringResults.toString().replace(", ", "\n"));
                stringResults.append("\n Sum: ").append(sum).append(", mean: ").append(mean);
                event.reply("Results were too long to fit in an embed, so here's a file with all of them").queue();
                event.getChannel().sendFile(new ByteArrayInputStream(stringResults.toString().getBytes()), "results.txt").queue();
            } else {
                EmbedBuilder rollEmbed = new EmbedBuilder();
                rollEmbed.setTitle("Roll");
                rollEmbed.setDescription(sides + " sides, " + rolls + " rolls");
                rollEmbed.addField("Results", stringResults.toString(), false);
                rollEmbed.setFooter("Sum: " + sum + ", mean: " + mean);
                rollEmbed.setThumbnail("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/2-Dice-Icon.svg/1200px-2-Dice-Icon.svg.png");
                event.replyEmbeds(rollEmbed.build()).queue();
            }

        }
    }
}
