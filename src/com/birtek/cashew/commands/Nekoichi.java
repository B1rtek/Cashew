package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Nekoichi extends BaseCommand {

    String[] nekoichi = {
            "ずっと大切だよ いつだって隣にいるよ",
            "同じ時間の中で そっと寄り添っていたい"
    };

    private void singNekoichi(MessageChannel destinationChannel) {
        for(String line:nekoichi) {
            destinationChannel.sendMessage(line).queue();
            try {
                Thread.sleep(8 * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "nekoichi")) {
            if(cantBeExecutedPrefix(event, "nekoichi", false)) {
                event.getMessage().reply("This command is turned off in this channel").mentionRepliedUser(false).queue();
                return;
            }
            singNekoichi(event.getChannel());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("nekoichi")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            event.reply("Nekoichi by Duca").queue();
            singNekoichi(event.getChannel());
        }
    }
}