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
            int knifeGroup = database.getKnifeGroup(knifeUrl);
            if(containerId == 0 || knifeGroup == 0) {
                LOGGER.warn("Case ID of knife group set to 0, skipping...");
                continue;
            }
            caseWebscraper.setCaseId(containerId);
            caseWebscraper.setKnifeGroup(knifeGroup);
            if(!caseWebscraper.saveToDatabase()) {
                LOGGER.error("Inserting case definition into the database failed!");
                continue;
            }
            LOGGER.info(caseWebscraper.getInfo());
            LOGGER.info("Found items:");
            for (String skin : skins) {
                LOGGER.info(skin);
            }
            LOGGER.info("Downloading skin data...");
            SkinWebscraper skinWebscraper = new SkinWebscraper(caseWebscraper.getType());
            for (String skin : skins) {
                skinWebscraper.analyze(skin);
                LOGGER.info(skinWebscraper.getInfo());
            }
            if (caseWebscraper.getType().equals("case")) {
                LOGGER.info("Knives @: " + knifeUrl);
                LOGGER.info("Scraping knife list...");
                KnivesWebscraper knivesWebscraper = new KnivesWebscraper(knifeUrl);
                ArrayList<String> knives = knivesWebscraper.getItems();
                LOGGER.info(knivesWebscraper.getInfo());
                LOGGER.info("Found knives:");
                for (String knife : knives) {
                    LOGGER.info(knife);
                }
                LOGGER.info("Downloading knife data...");
                for (String knife : knives) {
                    skinWebscraper.analyze(knife);
                    LOGGER.info(skinWebscraper.getInfo());
                }
            }
        }
    }
}