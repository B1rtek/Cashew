package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class PollSummarizer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollSummarizer.class);

    private final int id;
    private final String messageID, channelID, pollEndingTime;
    private static JDA jdaInstance;

    public PollSummarizer(int id, String messageID, String channelID, String pollEndingTime) {
        this.id = id;
        this.messageID = messageID;
        this.pollEndingTime = pollEndingTime;
        this.channelID = channelID;
    }

    private static MessageReaction getReactionByEmoji(List<MessageReaction> reactions, String emoji) {
        for(MessageReaction reaction: reactions) {
            if(reaction.getEmoji().equals(Emoji.fromUnicode(emoji))) return reaction;
        }
        return null;
    }

    private static String calculatePercentage(int votes, int totalVotes) {
        return Math.round((float)votes/(float)totalVotes * 10000)/100.0 + "%";
    }

    @Override
    public void run() {
        try {
            Message pollEmbedMessage = Objects.requireNonNull(jdaInstance.getTextChannelById(channelID)).retrieveMessageById(messageID).complete();
            MessageEmbed pollEmbed = pollEmbedMessage.getEmbeds().get(0);
            int totalVotes = 0;
            ArrayList<Pair<Integer, String>> votes = new ArrayList<>();
            List<MessageReaction> reactions = pollEmbedMessage.getReactions();
            for(MessageEmbed.Field field: pollEmbed.getFields()) {
                MessageReaction reaction = getReactionByEmoji(reactions, field.getName());
                if(reaction != null) {
                    votes.add(Pair.of(reaction.getCount()-1, field.getValue()));
                    totalVotes += reaction.getCount()-1;
                }
            }
            votes.sort((o1, o2) -> o2.getLeft().compareTo(o1.getLeft()));
            Instant pollEnd = Objects.requireNonNull(pollEmbed.getTimestamp()).toInstant();

            EmbedBuilder resultsEmbed = new EmbedBuilder();
            resultsEmbed.setTitle("Poll results");
            resultsEmbed.setDescription(pollEmbed.getDescription());
            for(Pair<Integer, String> vote: votes) {
                resultsEmbed.addField(vote.getLeft() + " votes, " + calculatePercentage(vote.getLeft(), totalVotes), vote.getRight(), false);
            }
            resultsEmbed.setFooter("Ended at");
            resultsEmbed.setTimestamp(pollEnd);

            pollEmbedMessage.editMessageEmbeds(resultsEmbed.build()).queue();
        } catch (NullPointerException ignored) {} finally {
            Cashew.pollManager.deletePoll(this.id);
        }
    }
}
