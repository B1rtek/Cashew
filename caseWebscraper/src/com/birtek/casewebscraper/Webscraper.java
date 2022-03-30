package com.birtek.casewebscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Webscraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Webscraper.class);

    public static void main(String[] args) {
        Database database = Database.getInstance();
        TargetLoader targetLoader = new TargetLoader();
        targetLoader.load();
        ArrayList<String> targets = targetLoader.getTargets();
        for (String target : targets) {
            CaseWebscraper caseWebscraper = new CaseWebscraper(target);
            ArrayList<String> skins = caseWebscraper.getItems();
            String knifeUrl = caseWebscraper.getKnivesUrl();
            int containerId = database.getContainerId(caseWebscraper.getType());
            if(containerId == 0) {
                LOGGER.warn("Case ID set to 0, skipping...");
                continue;
            }
            caseWebscraper.setCaseId(containerId);
            LOGGER.info(caseWebscraper.getInfo());
            LOGGER.info("Found items:");
            for (String skin : skins) {
                System.out.println(skin);
            }
            LOGGER.info("Downloading skin data...");
            SkinWebscraper skinWebscraper = new SkinWebscraper(caseWebscraper.getType());
            for (String skin : skins) {
                skinWebscraper.analyze(skin);
                skinWebscraper.setCaseID(containerId);
                LOGGER.info("Saving " + skinWebscraper.getName());
                if(!skinWebscraper.saveToDatabase()) {
                    LOGGER.warn("Failed to save this item to the database!");
                }
            }
            if (caseWebscraper.getType().equals("case")) {
                LOGGER.info("Knives @: " + knifeUrl);
                LOGGER.info("Scraping knife list...");
                KnivesWebscraper knivesWebscraper = new KnivesWebscraper(knifeUrl);
                ArrayList<String> knives = knivesWebscraper.getItems();
                LOGGER.info(knivesWebscraper.getInfo());
                LOGGER.info("Found knives:");
                for (String knife : knives) {
                    System.out.println(knife);
                }
                if(knives.isEmpty()) {
                    LOGGER.error("No knives found, skipping");
                    continue;
                }
                LOGGER.info("Downloading knife data...");
                skinWebscraper.setType("knife");
                int knifeGroup = database.getKnifeGroup(knives.get(0));
                if(knifeGroup == 0) {
                    LOGGER.error("KnifeGroup set to 0, skipping");
                    continue;
                }
                if(database.knifesAlreadyDone(knives.get(0))) {
                    LOGGER.info("Knives already in the database, skipping");
                } else {
                    for (String knife : knives) {
                        skinWebscraper.analyze(knife);
                        skinWebscraper.setCaseID(knifeGroup);
                        LOGGER.info("Saving " + skinWebscraper.getName());
                        if(!skinWebscraper.saveToDatabase()) {
                            LOGGER.warn("Failed to save this rare special item to the database!");
                        }
                    }
                }
                caseWebscraper.setKnifeGroup(knifeGroup);
            }
            if(!caseWebscraper.saveToDatabase()) {
                LOGGER.error("Inserting case definition into the database failed!");
            } else {
                LOGGER.info("Container has been successfully analyzed.");
            }
        }
    }
}