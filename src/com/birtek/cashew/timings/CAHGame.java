package com.birtek.cashew.timings;

import java.util.ArrayList;
import java.util.Random;

public class CAHGame {

    private final String gameCode;
    ArrayList<String> players = new ArrayList<>();

    public CAHGame() {
        gameCode = generateGameCode();
    }

    private String generateGameCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append((char)random.nextInt(65 ,91));
        }
        return code.toString();
    }

    public void joinGame(String userID) {
        players.add(userID);
    }

    public String getGameCode() {
        return gameCode;
    }

    public String getPlayersList() {
        StringBuilder result = new StringBuilder();
        for(String player: players) {
            result.append(player).append(", ");
        }
        result.delete(result.length()-2, result.length());
        return result.toString();
    }

    public String getDecksList() {
        return "none";
    }
}
