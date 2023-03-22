package com.birtek.cashew.timings;

import java.util.HashMap;
import java.util.Random;

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

    public boolean leaveGame(String userID) {
        CAHGame gameToLeave = players.get(userID);
        if(gameToLeave == null) return false;
        gameToLeave.leaveGame(userID);
        if(gameToLeave.getPlayersList().isEmpty()) {

        }
        return true;
    }

    public CAHGame getGame(String userID) {
        return players.get(userID);
    }

    public String generateGameCode() {
        String codeCandidate = "";
        while(codeCandidate.isEmpty() || gameCodes.containsKey(codeCandidate)) {
            Random random = new Random();
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                code.append((char)random.nextInt(65 ,91));
            }
            codeCandidate = code.toString();
        }
        return codeCandidate;
    }
}
