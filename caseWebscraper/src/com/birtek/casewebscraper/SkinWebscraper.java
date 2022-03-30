package com.birtek.casewebscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static java.util.Map.entry;

public class SkinWebscraper {

    private int caseID, rarity;
    private String name, description, flavorText, finishStyle, wearImg1, wearImg2, wearImg3, inspectFN, inspectMW, inspectFT, inspectWW, inspectBS, url;
    private double minFloat, maxFloat;
    Document doc;
    String type;

    private static final Logger LOGGER = LoggerFactory.getLogger(SkinWebscraper.class);

    private static final Map<String, Integer> rarityTranslator = Map.ofEntries(
            entry("Consumer", 0),
            entry("Industrial", 1),
            entry("Mil-Spec", 2),
            entry("Restricted", 3),
            entry("Classified", 4),
            entry("Covert", 5),
            entry("Remarkable", 3),
            entry("High Grade", 2),
            entry("Exotic", 4)
    );

    public SkinWebscraper(String type) {
        this.type = type;
    }

    private void clear() {
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

    public void analyze(String url) {
        clear();
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOGGER.error("Couldn't connect to " + url);
            return;
        }
        findRarity();
        findName();
        if (!type.equals("capsule")) {
            findSkinInfo();
        }
        findImages();
        this.url = url;
    }

    private void findRarity() {
        Element element = doc.select("a .nounderline, .quality, p .nomargin").get(0);
        String rarityString = element.text();
        String[] keys = rarityTranslator.keySet().toArray(new String[0]);
        for (String key : keys) {
            if (rarityString.contains(key)) {
                rarity = rarityTranslator.get(key);
                break;
            }
        }
    }

    private void findName() {
        name = doc.select(".well.result-box.nomargin").get(0).getElementsByTag("h2").get(0).text();
    }

    private void findSkinInfo() {
        ArrayList<Element> elements = doc.select(".well.text-left.wear-well, .skin-misc-details");
        if (elements.size() >= 2) {
            findFloats(elements.get(0));
            ArrayList<Element> children = elements.get(1).children();
            for (Element element : children) {
                if (description == null) description = getSkinDetail(element.text(), "Description: ");
                if(flavorText == null ) flavorText = getSkinDetail(element.text(), "Flavor Text: ");
                if(finishStyle == null) finishStyle = getSkinDetail(element.text(), "Finish Style: ");
            }
        }
    }

    private void findFloats(Element element) {
        ArrayList<Element> markers = element.getElementsByTag("div").get(0).getElementsByClass("marker-wrapper");
        if (!markers.isEmpty()) {
            minFloat = Double.parseDouble(markers.get(0).text());
            maxFloat = Double.parseDouble(markers.get(1).text());
        }
    }

    private String getSkinDetail(String candidate, String toGet) {
        if (candidate.startsWith(toGet)) {
            return candidate.substring(toGet.length());
        }
        return null;
    }

    private void findImages() {
        ArrayList<Element> inspectButtons = doc.select("div .btn-group-sm.btn-group-justified").get(0).children();
        for (Element button : inspectButtons) {
            String quality = "FN";
            if (!type.equals("capsule")) {
                quality = button.text().substring(9, 11);
            }
            String inspectLink = button.attributes().get("href");
            String wearImg = button.attributes().get("data-hoverimg");
            if (type.equals("capsule")) {
                wearImg = findCapsuleImage();
            } else if (name.contains("★ (Vanilla)")) {
                wearImg = findVanillaKnifeImage();
                quality = "FN";
            }
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

    private String findCapsuleImage() {
        return doc.select(".img-responsive.center-block.item-details-img").get(0).attributes().get("src");
    }

    private String findVanillaKnifeImage() {
        return doc.select(".img-responsive.center-block.main-skin-img.margin-top-sm.margin-bot-sm").get(0).attributes().get("src");
    }

    public void setCaseID(int caseId) {
        caseID = caseId;
    }

    public boolean saveToDatabase() {
        Database database = Database.getInstance();
        return database.saveItemToDatabase(this);
    }

    public int getCaseID() {
        return caseID;
    }

    public int getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public String getFinishStyle() {
        return finishStyle;
    }

    public String getWearImg1() {
        return wearImg1;
    }

    public String getWearImg2() {
        return wearImg2;
    }

    public String getWearImg3() {
        return wearImg3;
    }

    public String getInspectFN() {
        return inspectFN;
    }

    public String getInspectMW() {
        return inspectMW;
    }

    public String getInspectFT() {
        return inspectFT;
    }

    public String getInspectWW() {
        return inspectWW;
    }

    public String getInspectBS() {
        return inspectBS;
    }

    public String getUrl() {
        return url;
    }

    public double getMinFloat() {
        return minFloat;
    }

    public double getMaxFloat() {
        return maxFloat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContainerId(int id) {
        this.caseID = id;
    }

    String getInfo() {
        if (!type.equals("capsule")) {
            return "Skin " + name + ": \n" +
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
        } else {
            return name + ": \n" +
                    "   Rarity: " + rarity + "\n" +
                    "   Image: " + wearImg1 + "\n" +
                    "   Inspect: " + inspectFN;
        }

    }
}
