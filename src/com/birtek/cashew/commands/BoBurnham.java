package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class BoBurnham extends BaseCommand {

    Permission[] boBurnhamCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "boburnham")) {
            if(checkPermissions(event, boBurnhamCommandPermissions)) {
                int nsfw = 0;
                if(args.length>=2) {
                    if(args[1].equalsIgnoreCase("nsfw")) {
                        nsfw = 1;
                    }
                }
                Database database = Database.getInstance();
                int count = database.getQuoteCount(nsfw);
                if(count>0) {
                    Random rand = new Random();
                    int quoteNumber = rand.nextInt(count)+1;
                    ResultSet quotes = database.getQuotes(nsfw);
                    if(quotes!=null) {
                        try {
                            EmbedBuilder quoteEmbed = new EmbedBuilder();
                            int rowNumber = 1;
                            while(quotes.next()) {
                                if(rowNumber<quoteNumber) {
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
                                    quoteEmbed.setFooter("from \""+ quotes.getString("album")+"\"");
                                    break;
                                }
                            }
                            event.getChannel().sendMessageEmbeds(quoteEmbed.build()).queue();

                        } catch (SQLException e) {
                            e.printStackTrace();
                            event.getMessage().reply("Something went wrong...").mentionRepliedUser(false).queue();
                        }
                    } else {
                        event.getMessage().reply("Something went wrong...").mentionRepliedUser(false).queue();
                    }
                } else {
                    event.getMessage().reply("Something went wrong...").mentionRepliedUser(false).queue();
                }
            }
        }
    }
}