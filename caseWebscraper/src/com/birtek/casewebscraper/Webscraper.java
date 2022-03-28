package com.birtek.casewebscraper;

import java.util.ArrayList;

public class Webscraper {
    public static void main(String[] args) {
        TargetLoader targetLoader = new TargetLoader();
        targetLoader.load();
        ArrayList<String> targets = targetLoader.getTargets();
        for (String target : targets) {
            CaseWebscraper caseWebscraper = new CaseWebscraper(target);
            ArrayList<String> skins = caseWebscraper.getItems();
            String knifeUrl = caseWebscraper.getKnivesUrl();
            System.out.println(caseWebscraper.getInfo());
            System.out.println("Found items:");
            for (String skin : skins) {
                System.out.println(skin);
            }
            System.out.println("Downloading skin data...");
            SkinWebscraper skinWebscraper = new SkinWebscraper(caseWebscraper.getType());
            for (String skin : skins) {
                skinWebscraper.analyze(skin);
                System.out.println(skinWebscraper.getInfo());
            }
            if (caseWebscraper.getType().equals("case")) {
                System.out.println("Knives @: " + knifeUrl);
                System.out.println("Scraping knife list...");
                KnivesWebscraper knivesWebscraper = new KnivesWebscraper(knifeUrl);
                ArrayList<String> knives = knivesWebscraper.getItems();
                System.out.println(knivesWebscraper.getInfo());
                System.out.println("Found knives:");
                for (String knife : knives) {
                    System.out.println(knife);
                }
                System.out.println("Downloading knife data...");
                for (String knife : knives) {
                    skinWebscraper.analyze(knife);
                    System.out.println(skinWebscraper.getInfo());
                }
            }
        }
    }
}