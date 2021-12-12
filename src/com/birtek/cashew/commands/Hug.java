package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class Hug extends BaseCommand {

    Permission[] hugCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    EmbedGif[] hugGifs = {
            new EmbedGif("https://c.tenor.com/NE54PXHDQ8sAAAAC/chocola-vanilla.gif", 0xFCA6C4),
            new EmbedGif("https://c.tenor.com/WvWIdHc4RcMAAAAC/cinnamon-maple.gif", 0xF38BAB),
            new EmbedGif("https://c.tenor.com/er-bRdahGsoAAAAC/chocola-kashou.gif", 0xE4C594),
            new EmbedGif("https://c.tenor.com/eWN-9473eYMAAAAC/chocola-cacao.gif", 0x6B545F),
            new EmbedGif("https://c.tenor.com/6nZ5yOA-av4AAAAC/cacao-shigure.gif", 0xC2E4DB),
            new EmbedGif("https://c.tenor.com/Dib7E_QAgm4AAAAC/chocola-vanilla.gif", 0x70C33F),
            new EmbedGif("https://c.tenor.com/X9jHE2ReNFMAAAAC/chocola-cacao.gif", 0xDE6D79),
            new EmbedGif("https://c.tenor.com/ABL6eJAYkYEAAAAC/chocola-cacao.gif", 0xF8F1E9),
            new EmbedGif("https://c.tenor.com/a8IKk6mINHMAAAAC/cacao-chocola.gif", 0xF68295),
            new EmbedGif("https://c.tenor.com/sRKCDkKmy2IAAAAC/cacao-chocola.gif", 0x5A5873),
            new EmbedGif("https://c.tenor.com/ut7VdsSFjEMAAAAC/chocola-vanilla.gif", 0x5C909A),
            new EmbedGif("https://c.tenor.com/jfVfPs37_xcAAAAC/shigure-kashou.gif", 0xDCC3AC),
            new EmbedGif("https://c.tenor.com/R0ghzmd8qX8AAAAC/shigure-maple.gif", 0x2B3244),
            new EmbedGif("https://c.tenor.com/Q1oaZM8mbfQAAAAC/chiyo-cacao.gif", 0xCCB4E4),
            new EmbedGif("https://c.tenor.com/udA2QWUHst8AAAAC/chocola-vanilla.gif", 0xFAE0DB),
            new EmbedGif("https://c.tenor.com/zSEfmuYW6aoAAAAC/chocola-vanilla.gif", 0x5878D6),
            new EmbedGif("https://c.tenor.com/cqrKEII-huIAAAAC/coconut-azuki.gif", 0x949EF0)
    };

    String[] reactions = {
            "UwU", "OwO", ":3", ";3", "Nyaaa!", "<3", "Yayy!", "Cute~", "Adorable~"
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "hug")) {
            if(checkPermissions(event, hugCommandPermissions)) {
                Random random = new Random();
                int gifNumber = random.nextInt(hugGifs.length);
                String[] betterArgs = event.getMessage().getContentDisplay().replaceAll("@", "").split("\\s+");
                StringBuilder embedMessage;
                if(event.isWebhookMessage()) {
                    embedMessage = new StringBuilder(event.getAuthor().getName() + " hugs");
                } else {
                    if(Objects.requireNonNull(event.getMember()).getNickname()==null) {
                        embedMessage = new StringBuilder(event.getAuthor().getName() + " hugs");
                    } else {
                        embedMessage = new StringBuilder(Objects.requireNonNull(event.getMember()).getNickname() + " hugs");
                    }
                }
                for(int i=1; i<betterArgs.length; i++)
                {
                    embedMessage.append(" ");
                    embedMessage.append(betterArgs[i]);
                }
                embedMessage.append("! ").append(reactions[random.nextInt(reactions.length)]);
                EmbedBuilder hugEmbed = new EmbedBuilder();
                hugEmbed.setColor(hugGifs[gifNumber].getColor());
                hugEmbed.setImage(hugGifs[gifNumber].getGifURL());
                hugEmbed.setAuthor(embedMessage.toString(), null, event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessageEmbeds(hugEmbed.build()).queue();
                hugEmbed.clear();
            }
        }
    }
}
