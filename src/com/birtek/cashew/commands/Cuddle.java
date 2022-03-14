package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class Cuddle extends BaseCuddlyCommand {

    Permission[] cuddleCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    EmbedGif[] cuddleGifs = {
            new EmbedGif("https://media1.tenor.com/images/b9a38b215d3fc3ba3439f681fbf24bee/tenor.gif", 0xE5F5FB),
            new EmbedGif("https://media1.tenor.com/images/22b7f1a86b6a882dfc854c959007ea0f/tenor.gif", 0xF8E9F1),
            new EmbedGif("https://media1.tenor.com/images/5b33195c19399b897307dd030066babe/tenor.gif", 0x33304E),
            new EmbedGif("https://media1.tenor.com/images/00e36bd813f89de78db14ec7999440c0/tenor.gif", 0xFBEADB),
            new EmbedGif("https://media1.tenor.com/images/a35dcf44a727518cf4c148587f71bbd8/tenor.gif", 0x401720),
            new EmbedGif("https://media1.tenor.com/images/8223cf0223023dce3ac7f28c2885874b/tenor.gif", 0xF9E7C1),
            new EmbedGif("https://media1.tenor.com/images/f26091a329fe75c5995fdf1e6123834a/tenor.gif", 0x2C569B),
            new EmbedGif("https://media1.tenor.com/images/e590d20645d330dabb99e14121822470/tenor.gif", 0xCF5B5F),
            new EmbedGif("https://media1.tenor.com/images/d240ea9d07e513e696892a764a8a8acf/tenor.gif", 0xC5E1D4),
            new EmbedGif("https://media1.tenor.com/images/5aa0da336b4d96c4ba836ea0d8cd4984/tenor.gif", 0x666DA0)
    };

    String action = "cuddles";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "cuddle")) {
            if (checkPermissions(event, cuddleCommandPermissions)) {
                String cuddlyString = purifyFromMentionsAndMerge(args, event.getGuild(), true);
                if (cuddlyString.isEmpty()) {
                    event.getMessage().reply("You can't cuddle no one!").mentionRepliedUser(false).queue();
                    return;
                }
                sendCuddlyEmbedFromPrefix(event, cuddlyString, cuddleGifs, action);
            } else {
                event.getMessage().reply("For some reason, you can't cuddle :(").mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("cuddle")) {
            if (checkSlashCommandPermissions(event, cuddleCommandPermissions)) {
                String[] cuddlyStringSplit = event.getOption("tocuddle", "", OptionMapping::getAsString).split("\\s+");
                String cuddlyString = purifyFromMentionsAndMerge(cuddlyStringSplit, event.getGuild(), false);
                String author = Objects.requireNonNull(event.getMember()).getEffectiveName();
                if (!cuddlyString.isEmpty()) {
                    event.replyEmbeds(createCuddlyEmbed(cuddlyString, event.getUser(), author, cuddleGifs, action)).queue();
                } else {
                    event.reply("You can't cuddle no one!").setEphemeral(true).queue();
                }
            } else {
                event.reply("For some reason, you can't cuddle :(").setEphemeral(true).queue();
            }
        }
    }
}
