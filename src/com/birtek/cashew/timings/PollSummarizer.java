package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.commands.BaseCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class PollSummarizer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollSummarizer.class);

    private int id;
    private final String messageID, channelID, pollEndingTime;
    private static JDA jdaInstance;

    /**
     * A class that contains all information needed to summarize the poll as well as being the runnable which does the
     * summarization
     *
     * @param id             ID of the poll assigned by the database
     * @param channelID      ID of the channel in which the poll was created
     * @param messageID      ID of the message containing the poll
     * @param pollEndingTime timestamp in a String form interpretable by date formatters with the poll ending time and date
     */
    public PollSummarizer(int id, String channelID, String messageID, String pollEndingTime) {
        this.id = id;
        this.messageID = messageID;
        this.pollEndingTime = pollEndingTime;
        this.channelID = channelID;
    }

    private static MessageReaction getReactionByEmoji(List<MessageReaction> reactions, String emoji) {
        for (MessageReaction reaction : reactions) {
            if (reaction.getEmoji().equals(Emoji.fromUnicode(emoji))) return reaction;
        }
        return null;
    }

    private static String calculatePercentage(int votes, int totalVotes) {
        return Math.round((float) votes / (float) totalVotes * 10000) / 100.0 + "%";
    }

    private static int randomReadableRGB(boolean bright) {
        Random random = new Random();
        int rand = random.nextInt(256);
        if (bright) {
            return (rand < 150 ? 150 : (Math.min(rand, 220)));
        } else {
            return (rand < 75 ? 75 : (Math.min(rand, 125)));
        }
    }

    private static ArrayList<Color> createRandomGradient(int steps) {
        Color color1 = new Color(randomReadableRGB(false), randomReadableRGB(false), randomReadableRGB(false));
        Color color2 = new Color(randomReadableRGB(true), randomReadableRGB(true), randomReadableRGB(true));
        ArrayList<Color> interpolatedColors = new ArrayList<>();
        int gradientSteps = Math.max(steps, 5);
        for (int i = 1; i <= steps; i++) {
            int targetR = color1.getRed() + (color2.getRed() - color1.getRed()) / gradientSteps * (gradientSteps - i);
            int targetG = color1.getGreen() + (color2.getGreen() - color1.getGreen()) / gradientSteps * (gradientSteps - i);
            int targetB = color1.getBlue() + (color2.getBlue() - color1.getBlue()) / gradientSteps * (gradientSteps - i);
            interpolatedColors.add(new Color(targetR, targetG, targetB));
        }
        return interpolatedColors;
    }

    private HashMap<String, Color> createSliceColorMap(ArrayList<Pair<String, Integer>> slices, ArrayList<Color> colors) {
        HashMap<String, Color> colorMap = new HashMap<>();
        for (int i = 0; i < slices.size(); i++) {
            colorMap.put(slices.get(i).getLeft(), colors.get(i));
        }
        return colorMap;
    }

    private ArrayList<Pair<String, Integer>> generatePiechartCompatibleVotes(ArrayList<Pair<String, Integer>> votes) {
        ArrayList<Pair<String, Integer>> shortVotes = new ArrayList<>();
        for (Pair<String, Integer> vote : votes) {
            if (vote.getRight() == 0) continue;
            String optionName = vote.getLeft();
            if (optionName.length() > 15) optionName = optionName.substring(0, 12) + "...";
            shortVotes.add(Pair.of(optionName, vote.getRight()));
        }
        return shortVotes;
    }

    /**
     * Calculates the results of the poll and then edits the original embed placing results in it and stopping the count
     * of new votes
     */
    @Override
    public void run() {
        try {
            Message pollEmbedMessage = Objects.requireNonNull(jdaInstance.getTextChannelById(channelID)).retrieveMessageById(messageID).complete();
            MessageEmbed pollEmbed = pollEmbedMessage.getEmbeds().get(0);
            int totalVotes = 0;
            ArrayList<Pair<String, Integer>> votes = new ArrayList<>();
            List<MessageReaction> reactions = pollEmbedMessage.getReactions();
            for (MessageEmbed.Field field : pollEmbed.getFields()) {
                MessageReaction reaction = getReactionByEmoji(reactions, field.getName());
                if (reaction != null) {
                    votes.add(Pair.of(field.getValue(), reaction.getCount() - 1));
                    totalVotes += reaction.getCount() - 1;
                }
            }
            votes.sort((o1, o2) -> o2.getRight().compareTo(o1.getRight()));
            Instant pollEnd = Objects.requireNonNull(pollEmbed.getTimestamp()).toInstant();

            EmbedBuilder resultsEmbed = new EmbedBuilder();
            resultsEmbed.setTitle(pollEmbed.getTitle());
            if (totalVotes != 0) {
                resultsEmbed.setDescription("Final results");
                for (Pair<String, Integer> vote : votes) {
                    resultsEmbed.addField(vote.getRight() + " vote" + (vote.getRight() != 1 ? "s" : "") + ", " + calculatePercentage(vote.getRight(), totalVotes), vote.getLeft(), false);
                }
            } else {
                resultsEmbed.setDescription("No one voted!");
            }
            resultsEmbed.setFooter("Ended at");
            resultsEmbed.setTimestamp(pollEnd);

            ArrayList<Pair<String, Integer>> piechartVotes = generatePiechartCompatibleVotes(votes);
            HashMap<String, Color> sliceColors = createSliceColorMap(piechartVotes, createRandomGradient(piechartVotes.size()));
            InputStream pieChart = BaseCommand.generatePiechart(piechartVotes, sliceColors, pollEmbed.getTitle());
            if (pieChart == null) {
                LOGGER.warn("Failed to generate " + pollEmbed.getTitle() + " poll piechart!");
                pollEmbedMessage.editMessageEmbeds(resultsEmbed.build()).queue();
            } else {
                resultsEmbed.setImage("attachment://piechart.png");
                pollEmbedMessage.editMessageEmbeds(resultsEmbed.build()).setFiles(FileUpload.fromData(pieChart, "piechart.png")).queue();
            }
        } catch (NullPointerException ignored) {
        }
        if (!Cashew.pollManager.deletePoll(this.id)) LOGGER.warn("Failed to remove Poll " + this.id + " properly!");
    }

    public int getId() {
        return this.id;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getEndTime() {
        return this.pollEndingTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setJdaInstance(JDA jda) {
        jdaInstance = jda;
    }
}
