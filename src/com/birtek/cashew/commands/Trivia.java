package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.TriviaQuestion;
import com.birtek.cashew.database.TriviaQuestionsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
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
                event.reply("Not implemented yet").setEphemeral(true).queue();
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
