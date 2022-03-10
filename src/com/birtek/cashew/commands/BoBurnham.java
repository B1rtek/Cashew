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
import java.util.Random;

public class BoBurnham extends BaseCommand {

    Permission[] boBurnhamCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    private MessageEmbed getQuoteEmbed(boolean nsfw) {
        Database database = Database.getInstance();
        int count = database.getQuoteCount(nsfw ? 1 : 0);
        if (count == 0) return null;
        Random rand = new Random();
        int quoteNumber = rand.nextInt(count) + 1;
        ResultSet quotes = database.getQuotes(nsfw ? 1 : 0);
        if (quotes == null) return null;
        try {
            EmbedBuilder quoteEmbed = new EmbedBuilder();
            int rowNumber = 1;
            while (quotes.next()) {
                if (rowNumber < quoteNumber) {
                    rowNumber++;
                } else {
                    String quoteContent = quotes.getString("quote");
                    String[] quoteParts = quoteContent.split("▒");
                    StringBuilder description = new StringBuilder();
                    for (String quotePart : quoteParts) {
                        description.append(quotePart);
                        description.append('\n');
                    }
                    quoteEmbed.setDescription(description.toString());
                    quoteEmbed.setAuthor(quotes.getString("track"), quotes.getString("link"), quotes.getString("albumCover"));
                    quoteEmbed.setFooter("from \"" + quotes.getString("album") + "\"");
                    return quoteEmbed.build();
                }
            }

        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "boburnham")) {
            if (checkPermissions(event, boBurnhamCommandPermissions)) {
                boolean nsfw = false;
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("nsfw")) {
                        nsfw = true;
                    }
                }
                MessageEmbed quoteEmbed = getQuoteEmbed(nsfw);
                if (quoteEmbed != null) {
                    event.getChannel().sendMessageEmbeds(quoteEmbed).queue();
                } else {
                    event.getMessage().reply("Something went wrong while executing this command").mentionRepliedUser(false).queue();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("boburnham")) {
            if (checkSlashCommandPermissions(event, boBurnhamCommandPermissions)) {
                boolean nsfw = event.getOption("nsfw", "sfw", OptionMapping::getAsString).equals("nsfw");
                MessageEmbed quoteEmbed = getQuoteEmbed(nsfw);
                if (quoteEmbed != null) {
                    event.replyEmbeds(quoteEmbed).queue();
                } else {
                    event.reply("Something went wrong while executing this command").setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equals("boburnham")) {
            event.replyChoiceStrings("nsfw", "sfw").queue();
        }
    }
}