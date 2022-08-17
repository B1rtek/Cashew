package com.birtek.cashew.commands;

import com.birtek.cashew.database.EmbedGif;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;
import java.util.Random;

public class BaseCuddlyCommand extends BaseCommand {

    /**
     * Merges the string array into one string while turning all mentions into their effective member names
     *
     * @param splitWithMentions array of strings, the result of .split("\\s+")ting the command input
     * @param guild             {@link Guild Server} in which the command was executed
     * @param ignoreFirst       if set to true, will ignore the first element of the string array, used to omit the $command
     *                          part in case of prefix commands being used
     * @return String with merged and processed pings from the command input
     */
    public String purifyFromMentionsAndMerge(String[] splitWithMentions, Guild guild, boolean ignoreFirst) {
        int start = ignoreFirst ? 1 : 0;
        for (int i = start; i < splitWithMentions.length; i++) {
            if (splitWithMentions[i].length() == 22 && splitWithMentions[i].startsWith("<@!") && splitWithMentions[i].endsWith(">")) {
                String id = splitWithMentions[i].substring(3, 21);
                String name = Objects.requireNonNull(guild.getMemberById(id)).getEffectiveName();
                splitWithMentions[i] = name;
            }
            if (splitWithMentions[i].length() == 21 && splitWithMentions[i].startsWith("<@") && splitWithMentions[i].endsWith(">")) {
                String id = splitWithMentions[i].substring(2, 20);
                String name = Objects.requireNonNull(guild.getMemberById(id)).getEffectiveName();
                splitWithMentions[i] = name;
            }
        }
        StringBuilder result = new StringBuilder();
        for (int i = start; i < splitWithMentions.length; i++) {
            result.append(splitWithMentions[i]);
            if (i != splitWithMentions.length - 1) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    /**
     * Creates an {@link MessageEmbed embed} with a gif and a message saying "someone cuddles/pats/other someone else!"
     *
     * @param cuddlyString String generated from
     *                     {@link #purifyFromMentionsAndMerge(String[], Guild, boolean) purifyFromMentionsAndMerge()}
     * @param author       {@link Member server member} who executed the command
     * @param cuddlyGifs   an array of {@link EmbedGif EmbedGifs}, which contain both the gif url and their theme color
     * @param action       String placed between the executor's name and the rest of the input, int this case
     *                     "cuddles/pats" etc so together it creates "B1rtek pats Cashew"
     * @param reactions    an array of Strings like "owo" or "uwu" that are randomly appended at the end of the message
     * @return a {@link MessageEmbed MessageEmbed} with a gif and a message containing the performed cuddly action
     */
    protected MessageEmbed createCuddlyEmbed(String cuddlyString, Member author, EmbedGif[] cuddlyGifs, String action, String[] reactions) {
        Random random = new Random();
        int gifNumber = random.nextInt(cuddlyGifs.length);
        EmbedBuilder cuddleEmbed = new EmbedBuilder();
        cuddlyString = author.getEffectiveName() + " " + action + " " + cuddlyString + "! " + reactions[random.nextInt(reactions.length)];
        cuddleEmbed.setColor(cuddlyGifs[gifNumber].getColor());
        cuddleEmbed.setImage(cuddlyGifs[gifNumber].getGifURL());
        cuddleEmbed.setTitle(cuddlyString);
        cuddleEmbed.setFooter("by " + author.getEffectiveName(), author.getEffectiveAvatarUrl());
        return cuddleEmbed.build();
    }

    /**
     * Gets a cuddly embed and then responds with it. Used with the prefix versions of the command, will be removed in
     * the future when prefix commands are abandoned for good
     *
     * @param event        {@link MessageReceivedEvent event} received when the message with the command was spotted
     * @param cuddlyString String generated from
     *                     {@link #purifyFromMentionsAndMerge(String[], Guild, boolean) purifyFromMentionsAndMerge()}
     * @param gifs         an array of {@link EmbedGif EmbedGifs}, which contain both the gif url and their theme color
     * @param action       String placed between the executor's name and the rest of the input, int this case
     *                     "cuddles/pats" etc so together it creates "B1rtek pats Cashew"
     * @param reactions    an array of Strings like "owo" or "uwu" that are randomly appended at the end of the message
     */
    protected void sendCuddlyEmbedFromPrefix(MessageReceivedEvent event, String cuddlyString, EmbedGif[] gifs, String action, String[] reactions) {
        MessageEmbed cuddlyEmbed;
        if (event.isWebhookMessage()) {
            cuddlyEmbed = createCuddlyEmbed(cuddlyString, Objects.requireNonNull(event.getMember()), gifs, action, reactions);
        } else {
            cuddlyEmbed = createCuddlyEmbed(cuddlyString, Objects.requireNonNull(event.getMember()), gifs, action, reactions);
        }
        event.getMessage().replyEmbeds(cuddlyEmbed).queue();
    }
}
