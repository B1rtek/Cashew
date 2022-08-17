package com.birtek.cashew.commands;

import com.birtek.cashew.database.BestNekoDatabase;
import com.birtek.cashew.database.BestNekoGifsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class BestNeko extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(BestNeko.class);
    private final ArrayList<String> nekos;
    private final ArrayList<ArrayList<String>> nekoGifs;

    /**
     * Gets lists of nekos and neko gifs from the database
     */
    public BestNeko() {
        BestNekoGifsDatabase database = BestNekoGifsDatabase.getInstance();
        nekos = database.getNekos();
        nekoGifs = database.getNekoGifs();
    }

    /**
     * Gets the ID of the neko from the nekos list
     *
     * @param neko name of the neko
     * @return ID of the neko from the database, or 0 if that neko doesn't exist
     */
    private int getNekoID(String neko) {
        int index = 1;
        for(String nekoName: nekos) {
            if(nekoName.equalsIgnoreCase(neko)) return index;
            index++;
        }
        return 0;
    }

    /**
     * Gets the name of the neko with the given ID
     *
     * @param id ID of the neko to get the name of
     * @return name of the neko in a String
     */
    private String getNekoName(int id) {
        return nekos.get(id - 1);
    }

    /**
     * Gets a random gif from the neko gifs collection of the chosen neko
     *
     * @param id ID of the neko whose gif will be chosen
     * @return a link to a random gif of the chosen neko
     */
    private String getRandomFavouriteNekoGif(int id) {
        Random random = new Random();
        return nekoGifs.get(id - 1).get(random.nextInt(nekoGifs.get(id - 1).size()));
    }

    /**
     * Creates an embed with the chosen gif and chosen neko name
     *
     * @param nekoGif  gif of the chosen neko
     * @param nekoName name of the chosen neko
     * @return a {@link MessageEmbed MessageEmbed} with a gif of the neko
     */
    private MessageEmbed createBestNekoEmbed(String nekoGif, String nekoName) {
        EmbedBuilder bestNekoEmbed = new EmbedBuilder();
        bestNekoEmbed.setAuthor("❤️  " + nekoName + "  ❤️");
        bestNekoEmbed.setImage(nekoGif);
        bestNekoEmbed.setFooter("Best neko!");
        return bestNekoEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bestneko")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            if (Objects.equals(event.getSubcommandName(), "set")) {
                String neko = event.getOption("neko", "Maple", OptionMapping::getAsString);
                int id = getNekoID(neko);
                if (id == 0) {
                    event.reply("This neko doesn't exist!").setEphemeral(true).queue();
                    return;
                }
                BestNekoDatabase database = BestNekoDatabase.getInstance();
                if (database.setNeko(event.getUser().getId(), id)) {
                    event.reply("Favourite neko successfully set to " + getNekoName(id) + "!").setEphemeral(true).queue();
                } else {
                    event.reply("Something went wrong while setting your favourite neko, try again later").setEphemeral(true).queue();
                }
            } else if (Objects.equals(event.getSubcommandName(), "send")) {
                BestNekoDatabase database = BestNekoDatabase.getInstance();
                int id = database.getNeko(event.getUser().getId());
                if (id == 0) {
                    event.reply("You didn't set your favourite neko yet, set the neko with /bestneko set").setEphemeral(true).queue();
                    return;
                } else if (id == -1) {
                    event.reply("Something went wrong while getting your favourite neko, try again later").setEphemeral(true).queue();
                    return;
                }
                String nekoGif = getRandomFavouriteNekoGif(id);
                event.replyEmbeds(createBestNekoEmbed(nekoGif, getNekoName(id))).queue();
            } else if (Objects.equals(event.getSubcommandName(), "chart")) {
                BestNekoDatabase database = BestNekoDatabase.getInstance();
                ArrayList<Pair<String, Integer>> distribution = database.getNekosDistribution();
                if(distribution.isEmpty()) {
                    event.reply("No one chose their favourite neko yet!").setEphemeral(true).queue();
                    return;
                }
                BestNekoGifsDatabase gifsDatabase = BestNekoGifsDatabase.getInstance();
                HashMap<String, Color> nekoColors = gifsDatabase.getNekoColors();
                String chartName = "Favourite nekos distribution chart";
                InputStream bestNekoPiechart = generatePiechart(distribution, nekoColors, chartName);
                if(bestNekoPiechart == null) {
                    LOGGER.warn("/bestneko chart generated a null image!");
                    event.reply("Failed to generate the piechart, try again later").setEphemeral(true).queue();
                    return;
                }
                EmbedBuilder piechartEmbed = new EmbedBuilder();
                piechartEmbed.setTitle(chartName);
                double total = 0.0;
                for(Pair<String, Integer> neko: distribution) {
                    total += neko.getRight();
                }
                for(Pair<String, Integer> neko: distribution) {
                    String percentage = Math.round((double) neko.getRight() * 100.0 / total * 100.0) / 100.0 + " %";
                    piechartEmbed.addField(neko.getLeft(), percentage, true);
                }
                piechartEmbed.setImage("attachment://piechart.png");
                piechartEmbed.setColor(nekoColors.get(distribution.get(0).getLeft()));
                piechartEmbed.setFooter("Total votes: " + (int) total);
                event.replyFile(bestNekoPiechart, "piechart.png").addEmbeds(piechartEmbed.build()).queue();
            } else {
                event.reply("No subcommand specified (how???)").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().startsWith("bestneko")) {
            if (event.getFocusedOption().getName().equals("neko")) {
                String typed = event.getOption("neko", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(nekos, typed)).queue();
            }
        }
    }
}