package com.birtek.cashew.commands;

import com.birtek.cashew.database.Card;
import com.birtek.cashew.database.ManyDecksWebscraper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

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
                StringBuilder deckContent = new StringBuilder("```\n");
                for (Card card : deck) {
                    deckContent.append(card.color()?"white: ":"black: ");
                    for(String part: card.content()) {
                        deckContent.append(part).append(" ");
                    }
                    deckContent.append('\n');
                }
                deckContent.append("```");
                event.reply(deckContent.toString()).queue();
            } else {
                event.reply("Not implemented yet").setEphemeral(true).queue();
            }
        }
    }
}
