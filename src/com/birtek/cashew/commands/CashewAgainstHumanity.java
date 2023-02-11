package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.Card;
import com.birtek.cashew.database.ManyDecksWebscraper;
import com.birtek.cashew.timings.CAHGame;
import com.google.common.base.Charsets;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class CashewAgainstHumanity extends BaseCommand {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("cah")) {
            if (cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            if (!event.getChannel().canTalk()) {
                event.reply("This command won't work in here, Cashew can't see or/and write in this channel!").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName() == null) {
                event.reply("Bad command specified (how???)").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName().equals("deck")) {
                String deckCode = event.getOption("deck", null, OptionMapping::getAsString);
                if (deckCode == null || deckCode.length() != 5) {
                    event.reply("Invalid deck code").setEphemeral(true).queue();
                    return;
                }
                ArrayList<Card> deck = ManyDecksWebscraper.getDeck(deckCode);
                if(deck == null) {
                    event.reply("Something went wrong while downloading the deck or it does not exist").setEphemeral(true).queue();
                    return;
                }
                StringBuilder deckContent = new StringBuilder();
                for (Card card : deck) {
                    deckContent.append(card.color()?"⬜ ":"⬛ ");
                    for(String part: card.content()) {
                        deckContent.append(part).append(" ");
                    }
                    deckContent.append('\n');
                }
                event.replyFiles(FileUpload.fromData(new ByteArrayInputStream(deckContent.toString().getBytes(Charsets.UTF_8)), "results.txt")).queue();
            } else if (event.getSubcommandName().equals("create")) {
                if(!Cashew.cahGameManager.joinGame(event.getUser().getId())) {
                    event.reply("Failed to create a game!").setEphemeral(true).queue();
                    return;
                }
                CAHGame createdGame = Cashew.cahGameManager.getGame(event.getUser().getId());
                if(createdGame == null) {
                    event.reply("Failed to create a game!").setEphemeral(true).queue();
                    return;
                }
                EmbedBuilder newGameEmbed = new EmbedBuilder();
                newGameEmbed.setTitle("New Cashew Against Humanity Game");
                newGameEmbed.addField("Players: ", createdGame.getPlayersList(), false);
                newGameEmbed.addField("Decks: ", createdGame.getDecksList(), false);
                newGameEmbed.setFooter("Join code: " + createdGame.getGameCode());
                event.replyEmbeds(newGameEmbed.build()).queue();
            } else {
                event.reply("Not implemented yet").setEphemeral(true).queue();
            }
        }
    }
}
