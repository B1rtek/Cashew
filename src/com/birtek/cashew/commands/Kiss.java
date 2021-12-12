package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class Kiss extends BaseCommand {

    Permission[] kissCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    EmbedGif[] kissGifs = {
            new EmbedGif("https://c.tenor.com/3Y9B4si5mR4AAAAC/maple-cinnamon.gif", 0xF78ECB),
            new EmbedGif("https://media1.tenor.com/images/b1726d7c03317421fb504faa2deb674f/tenor.gif", 0xF6EDA7)
    };

    String[] reactions = {
            "UwU", "OwO", ":3", ";3", "Nyaaa!", "<3", "Yayy!", "Cute~", "Adorable~", "Hot", ">w<"
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "kiss")) {
            if(checkPermissions(event, kissCommandPermissions)) {
                Random random = new Random();
                int gifNumber = random.nextInt(kissGifs.length);
                String[] betterArgs = event.getMessage().getContentDisplay().replaceAll("@", "").split("\\s+");
                StringBuilder embedMessage;
                if(event.isWebhookMessage()) {
                    embedMessage = new StringBuilder(event.getAuthor().getName() + " kisses");
                } else {
                    if(Objects.requireNonNull(event.getMember()).getNickname()==null) {
                        embedMessage = new StringBuilder(event.getAuthor().getName() + " kisses");
                    } else {
                        embedMessage = new StringBuilder(Objects.requireNonNull(event.getMember()).getNickname() + " kisses");
                    }
                }
                for(int i=1; i<betterArgs.length; i++)
                {
                    embedMessage.append(" ");
                    embedMessage.append(betterArgs[i]);
                }
                embedMessage.append("! ").append(reactions[random.nextInt(reactions.length)]);
                EmbedBuilder kissEmbed = new EmbedBuilder();
                kissEmbed.setColor(kissGifs[gifNumber].getColor());
                kissEmbed.setImage(kissGifs[gifNumber].getGifURL());
                kissEmbed.setAuthor(embedMessage.toString(), null, event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessageEmbeds(kissEmbed.build()).queue();
                kissEmbed.clear();
            }
        }
    }
}
