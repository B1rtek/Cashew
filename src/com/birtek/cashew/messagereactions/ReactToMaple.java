package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Cashew;
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

    String[] mapleGifs = {
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
        if(!event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID) && checkIfNotBot(event) && checkActivitySettings(event, 2)) {
            for(String mention: mapleMentions) {
                if(message.contains(mention)) {
                    if(mention.equals("best catgirl") || mention.equals("best neko")) {
                        if(!message.contains("dumbest")) {
                            sendTheBestNekoGif(event);
                        }
                    } else {
                        sendTheBestNekoGif(event);
                    }
                    break;
                }
            }
            for(String mention: mayPullMentions) {
                if(message.contains(mention)) {
                    sendTheMayPullGif(event);
                    break;
                }
            }
        }
    }

    public String getABestNekoGif() {
        Random rand = new Random();
        int index = rand.nextInt(mapleGifs.length);
        return mapleGifs[index];
    }

    public void sendTheBestNekoGif(MessageReceivedEvent event) {
        event.getMessage().reply(getABestNekoGif()).mentionRepliedUser(false).queue();
        event.getChannel().sendMessage("Best neko!").complete();
    }

    private void sendTheMayPullGif(MessageReceivedEvent event) {
        event.getMessage().reply(mayPullGif).mentionRepliedUser(false).queue();
        Random rand = new Random();
        int index = rand.nextInt(mayPullQuotes.length);
        event.getChannel().sendMessage(mayPullQuotes[index]).complete();
    }
}