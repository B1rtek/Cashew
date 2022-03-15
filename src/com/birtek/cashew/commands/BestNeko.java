package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static com.birtek.cashew.messagereactions.ReactToMaple.getABestNekoEmbed;

public class BestNeko extends BaseCommand {

    Permission[] bestNekoCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "bestneko")) {
            if (checkPermissions(event, bestNekoCommandPermissions)) {
                event.getMessage().replyEmbeds(getABestNekoEmbed()).mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bestneko")) {
            if (checkSlashCommandPermissions(event, bestNekoCommandPermissions)) {
                event.replyEmbeds(getABestNekoEmbed()).queue();
            } else {
                event.reply("How did you even write this message?!").setEphemeral(true).queue();
            }
        }
    }
}