package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.TriviaQuestion;
import com.birtek.cashew.database.TriviaQuestionsDatabase;
import com.birtek.cashew.database.TriviaStats;
import com.birtek.cashew.database.TriviaStatsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Trivia extends BaseCommand {

    ArrayList<String> difficulties = new ArrayList<>() {
        {
            add("easy");
            add("medium");
            add("hard");
        }
    };

    /**
     * Generates a nice embed with a question and a countdown timer representing time left to respond to the question
     *
     * @param question {@link TriviaQuestion question} to present in an embed
     * @return a {@link MessageEmbed MessageEmbed} with the question in it
     */
    private MessageEmbed generateQuestionEmbed(TriviaQuestion question) {
        EmbedBuilder questionEmbed = new EmbedBuilder();
        questionEmbed.setTitle(question.question());
        questionEmbed.setImage(question.imageURL());
        questionEmbed.setDescription("Ends " + TimeFormat.RELATIVE.format(Instant.now().plusSeconds(15)));
        if(question.isResponseLimited()) {
            questionEmbed.setFooter("You have " + question.getResponsesLeft() + " attempt" + (question.getResponsesLeft()==1?"":"s") + " to answer this question");
        }
        int color = switch (question.difficulty()) {
            case 1 -> 0x04c907;
            case 2 -> 0xfff203;
            case 3 -> 0xf22800;
            default -> 0x009df2;
        };
        questionEmbed.setColor(color);
        return questionEmbed.build();
    }

    /**
     * Generates an embed with user's TriviaStats
     * @param userStats {@link TriviaStats TriviaStats} of the requested user
     * @param user {@link User user} whose stats will be displayed
     * @return a {@link MessageEmbed MessageEmbed} with user's trivia stats like amount of completed questions per type
     * and percentage of correctly answered questions
     */
    private MessageEmbed generateStatsEmbed(TriviaStats userStats, User user) {
        TriviaQuestionsDatabase database = TriviaQuestionsDatabase.getInstance();
        HashMap<Integer, Integer> distribution = database.getQuestionsCountByType();
        DecimalFormat df = new DecimalFormat("##.##%");
        double percent = ((double) userStats.gamesWon() / (double) userStats.gamesPlayed());
        String winPercentage = df.format(percent);
        EmbedBuilder statsEmbed = new EmbedBuilder();
        statsEmbed.setTitle(user.getName() + "'s Trivia stats");
        statsEmbed.setThumbnail(user.getEffectiveAvatarUrl());
        for(int i=1; i<=3; i++) {
            String difficultyName = TriviaQuestion.getDifficultyName(i);
            statsEmbed.addField(difficultyName.substring(0, 1).toUpperCase(Locale.ROOT) + difficultyName.substring(1) + " questions completed", userStats.getProgressByDifficulty(i) + "/" + distribution.get(i), true);
        }
        statsEmbed.addField("Total questions", String.valueOf(userStats.gamesPlayed()), true);
        statsEmbed.addField("Correct answers", String.valueOf(userStats.gamesWon()), true);

        statsEmbed.addField("% correct", winPercentage, true);
        return statsEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("trivia")) {
            if (cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            if (Objects.equals(event.getSubcommandName(), "question")) {
                String dif = event.getOption("difficulty", "", OptionMapping::getAsString);
                int difficulty = difficulties.indexOf(dif);
                difficulty = difficulty == -1 ? 0 : difficulty+1;
                TriviaQuestionsDatabase database = TriviaQuestionsDatabase.getInstance();
                TriviaQuestion question = database.getRandomQuestion(difficulty);
                MessageEmbed questionEmbed = generateQuestionEmbed(question);
                if (Cashew.triviaQuestionsManager.addQuestion(event.getUser().getId(), event.getChannel().getId(), question)) {
                    event.replyEmbeds(questionEmbed).queue();
                } else {
                    event.reply("You're already playing a game of trivia!").setEphemeral(true).queue();
                }
            } else {
                User targetUser = event.getOption("user", event.getUser(), OptionMapping::getAsUser);
                TriviaStatsDatabase database = TriviaStatsDatabase.getInstance();
                TriviaStats userStats = database.getUserStats(targetUser.getId());
                event.replyEmbeds(generateStatsEmbed(userStats, targetUser)).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().startsWith("trivia")) {
            if (event.getFocusedOption().getName().equals("difficulty")) {
                String typed = event.getOption("difficulty", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(difficulties, typed)).queue();
            }
        }
    }
}
