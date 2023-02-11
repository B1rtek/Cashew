package com.birtek.cashew.timings;

import java.util.HashMap;

public class CAHGameManager {

    private HashMap<String, CAHGame> players = new HashMap<>();
    private HashMap<String, CAHGame> gameCodes = new HashMap<>();

    public boolean joinGame(String userID) {
        return joinGame(userID, null);
    }

    public boolean joinGame(String userID, String gameCode) {
        if(players.containsKey(userID)) return false;
        if(gameCode == null) {
            CAHGame newGame = new CAHGame();
            newGame.joinGame(userID);
            players.put(userID, newGame);
            gameCodes.put(newGame.getGameCode(), newGame);
        } else {
            CAHGame gameToJoin = gameCodes.get(gameCode);
            gameToJoin.joinGame(userID);
            players.put(userID, gameToJoin);
        }
        return true;
    }

    public CAHGame getGame(String userID) {
        return players.get(userID);
    }
}
