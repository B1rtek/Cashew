package com.birtek.casewebscraper;

import java.util.ArrayList;

public class Webscraper {
    public static void main(String[] args) {
        TargetLoader targetLoader = new TargetLoader();
        targetLoader.load();
        ArrayList<String> targets = targetLoader.getTargets();
        for (String target : targets) {
            CaseWebscraper caseWebscraper = new CaseWebscraper(target);
            ArrayList<String> skins = caseWebscraper.getSkins();
            String knifeUrl = caseWebscraper.getKnivesUrl();
            System.out.println("Found skins:");
            for (String skin : skins) {
                System.out.println(skin);
            }
            System.out.println("Knives: " + knifeUrl);
            System.out.println("Downloading skin data...");
            SkinWebscraper skinWebscraper = new SkinWebscraper();
            for (String skin : skins) {
                skinWebscraper.analyze(skin);
                System.out.println(skinWebscraper.getInfo());
            }
        }
    }
}