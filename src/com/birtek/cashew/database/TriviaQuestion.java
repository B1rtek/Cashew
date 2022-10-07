package com.birtek.cashew.database;

import java.util.ArrayList;

public final class TriviaQuestion {
    private final int id;
    private final String question;
    private final ArrayList<String> answers;
    private final int difficulty;
    private final String imageURL;
    private int responsesLeft;

    public TriviaQuestion(int id, String question, ArrayList<String> answers, int difficulty, String imageURL, int responsesLeft) {
        this.id = id;
        this.question = question;
        this.answers = answers;
        this.difficulty = difficulty;
        this.imageURL = imageURL;
        this.responsesLeft = responsesLeft;
    }

    public int id() {
        return id;
    }

    public String question() {
        return question;
    }

    public ArrayList<String> answers() {
        return answers;
    }

    public int difficulty() {
        return difficulty;
    }

    public String imageURL() {
        return imageURL;
    }

    /**
     * Decreases the amount of responses left
     * @return true if the responses limit was reached, false otherwise
     */
    public boolean responsesLimitHit() {
        if(responsesLeft == -1) return false;
        responsesLeft--;
        return responsesLeft == 0;
    }

    public boolean isResponseLimited() {
        return responsesLeft != -1;
    }

    public int getResponsesLeft() {
        return responsesLeft;
    }

}
