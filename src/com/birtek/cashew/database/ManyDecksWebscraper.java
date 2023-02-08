package com.birtek.cashew.database;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ManyDecksWebscraper {

    public static ArrayList<Card> getDeck(String deckCode) {
        Document deckPage = null;
        try {
            deckPage = Jsoup.connect("https://decks.rereadgames.com/decks/" + deckCode).get();
        } catch (IOException e) {
            return null;
        }
        ArrayList<Card> deck = new ArrayList<>();
        Element cardsList = deckPage.getElementsByClass("cards").get(0);
        boolean cardColor = true;
        Elements cardListsByType = cardsList.getElementsByTag("ul");
        for(Element cardTypeList: cardListsByType) {
            Elements cards = cardTypeList.getElementsByTag("li");
            for(Element card: cards) {
                Element content = card.getElementsByClass("face").get(0);
                ArrayList<String> contents = new ArrayList<>();
                if(cardColor) {
                    contents.add(content.text());
                } else {
                    Elements spans = content.getElementsByTag("span");
                    for(Element span: spans) {
                        if(span.hasClass("text")) {
                            contents.add(span.text());
                        }
                        if(span.hasClass("slot")) {
                            contents.add("__________");
                        }
                    }
                }
                deck.add(new Card(cardColor, contents));
            }
            cardColor = false;
        }
        return deck;
    }
}
