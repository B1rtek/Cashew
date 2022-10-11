package com.birtek.cashew.reactions;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.TriviaQuestion;
import com.birtek.cashew.database.TriviaQuestionsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Locale;

public class TriviaQuestionsListener extends ListenerAdapter {

    /**
     * Generates an embed showing user's current progress in the difficulty category
     *
     * @param amountCompleted amount of completed questions of the difficulty
     * @param difficulty      difficulty level of the previously responded to question
     * @return a {@link MessageEmbed MessageEmbed} with a progressbar showing current progress
     */
    private MessageEmbed generateProgressEmbed(int amountCompleted, int difficulty) {
        TriviaQuestionsDatabase database = TriviaQuestionsDatabase.getInstance();
        int totalQuestions = database.getQuestionsCountByType().get(difficulty);
        int squaresToShow = 20 * amountCompleted / totalQuestions;
        DecimalFormat df = new DecimalFormat("##.##%");
        double percent = ((double) amountCompleted / (double) totalQuestions);
        String formattedPercent = df.format(percent);
        String progressbarSquare = switch (difficulty) {
            case 1 -> "\uD83D\uDFE9";
            case 2 -> "\uD83D\uDFE8";
            case 3 -> "\uD83D\uDFE5";
            default -> "⬛";
        };
        String progressbar = progressbarSquare.repeat(squaresToShow) + "⬛".repeat(20 - squaresToShow) + "  " + formattedPercent + " (" + amountCompleted + " out of " + totalQuestions + ")";
        EmbedBuilder progressEmbed = new EmbedBuilder();
        progressEmbed.setTitle("Your " + TriviaQuestion.getDifficultyName(difficulty) + " questions progress:");
        progressEmbed.setDescription(progressbar);
        if(amountCompleted == totalQuestions) {
            progressEmbed.setFooter("You have completed all " + TriviaQuestion.getDifficultyName(difficulty) + " questions!");
        }
        return progressEmbed.build();
    }

    /**
     * Generates a simple embed saying that the questions wasn't answered correctly
     * @param message message saying why the attempt has failed
     * @return a {@link MessageEmbed MessageEmbed} with the failure message formatted nicely
     */
    public static MessageEmbed generateFailEmbed(String message) {
        EmbedBuilder failEmbed = new EmbedBuilder();
        failEmbed.setTitle("❌ " + message);
        return failEmbed.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (Cashew.triviaQuestionsManager.isBeingPlayedIn(event.getChannel().getId())) {
            TriviaQuestion usersQuestion = Cashew.triviaQuestionsManager.getUsersQuestion(event.getAuthor().getId());
            if (usersQuestion == null) return;
            int result = Cashew.triviaQuestionsManager.checkAnswer(event.getAuthor().getId(), event.getMessage().getContentRaw().toLowerCase(Locale.ROOT));
            if(result == -2) {
                event.getMessage().reply("Something went wrong while saving your progress").mentionRepliedUser(false).queue();
            }
            if (result >= 1) {
                event.getMessage().reply("Correct!").addEmbeds(generateProgressEmbed(result, usersQuestion.difficulty())).mentionRepliedUser(false).queue();
            } else {
                if (usersQuestion.isResponseLimited()) {
                    event.getMessage().addReaction(Emoji.fromUnicode("❌")).queue();
                }
                if (result == -1) {
                    event.getMessage().replyEmbeds(generateFailEmbed("You ran out of attempts to answer this question!")).mentionRepliedUser(true).queue();
                }
            }
        }
    }
}
