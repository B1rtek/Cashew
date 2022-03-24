package com.birtek.casewebscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class SkinWebscraper {

    private int caseID, rarity;
    private String name, description, flavorText, finishStyle, wearImg1, wearImg2, wearImg3, inspectFN, inspectMW, inspectFT, inspectWW, inspectBS;
    private double minFloat, maxFloat;
    Document doc;

    private static final ArrayList<String> rarityTranslator = new ArrayList<String>() {
        {
            add("Consumer");
            add("Industrial");
            add("Mil-Spec");
            add("Restricted");
            add("Classified");
            add("Covert");
        }
    };

    public SkinWebscraper() {

    }

    void clear() {
        caseID = 0;
        rarity = -1;
        name = null;
        description = null;
        flavorText = null;
        finishStyle = null;
        wearImg1 = null;
        wearImg2 = null;
        wearImg3 = null;
        inspectFN = null;
        inspectMW = null;
        inspectFT = null;
        inspectWW = null;
        inspectBS = null;
        minFloat = -1;
        maxFloat = -1;
    }

    boolean analyze(String url) {
        clear();
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.err.println("Couldn't connect to " + url);
            return false;
        }
        findRarity();
        findName();
        findSkinInfo();
        findImages();
        return true;
    }

    private void findRarity() {
        Element element = doc.select("a .nounderline, .quality, p .nomargin").get(0);
        String rarityString = element.text();
        for (int i = 0; i < rarityTranslator.size(); i++) {
            if (rarityString.contains(rarityTranslator.get(i))) {
                rarity = i;
                break;
            }
        }
    }

    private void findName() {
        name = doc.select(".well.result-box.nomargin").get(0).getElementsByTag("h2").get(0).text();
    }

    private void findSkinInfo() {
        ArrayList<Element> elements = doc.select(".well.text-left.wear-well, .skin-misc-details");
        findFloats(elements.get(0));
        createDescription(elements.get(1).children().get(1).text());
        flavorText = elements.get(1).children().get(2).getElementsByTag("em").get(0).text();
        finishStyle = elements.get(1).children().get(3).getElementsByTag("span").get(0).text();
    }

    private void findFloats(Element element) {
        ArrayList<Element> markers = element.getElementsByTag("div").get(0).getElementsByClass("marker-wrapper");
        minFloat = Double.parseDouble(markers.get(0).text());
        maxFloat = Double.parseDouble(markers.get(1).text());
    }

    void createDescription(String candidate) {
        String bad = "Description: ";
        if (candidate.startsWith(bad)) {
            description = candidate.substring(bad.length());
        }
    }

    private void findImages() {
        ArrayList<Element> inspectButtons = doc.select("div .btn-group-sm.btn-group-justified").get(0).children();
        for (Element button : inspectButtons) {
            String quality = button.text().substring(9, 11);
            String inspectLink = button.attributes().get("href");
            String wearImg = button.attributes().get("data-hoverimg");
            switch (quality) {
                case "FN" -> {
                    wearImg1 = wearImg;
                    inspectFN = inspectLink;
                }
                case "MW" -> {
                    wearImg1 = wearImg;
                    inspectMW = inspectLink;
                }
                case "FT" -> {
                    wearImg2 = wearImg;
                    inspectFT = inspectLink;
                }
                case "WW" -> {
                    wearImg2 = wearImg;
                    inspectWW = inspectLink;
                }
                case "BS" -> {
                    wearImg3 = wearImg;
                    inspectBS = inspectLink;
                }
            }
        }
    }

    String getInfo() {
        return "--------------------------------------------------------------------------------\n" +
                "Skin " + name + ": \n" +
                "   Rarity: " + rarity + "\n" +
                "   Float range: " + minFloat + " - " + maxFloat + "\n" +
                "   Description: " + description + "\n" +
                "   Flavor text: " + flavorText + "\n" +
                "   Finish style: " + finishStyle + "\n" +
                "   Image (FN, MW): " + wearImg1 + "\n" +
                "   Image (FT, WW): " + wearImg2 + "\n" +
                "   Image (BS): " + wearImg3 + "\n" +
                "   Inspect (FN): " + inspectFN + "\n" +
                "   Inspect (MW): " + inspectMW + "\n" +
                "   Inspect (FT): " + inspectFT + "\n" +
                "   Inspect (WW): " + inspectWW + "\n" +
                "   Inspect (BS): " + inspectBS;
    }
}
