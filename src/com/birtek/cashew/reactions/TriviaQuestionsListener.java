package com.birtek.cashew.reactions;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.TriviaQuestion;
import com.birtek.cashew.database.TriviaStatsDatabase;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TriviaQuestionsListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(Cashew.triviaQuestionsManager.isBeingPlayedIn(event.getChannel().getId())) {
            TriviaQuestion usersQuestion = Cashew.triviaQuestionsManager.getUsersQuestion(event.getAuthor().getId());
            if(usersQuestion == null) return;
            if(Cashew.triviaQuestionsManager.checkAnswer(event.getAuthor().getId(), event.getMessage().getContentRaw().toLowerCase(Locale.ROOT))) {
                event.getMessage().reply("Correct!").mentionRepliedUser(false).queue();
                TriviaStatsDatabase database = TriviaStatsDatabase.getInstance();
                int updateResult = database.updateUserStats(event.getAuthor().getId(), true, usersQuestion);
                if(updateResult == -1) {
                    event.getMessage().reply("Failed to update your TriviaStats").mentionRepliedUser(false).queue();
                } else if (updateResult == 1) {
                    String difficultyName = switch (usersQuestion.difficulty()) {
                        case 1 -> "easy";
                        case 2 -> "medium";
                        case 3 -> "hard";
                        default -> "random (?) idk there was an error";
                    };
                    event.getMessage().reply("You have completed all " + difficultyName + " questions!").mentionRepliedUser(false).queue();
                }
            }
        }
    }
}
