package com.birtek.cashew.reactions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Random;

public class ReactToMaple extends BaseReaction {

    String[] mapleMentions = {
            "maple",
            "maple minaduki",
            "may-chan",
            "meipuru",
            "mei-chan",
            "best neko",
            "best catgirl",
            "may chan"
    };
    
    String[] mayPullMentions = {
            "pullmay",
            "pull-may",
            "pull may",
            "maypull",
            "may-pull",
            "may pull",
            "the funny thing from episode 8",
            "maple pulling",
            "pulling maple"
    };

    static String[] mapleGifsSources = {
            "https://tenor.com/view/maple-maple-minaduki-nekopara-nekopara-vol4-sayori-gif-21935718",
            "https://tenor.com/view/maple-nekopara-pretty-gif-16356346",
            "https://tenor.com/view/nekopara-maple-music-game-visual-gif-7835668",
            "https://tenor.com/view/nekopara-maple-play-tail-wiggle-gif-16356350",
            "https://tenor.com/view/nekopara-maple-phone-talk-cute-gif-17483987",
            "https://tenor.com/view/nekopara-catgirl-maple-cute-talk-gif-17364104",
            "https://tenor.com/view/nekopara-maple-catgirl-anime-cute-gif-16504875",
            "https://tenor.com/view/nekopara-maple-catgirls-anime-sleeping-gif-16717174",
            "https://tenor.com/view/nekopara-maple-catgirl-video-game-series-sleeping-gif-17335787",
            "https://tenor.com/view/nekopara-maple-maple-minaduki-nekopara-vol4-sayori-gif-21673507",
            "https://tenor.com/view/nekopara-maple-catgirl-gif-18454847",
            "https://tenor.com/view/nekopara-maple-catgirl-gif-17989418",
            "https://tenor.com/view/nekopara-maple-catgirl-anime-gif-17363982",
            "https://tenor.com/view/nekopara-maple-catgirl-anime-gif-16972768",
            "https://tenor.com/view/maple-nekopara-gif-18279416",
            "https://tenor.com/view/nekopara-maple-catgirl-write-gif-16356343",
            "https://tenor.com/view/nekopara-maple-catgirl-bath-cute-gif-16356329",
            "https://tenor.com/view/nekopara-maple-cat-girl-wet-anime-gif-17343896",
            "https://tenor.com/view/nekopara-maple-catgirl-happy-smile-gif-16504895",
            "https://tenor.com/view/nekopara-maple-catgirl-anime-cute-gif-16504885",
            "https://tenor.com/view/nekopara-maple-catgirl-happy-smile-gif-16504873",
            "https://tenor.com/view/nekopara-maple-drink-pour-sip-gif-16373297",
            "https://tenor.com/view/nekopara-maple-cat-girl-anime-blush-gif-16601477",
            "https://tenor.com/view/nekopara-catgirl-maple-yes-excited-gif-16437042",
            "https://tenor.com/view/nekopara-catgirl-mic-talking-anime-gif-16627309",
            "https://tenor.com/view/nekopara-maple-catgirl-gif-18769185",
            "https://tenor.com/view/nekopara-maple-catgirl-drink-water-gif-16504890"
    };

    static String[] mapleGifsLiterally = {
            "https://c.tenor.com/ttFE6USydOgAAAAC/maple-maple-minaduki.gif",
            "https://c.tenor.com/fwbCIgfPTNEAAAAC/maple-nekopara.gif",
            "https://c.tenor.com/yeVevx38rxkAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/0iXqBDXCxP0AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/goLDw66TwboAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/9LKbpLfhNV0AAAAC/nekopara-catgirl.gif",
            "https://c.tenor.com/k8DbaTxiWnQAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/lcVK2ktvSv8AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/PmnYujvFjV0AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/Towqnie-lhcAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/b-Erfr4uoI0AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/864A3pmamfcAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/HupG69cVmNMAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/-lcA81boM9oAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/23ZvFvwfTesAAAAC/maple-nekopara.gif",
            "https://c.tenor.com/eZiFQbzHbdMAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/3zSMEahlfikAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/2xdztO3lMXYAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/uEy6gVfDb4gAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/YrAV8AaOUp0AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/KyTa0nfu388AAAAC/nekopara-maple.gif",
            "https://c.tenor.com/zKYj5pFZFpYAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/W2vnAsiF5LIAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/SYXLI5jnhCMAAAAC/nekopara-catgirl.gif",
            "https://c.tenor.com/4VJudrOesn4AAAAC/nekopara-catgirl.gif",
            "https://c.tenor.com/f-JscnqR_7MAAAAC/nekopara-maple.gif",
            "https://c.tenor.com/H3dK2_w3vsUAAAAC/nekopara-maple.gif"
    };

    String mayPullGif = "https://cdn.discordapp.com/attachments/852811110158827533/858365564077735977/MayPull.gif";

    String[] mayPullQuotes = {
            "Hey! Pay attention, Shigure! She's about to lose all her fur!",
            "Huh?! What happened?",
            "The vacuum sucking... combined with you grabbing... is creating a really pleasant sensation...",
            "It's almost more than I can handle... It's making me sooooo weeeeeet!!!",
            "Oh come on Cinnamon, seriously?! This is no time to be getting wet!"
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        if(!event.getAuthor().getId().equals(OwosEtc.CASHEW_USER_ID) && checkIfNotBot(event) && checkActivitySettings(event, 2)) {
            for(String mention: mapleMentions) {
                if(message.contains(mention)) {
                    if(mention.equals("best catgirl") || mention.equals("best neko")) {
                        if(!message.contains("dumbest")) {
                            event.getMessage().replyEmbeds(getABestNekoEmbed()).mentionRepliedUser(false).queue();
                        }
                    } else {
                        event.getMessage().replyEmbeds(getABestNekoEmbed()).mentionRepliedUser(false).queue();
                    }
                    break;
                }
            }
            for(String mention: mayPullMentions) {
                if(message.contains(mention)) {
                    event.getMessage().replyEmbeds(getTheMayPullGifEmbed()).mentionRepliedUser(false).queue();
                    break;
                }
            }
        }
    }

    public static MessageEmbed getABestNekoEmbed() {
        Random random = new Random();
        int choice = random.nextInt(mapleGifsSources.length);
        EmbedBuilder bestNekoEmbed = new EmbedBuilder();
        bestNekoEmbed.setAuthor("\uD83C\uDF41 Maple Minaduki <3 \uD83C\uDF41", mapleGifsSources[choice]);
        bestNekoEmbed.setImage(mapleGifsLiterally[choice]);
        bestNekoEmbed.setFooter("Best neko!");
        return bestNekoEmbed.build();
    }

    private MessageEmbed getTheMayPullGifEmbed() {
        Random random = new Random();
        EmbedBuilder maypullEmbed = new EmbedBuilder();
        maypullEmbed.setAuthor("Maypull");
        maypullEmbed.setImage(mayPullGif);
        maypullEmbed.setFooter(mayPullQuotes[random.nextInt(mayPullQuotes.length)]);
        return maypullEmbed.build();
    }
}