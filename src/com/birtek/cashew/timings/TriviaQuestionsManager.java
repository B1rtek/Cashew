package com.birtek.cashew.timings;

import com.birtek.cashew.database.TriviaQuestion;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TriviaQuestionsManager {

    private final HashMap<String, TriviaQuestion> activeQuestions = new HashMap<>();
    private final HashMap<String, ScheduledFuture<?>> questionFutures = new HashMap<>();
    private final HashMap<String, Integer> gamesPlayedPerChannel = new HashMap<>();
    private final HashMap<String, String> players = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TriviaQuestionsManager(JDA jdaInstance) {
        TriviaQuestionTimer.setJdaInstance(jdaInstance);
    }

    /**
     * Adds a question to the pool of active questions and schedules its expiration timer
     *
     * @param channelID ID of the channel in which the trivia game is being played
     * @param userID    ID of the user who's playing
     * @param question  {@link TriviaQuestion} question that the player was assigned
     * @return true if the question was added to the pool, false if it wasn't because the player is already playing a
     * game of trivia
     */
    public boolean addQuestion(String userID, String channelID, TriviaQuestion question) {
        if (players.containsKey(userID)) return false;
        players.put(userID, channelID);
        if(gamesPlayedPerChannel.containsKey(channelID)) {
            gamesPlayedPerChannel.put(channelID, gamesPlayedPerChannel.get(channelID) + 1);
        } else {
            gamesPlayedPerChannel.put(channelID, 1);
        }
        activeQuestions.put(userID, question);
        TriviaQuestionTimer questionTimer = new TriviaQuestionTimer(channelID, userID);
        ScheduledFuture<?> questionFuture = scheduler.schedule(questionTimer, 15, TimeUnit.SECONDS);
        questionFutures.put(userID, questionFuture);
        return true;
    }

    /**
     * Checks whether user's response to the question is correct or not
     * @param userID ID of the user responding to the question
     * @param answer answer of the user
     * @return true if the answer is correct, false otherwise or if an error occurs
     */
    public boolean checkAnswer(String userID, String answer) {
        if(!activeQuestions.containsKey(userID)) return false;
        ArrayList<String> correctAnswers = activeQuestions.get(userID).answers();
        answer = answer.toLowerCase(Locale.ROOT);
        if(correctAnswers.contains(answer)) {
            removeQuestion(userID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a question from the manager's lists
     * @param userID ID of the user whose question is being removed from the lists
     */
    public void removeQuestion(String userID) {
        activeQuestions.remove(userID);
        questionFutures.get(userID).cancel(false);
        questionFutures.remove(userID);
        int currentGames = gamesPlayedPerChannel.get(players.get(userID)) - 1;
        if(currentGames == 0) {
            gamesPlayedPerChannel.remove(players.get(userID));
        } else {
            gamesPlayedPerChannel.put(players.get(userID), currentGames);
        }
        players.remove(userID);
    }

    /**
     * Checks whether there is a game going on in a certain channel
     * @param channelID ID of the channel to check for the game
     * @return true if there is a game going on in the channel, false otherwise
     */
    public boolean isBeingPlayedIn(String channelID) {
        return gamesPlayedPerChannel.containsKey(channelID);
    }
}
