package com.birtek.cashew.database;

import com.birtek.cashew.commands.BaseCommand;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ManyDecksWebscraper {

    public static ArrayList<Card> getDeck(String deckCode) {
        String deckJsonContent;
        URL dadJokeURL;
        try {
            dadJokeURL = new URL("https://decks.rereadgames.com/api/decks/" + deckCode);
        } catch (MalformedURLException e) {
            return null;
        }
        URLConnection dadJokeConnection;
        try {
            dadJokeConnection = dadJokeURL.openConnection();
            deckJsonContent = BaseCommand.readURL(dadJokeConnection);
        } catch (IOException e) {
            return null;
        }
        JSONObject deckJson = new JSONObject(deckJsonContent);
        ArrayList<Card> deck = new ArrayList<>();
        JSONArray calls = deckJson.getJSONArray("calls");
        JSONArray responses = deckJson.getJSONArray("responses");
        for (int i = 0; i < calls.length(); i++) {
            JSONArray content = calls.getJSONArray(i);
            ArrayList<String> cardContent = new ArrayList<>();
            for(int k = 0; k < content.length(); k++) {
                JSONArray subContent = content.getJSONArray(k);
                for (int j = 0; j < subContent.length(); j++) {
                    String part = subContent.optString(j);
                    if(part.equals("{}")) part = "__________";
                    cardContent.add(part);
                }
            }
            deck.add(new Card(false, cardContent));
        }
        for (int i=0; i<responses.length(); i++) {
            ArrayList<String> content = new ArrayList<>();
            content.add(responses.getString(i));
            deck.add(new Card(true, content));
        }
        return deck;
    }
}
