package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Hug extends BaseCuddlyCommand {

    Permission[] hugCommandPermissions = {
            Permission.MESSAGE_SEND
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

    String action = "hugs";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "hug")) {
            if(checkPermissions(event, hugCommandPermissions)) {
                String cuddlyString = purifyFromMentionsAndMerge(args, event.getGuild(), true);
                if (cuddlyString.isEmpty()) {
                    event.getMessage().reply("You can't hug no one!").mentionRepliedUser(false).queue();
                    return;
                }
                sendCuddlyEmbedFromPrefix(event, cuddlyString, hugGifs, action, reactions);
            } else {
                event.getMessage().reply("For some reason, you can't hug anyone :(").mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("hug")) {
            if (checkSlashCommandPermissions(event, hugCommandPermissions)) {
                String[] cuddlyStringSplit = event.getOption("tohug", "", OptionMapping::getAsString).split("\\s+");
                String cuddlyString = purifyFromMentionsAndMerge(cuddlyStringSplit, event.getGuild(), false);
                String author = Objects.requireNonNull(event.getMember()).getEffectiveName();
                if (!cuddlyString.isEmpty()) {
                    event.replyEmbeds(createCuddlyEmbed(cuddlyString, event.getUser(), author, hugGifs, action, reactions)).queue();
                } else {
                    event.reply("You can't hug no one!").setEphemeral(true).queue();
                }
            } else {
                event.reply("For some reason, you can't hug anyone :(").setEphemeral(true).queue();
            }
        }
    }
}
