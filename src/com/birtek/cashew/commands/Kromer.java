package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Kromer extends BaseCommand {

    String[] kromerGifs = {
            "https://tenor.com/view/spamton-spamton-neo-deltarune-deltarune-spamton-amogus-gif-23353265",
            "https://tenor.com/view/kromer-deltarune-spamton-deltarune-chapter2-gif-23177285",
            "https://tenor.com/view/deltarune-spamton-kromer-0001kromer-money-gif-23199506",
            "https://tenor.com/view/spamton-kromer-deltarune-big-shot-1997-gif-23301374",
            "https://tenor.com/view/kromer-big-shot-deltarune-spamton-gif-23245662",
            "https://tenor.com/view/spamton-0kromer-death-kromer-gif-23230738",
            "https://tenor.com/view/kromer-spamton-gif-23227110",
            "https://tenor.com/view/spamton-deltarune-punching-punching-wall-punching-the-wall-gif-23393913",
            "https://tenor.com/view/spamton-deltarune-undertale-spamton-neo-gif-23320706",
            "https://cdn.discordapp.com/attachments/903333009435029586/903716167326236732/Spamton_Unintelligible720P_HD.mp4"
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "kromer")) {
            Random random = new Random();
            event.getMessage().reply(kromerGifs[random.nextInt(kromerGifs.length)]).mentionRepliedUser(false).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("kromer")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            Random random = new Random();
            event.reply(kromerGifs[random.nextInt(kromerGifs.length)]).queue();
        }
    }
}
