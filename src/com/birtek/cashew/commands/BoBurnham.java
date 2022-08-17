package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.BoBurnhamDatabase;
import com.birtek.cashew.database.BoBurnhamQuote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

/**
 * /boburnham command class
 * Sends quotes from Bo Burnham on demand
 * (I just wish that my database had more of them)
 */
public class BoBurnham extends BaseCommand {

    /**
     * Creates an embed from the provided BoBurnhamQuote
     * @param quote quote to turn into an embed
     */
    private MessageEmbed createQuoteEmbed(BoBurnhamQuote quote) {
        EmbedBuilder quoteEmbed = new EmbedBuilder();
        String quoteContent = quote.quote();
        String[] quoteParts = quoteContent.split("▒");
        StringBuilder description = new StringBuilder();
        for (String quotePart : quoteParts) {
            description.append(quotePart);
            description.append('\n');
        }
        quoteEmbed.setDescription(description.toString());
        quoteEmbed.setAuthor(quote.track(), quote.link(), quote.albumCoverUrl());
        quoteEmbed.setFooter("from \"" + quote.album() + "\"");
        return quoteEmbed.build();
    }

    private MessageEmbed getQuoteEmbed(boolean nsfw) {
        BoBurnhamDatabase database = BoBurnhamDatabase.getInstance();
        BoBurnhamQuote quote = database.getQuote(nsfw);
        if(quote != null) {
            return createQuoteEmbed(quote);
        } else {
            return null;
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "boburnham")) {
            if(cantBeExecutedPrefix(event, "boburnham", false)) {
                event.getMessage().reply("This command is turned off in this channel").mentionRepliedUser(false).queue();
                return;
            }
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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("boburnham")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            boolean nsfw = event.getOption("nsfw", "sfw", OptionMapping::getAsString).equals("nsfw");
            MessageEmbed quoteEmbed = getQuoteEmbed(nsfw);
            if (quoteEmbed != null) {
                event.replyEmbeds(quoteEmbed).queue();
            } else {
                event.reply("Something went wrong while executing this command").setEphemeral(true).queue();
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