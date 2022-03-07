package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Random;

public class OwosEtc extends BaseReaction {

    String maplegasm = "https://cdn.discordapp.com/attachments/852812258677358592/857877021756620810/856925111809474610.png";
    String whentheimposterissus = "https://cdn.discordapp.com/attachments/857711843282649158/858835663179481118/maxresdefault.png";
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
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        String rawMessage = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
        //System.out.println(message);
        if (!event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID) && checkIfNotBot(event)) {
            String reactionMessage = "";
            if (message.contains("owo") && rawMessage.contains("owo") && checkActivitySettings(event, 1)) {
                reactionMessage += "OwO ";
                //event.getMessage().reply("OwO").mentionRepliedUser(false).queue();
            }
            if (message.contains("uwu") && rawMessage.contains("uwu") && checkActivitySettings(event, 1)) {
                reactionMessage += "UwU ";
                //event.getMessage().reply("UwU").mentionRepliedUser(false).queue();
            }
            if (message.contains("69") && rawMessage.contains("69") && checkActivitySettings(event, 1)) {
                reactionMessage += "nice ";
                //event.getMessage().reply("nice").mentionRepliedUser(false).queue();
            }
            if ((message.contains("amogus") && rawMessage.contains("amogus")) || (message.contains("a m o g u s") && rawMessage.contains("a m o g u s")) && checkActivitySettings(event, 1)) {
                reactionMessage += "ඞ sus ඞ ";
                //event.getMessage().reply("ඞ sus ඞ").mentionRepliedUser(false).queue();
            }
            if (message.contains("sus") && rawMessage.contains("sus") && checkActivitySettings(event, 1)) {
                reactionMessage += "ඞ amogus ඞ ";
                //event.getMessage().reply("ඞ amogus ඞ").mentionRepliedUser(false).queue();
            }
            if (message.contains("ඞ") && rawMessage.contains("ඞ") && checkActivitySettings(event, 1)) {
                reactionMessage += "ඞ amogus sus ඞ ";
                //event.getMessage().reply("ඞ amogus sus ඞ").mentionRepliedUser(false).queue();
            }
            if (message.contains("vent") && rawMessage.contains("vent") && checkActivitySettings(event, 1)) {
                reactionMessage += "ඞ he vented susss!!! ඞ ";
                //event.getMessage().reply("ඞ amogus ඞ").mentionRepliedUser(false).queue();
            }
            if (reactionMessage.length() > 0) {
                event.getMessage().reply(reactionMessage).mentionRepliedUser(false).queue();
            }
            if (message.contains("( ͡° ͜ʖ ͡°)") && rawMessage.contains("( ͡° ͜ʖ ͡°)") && checkActivitySettings(event, 1)) {
                if (event.getGuild().getId().equals(Cashew.NEKOPARA_EMOTES_UWU_SERVER_ID)) {
                    event.getMessage().reply(maplegasm).mentionRepliedUser(false).queue();
                }
                event.getMessage().reply("( ͡° ͜ʖ ͡°)( ͡° ͜ʖ ͡°)( ͡° ͜ʖ ͡°)").mentionRepliedUser(false).queue();
            }
            if ((message.contains("impostor") || message.contains("imposter")) && (rawMessage.contains("impostor") || rawMessage.contains("imposter")) && checkActivitySettings(event, 1)) {
                event.getMessage().reply(whentheimposterissus).mentionRepliedUser(false).queue();
            }
            if (message.contains("ęśąćż") && rawMessage.contains("ęśąćż") && checkActivitySettings(event, 1)) {
                event.getMessage().reply("https://cdn.discordapp.com/attachments/857711843282649158/921471143578320956/800x0x1.png").mentionRepliedUser(false).queue();
            }
            if (message.contains("worst neko") && rawMessage.contains("worst neko") && checkActivitySettings(event, 1)) {
                Random random = new Random();
                if (random.nextInt(5) == 0) {
                    event.getMessage().reply("https://cdn.discordapp.com/attachments/857711843282649158/862677751444799530/unknown.png").mentionRepliedUser(false).queue();
                }
            }
            if(message.contains("nya") && rawMessage.contains("nya") && checkActivitySettings(event, 1)) {
                event.getMessage().reply("nyaaa :3").mentionRepliedUser(false).queue();
            }

            // $kromer command
            if (message.startsWith(Cashew.COMMAND_PREFIX + "kromer") && rawMessage.startsWith(Cashew.COMMAND_PREFIX + "kromer") && checkActivitySettings(event, 0)) {
                if (!(message.length() > 6 + Cashew.COMMAND_PREFIX.length() && message.charAt(6 + Cashew.COMMAND_PREFIX.length()) != ' ')) {
                    Random random = new Random();
                    event.getMessage().reply(kromerGifs[random.nextInt(kromerGifs.length)]).mentionRepliedUser(false).queue();
                }
            }

            if (message.contains("bot") && rawMessage.contains("bot") && checkActivitySettings(event, 2)) {
                event.getMessage().reply(event.getAuthor().getAsMention() + " ти бот").mentionRepliedUser(false).queue();
            }
        }
    }
}