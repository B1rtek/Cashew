package com.birtek.cashew.commands;

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
     * @param author
     * @param authorName
     * @param cuddlyGifs
     * @param action
     * @param reactions
     * @return
     */
    protected MessageEmbed createCuddlyEmbed(String cuddlyString, Member author, String authorName, EmbedGif[] cuddlyGifs, String action, String[] reactions) {
        Random random = new Random();
        int gifNumber = random.nextInt(cuddlyGifs.length);
        EmbedBuilder cuddleEmbed = new EmbedBuilder();
        cuddlyString = authorName + " " + action + " " + cuddlyString + "! " + reactions[random.nextInt(reactions.length)];
        cuddleEmbed.setColor(cuddlyGifs[gifNumber].getColor());
        cuddleEmbed.setImage(cuddlyGifs[gifNumber].getGifURL());
        cuddleEmbed.setTitle(cuddlyString);
        cuddleEmbed.setFooter("by " + author.getEffectiveName(), author.getEffectiveAvatarUrl());
        return cuddleEmbed.build();
    }

    protected void sendCuddlyEmbedFromPrefix(MessageReceivedEvent event, String cuddlyString, EmbedGif[] gifs, String action, String[] reactions) {
        String author;
        MessageEmbed cuddlyEmbed;
        if (event.isWebhookMessage()) {
            author = event.getAuthor().getName();
            cuddlyEmbed = createCuddlyEmbed(cuddlyString, Objects.requireNonNull(event.getMember()), author, gifs, action, reactions);
        } else {
            author = Objects.requireNonNull(event.getMember()).getEffectiveName();
            cuddlyEmbed = createCuddlyEmbed(cuddlyString, event.getMember(), author, gifs, action, reactions);
        }
        event.getMessage().replyEmbeds(cuddlyEmbed).queue();
    }
}
