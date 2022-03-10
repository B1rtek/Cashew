package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.messagereactions.ReactToMaple;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class BestNeko extends BaseCommand {

    Permission[] bestNekoCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "bestneko")) {
            if(checkPermissions(event, bestNekoCommandPermissions)) {
                event.getChannel().sendMessage("Maple Minaduki <3").queue();
                ReactToMaple reactToMaple = new ReactToMaple();
                reactToMaple.sendTheBestNekoGif(event);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("bestneko")) {
            if(checkSlashCommandPermissions(event, bestNekoCommandPermissions)) {
                event.reply("Maple Minaduki <3").queue();
                ReactToMaple reactToMaple = new ReactToMaple();
                event.getChannel().sendMessage(reactToMaple.getABestNekoGif()).queue();
                event.getChannel().sendMessage("Best neko!").complete();
            }
        }
    }
}