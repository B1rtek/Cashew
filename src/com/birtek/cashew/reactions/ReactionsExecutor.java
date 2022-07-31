package com.birtek.cashew.reactions;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.Reaction;
import com.birtek.cashew.database.ReactionsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

/**
 * Spots the reactions contents in the messages and if the reaction is enabled in the channel, will reply to them
 */
public class ReactionsExecutor extends ListenerAdapter {

    static String[] mapleGifs = {
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
    private final ArrayList<Reaction> reactions;
    String mayPullGif = "https://cdn.discordapp.com/attachments/852811110158827533/858365564077735977/MayPull.gif";

    String[] mayPullQuotes = {
            "Hey! Pay attention, Shigure! She's about to lose all her fur!",
            "Huh?! What happened?",
            "The vacuum sucking... combined with you grabbing... is creating a really pleasant sensation...",
            "It's almost more than I can handle... It's making me sooooo weeeeeet!!!",
            "Oh come on Cinnamon, seriously?! This is no time to be getting wet!"
    };

    public ReactionsExecutor() {
        ReactionsDatabase database = ReactionsDatabase.getInstance();
        reactions = database.getAllReactions();
    }

    /**
     * Matches against all reactions and if a match is found, reacts if the reaction is on in the channel
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (!message.isFromGuild() || message.getAuthor().isBot()) return;
        String content = message.getContentDisplay();
        for (Reaction reaction : reactions) {
            if (isTriggered(reaction, content) && isActive(message, reaction)) {
                switch (reaction.actionID()) {
                    case 1 -> // reply
                            message.reply(reaction.actionContent()).mentionRepliedUser(false).queue();
                    case 2 -> { // reply with 20% chance
                        Random random = new Random();
                        if (random.nextInt(5) == 0) {
                            message.reply(reaction.actionContent()).mentionRepliedUser(false).queue();
                        }
                    }
                    case 3 -> // reaction
                            message.addReaction(Emoji.fromUnicode(reaction.actionContent())).queue();
                    case 4 -> // reply with mention
                            message.reply(reaction.actionContent()).mentionRepliedUser(true).queue();
                    case 5 -> // best neko embed (the original one)
                            message.replyEmbeds(getABestNekoEmbed()).mentionRepliedUser(false).queue();
                    case 6 -> // maypull embed
                            message.replyEmbeds(getAMaypullEmbed()).mentionRepliedUser(false).queue();
                }
            }
        }
    }

    /**
     * Matches message content against all reaction's triggers and tells if there is a match
     *
     * @param reaction {@link Reaction Reaction} to check whether any of its triggers match the message content
     * @param content  content of the message
     * @return true if the message triggers the reaction, false otherwise
     */
    private boolean isTriggered(Reaction reaction, String content) {
        for (String trigger : reaction.patterns()) {
            if (content.toLowerCase().contains(trigger)) return true;
        }
        return false;
    }

    /**
     * Checks in the {@link com.birtek.cashew.timings.ReactionsSettingsManager ReactionsSettingsManager} whether the
     * reactions is turned on in the channel
     *
     * @param message  {@link Message Message} from which IDs of the server and channel will be retrieved
     * @param reaction {@link Reaction Reaction} which settings will be checked and returned
     * @return true if the reaction is active in the channel, false otherwise
     */
    private boolean isActive(Message message, Reaction reaction) {
        String serverID = message.getGuild().getId();
        String channelID = message.getChannel().getId();
        return Cashew.reactionsSettingsManager.getActivitySettings(serverID, channelID, reaction.id());
    }

    /**
     * Creates a Bestneko embed of the old type. Copied from unused {@link ReactToMaple ReactToMaple}
     *
     * @return a {@link MessageEmbed MessageEmbed} with a GIF of Maple Minaduki in it, who is described as the best neko
     * which is true, Maple Minaduki really is the best neko in the whole Nekopara series and everyone who thinks that
     * she's not is just wrong.
     */
    private MessageEmbed getABestNekoEmbed() {
        Random random = new Random();
        int choice = random.nextInt(mapleGifs.length);
        EmbedBuilder bestNekoEmbed = new EmbedBuilder();
        bestNekoEmbed.setAuthor("\uD83C\uDF41 Maple Minaduki <3 \uD83C\uDF41");
        bestNekoEmbed.setImage(mapleGifs[choice]);
        bestNekoEmbed.setFooter("Best neko!");
        return bestNekoEmbed.build();
    }

    /**
     * Creates a Maypull embed. Copied from unused {@link ReactToMaple ReactToMaple}
     *
     * @return a {@link MessageEmbed MessageEmbed} with a GIF from "the funny scene from nekopara anime" with a quote
     * from that scene
     */
    private MessageEmbed getAMaypullEmbed() {
        Random random = new Random();
        EmbedBuilder maypullEmbed = new EmbedBuilder();
        maypullEmbed.setAuthor("Maypull");
        maypullEmbed.setImage(mayPullGif);
        maypullEmbed.setFooter(mayPullQuotes[random.nextInt(mayPullQuotes.length)]);
        return maypullEmbed.build();
    }
}
