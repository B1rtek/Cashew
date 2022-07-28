package com.birtek.cashew.commands;

import com.birtek.cashew.database.BestNekoDatabase;
import com.birtek.cashew.database.BestNekoGifsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class BestNeko extends BaseCommand {

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
        return nekos.indexOf(neko) + 1;
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
        bestNekoEmbed.setTitle("❤️ " + nekoName + " ❤️");
        bestNekoEmbed.setImage(nekoGif);
        bestNekoEmbed.setFooter("Best neko!");
        return bestNekoEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (Objects.equals(event.getSubcommandName(), "set")) {
            String neko = event.getOption("neko", "Maple", OptionMapping::getAsString);
            int id = getNekoID(neko);
            if (id == 0) {
                event.reply("This neko doesn't exist!").setEphemeral(true).queue();
                return;
            }
            BestNekoDatabase database = BestNekoDatabase.getInstance();
            if (database.setNeko(event.getUser().getId(), id)) {
                event.reply("Favourite neko successfully set to " + neko + "!").setEphemeral(true).queue();
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
        } else {
            event.reply("No subcommand specified (how???)").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().startsWith("bestneko")) {
            if(event.getFocusedOption().getName().equals("neko")) {
                String typed = event.getOption("neko", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(nekos, typed)).queue();
            }
        }
    }
}