package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public record TriviaQuestionTimer(String channelID, String userID) implements Runnable {

    private static JDA jdaInstance;

    public static void setJdaInstance(JDA jdaInstance) {
        TriviaQuestionTimer.jdaInstance = jdaInstance;
    }

    @Override
    public void run() {
        TextChannel targetChannel = jdaInstance.getTextChannelById(channelID);
        if(targetChannel != null) {
            targetChannel.sendMessage("<@!" + userID + ">, Time's up!").queue();
        }
        Cashew.triviaQuestionsManager.removeQuestion(userID);
    }
}
