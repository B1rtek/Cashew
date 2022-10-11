package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.TriviaStatsDatabase;
import com.birtek.cashew.reactions.TriviaQuestionsListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public record TriviaQuestionTimer(String channelID, String userID) implements Runnable {

    private static JDA jdaInstance;

    public static void setJdaInstance(JDA jdaInstance) {
        TriviaQuestionTimer.jdaInstance = jdaInstance;
    }

    @Override
    public void run() {
        TextChannel targetChannel = jdaInstance.getTextChannelById(channelID);
        if (targetChannel != null) {
            targetChannel.sendMessage("<@!" + userID +">").addEmbeds(TriviaQuestionsListener.generateFailEmbed("You ran out of time!")).queue();
        }
        Cashew.triviaQuestionsManager.removeQuestion(userID);
        TriviaStatsDatabase database = TriviaStatsDatabase.getInstance();
        if(!database.updateUserStats(userID, false, null) && targetChannel != null) {
            targetChannel.sendMessage("<@!" + userID +"> failed to save your progress!").queue();
        }
    }
}
