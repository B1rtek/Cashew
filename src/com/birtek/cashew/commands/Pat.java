package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Pat extends BaseCuddlyCommand {

    Permission[] patCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    EmbedGif[] patGifs = {
            new EmbedGif("https://media1.tenor.com/images/eea25cfcb9ff84c061b68ef6a36388f6/tenor.gif", 0xFAF4EE),
            new EmbedGif("https://media1.tenor.com/images/ced24b9e4cef7f6e107d4ab6e7b4527d/tenor.gif", 0xFCE393),
            new EmbedGif("https://media1.tenor.com/images/483d3111c21c0506a67525fbbc9b10ce/tenor.gif", 0xC3ECE1),
            new EmbedGif("https://c.tenor.com/taRnvkLdm-oAAAAC/cacao-azuki.gif", 0xFCEEE1),
            new EmbedGif("https://media1.tenor.com/images/1da3d002a13a5a6eb0937eb50f80f488/tenor.gif", 0xE0F3E4),
            new EmbedGif("https://media1.tenor.com/images/7a3becca07d971f7c381cadafbad689c/tenor.gif", 0xB99483),
            new EmbedGif("https://media1.tenor.com/images/832d0c95d5ef9644525bced0fdb0cd29/tenor.gif", 0xFCF3EA),
            new EmbedGif("https://media1.tenor.com/images/89f7372b743c65b61c9f181f203d37bd/tenor.gif", 0x9F7C82),
            new EmbedGif("https://media1.tenor.com/images/92b03839ae1fca09b163b76244bd6b60/tenor.gif", 0x832F70),
            new EmbedGif("https://media1.tenor.com/images/6627410e5f85cda4bd4b5ec129895518/tenor.gif", 0xECDA96),
            new EmbedGif("https://media1.tenor.com/images/ec58606b91025fd6fc674db2866de980/tenor.gif", 0x744F45),
            new EmbedGif("https://media1.tenor.com/images/9f926bf32120bcf3fbebadfecd9a0619/tenor.gif", 0xC9D0DF)
    };

    String[] reactions = {
            "UwU", "OwO", ":3", ";3", "Nyaaa!", "<3", "Yayy!", "Cute~", "Adorable~"
    };

    String action = "pats";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "pat")) {
            if (checkPermissions(event, patCommandPermissions)) {
                String cuddlyString = purifyFromMentionsAndMerge(args, event.getGuild(), true);
                if (cuddlyString.isEmpty()) {
                    event.getMessage().reply("You can't pat no one!").mentionRepliedUser(false).queue();
                    return;
                }
                sendCuddlyEmbedFromPrefix(event, cuddlyString, patGifs, action, reactions);
            } else {
                event.getMessage().reply("For some reason, you can't pat :(").mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("pat")) {
            if (checkSlashCommandPermissions(event, patCommandPermissions)) {
                String[] cuddlyStringSplit = event.getOption("topat", "", OptionMapping::getAsString).split("\\s+");
                String cuddlyString = purifyFromMentionsAndMerge(cuddlyStringSplit, event.getGuild(), false);
                String author = Objects.requireNonNull(event.getMember()).getEffectiveName();
                if (!cuddlyString.isEmpty()) {
                    event.replyEmbeds(createCuddlyEmbed(cuddlyString, event.getUser(), author, patGifs, action, reactions)).queue();
                } else {
                    event.reply("You can't pat no one!").setEphemeral(true).queue();
                }
            } else {
                event.reply("For some reason, you can't pat :(").setEphemeral(true).queue();
            }
        }
    }
}
