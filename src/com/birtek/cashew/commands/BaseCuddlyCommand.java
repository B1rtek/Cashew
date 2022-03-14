package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.Objects;
import java.util.Random;

public class BaseCuddlyCommand extends BaseCommand {

    public String purifyFromMentionsAndMerge(String[] splitWithMentions, Guild guild, boolean ignoreFirst) {
        int start = ignoreFirst ? 1 : 0;
        for (int i = start; i < splitWithMentions.length; i++) {
            if (splitWithMentions[i].length() == 22 && splitWithMentions[i].startsWith("<@!") && splitWithMentions[i].endsWith(">")) {
                String id = splitWithMentions[i].substring(3, 21);
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

    String[] reactions = {
            "UwU", "OwO", ":3", ";3", "Nyaaa!", "<3", "Yayy!", "Cute~", "Adorable~"
    };

    protected MessageEmbed createCuddlyEmbed(String cuddlyString, User author, String authorName, EmbedGif[] cuddlyGifs, String action) {
        Random random = new Random();
        int gifNumber = random.nextInt(cuddlyGifs.length);
        EmbedBuilder cuddleEmbed = new EmbedBuilder();
        cuddlyString = authorName + " " + action + " " + cuddlyString + "! " + reactions[random.nextInt(reactions.length)];
        cuddleEmbed.setColor(cuddlyGifs[gifNumber].getColor());
        cuddleEmbed.setImage(cuddlyGifs[gifNumber].getGifURL());
        cuddleEmbed.setAuthor(cuddlyString, null, author.getAvatarUrl());
        return cuddleEmbed.build();
    }
}
