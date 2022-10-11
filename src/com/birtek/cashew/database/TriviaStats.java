package com.birtek.cashew.database;

public record TriviaStats(String userID, String progress, int easy, int medium, int hard, int gamesPlayed, int gamesWon) {
    public int getProgressByDifficulty(int difficulty) {
        return switch (difficulty) {
            case 1 -> easy;
            case 2 -> medium;
            case 3 -> hard;
            default -> easy + medium + hard;
        };
    }
}
