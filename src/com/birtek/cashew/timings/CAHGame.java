package com.birtek.cashew.timings;

import com.birtek.cashew.Cashew;

import java.util.ArrayList;

public class CAHGame {

    private final String gameCode;
    ArrayList<String> players = new ArrayList<>();

    public CAHGame() {
        gameCode = Cashew.cahGameManager.generateGameCode();
    }

    public void joinGame(String userID) {
        players.add(userID);
    }

    public void leaveGame(String userID) {
        players.remove(userID);
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
