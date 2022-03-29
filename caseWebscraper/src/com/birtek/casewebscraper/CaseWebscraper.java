package com.birtek.casewebscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class CaseWebscraper {

    ArrayList<String> skins = new ArrayList<>();
    Document doc;

    String caseName, caseUrl, caseImageUrl, knifeUrl;
    String type;
    int caseId, knifeGroup;

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseWebscraper.class);

    public CaseWebscraper() {

    }

    public CaseWebscraper(String url) {
        try {
            analyze(url);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to " + url);
        }
    }

    public void analyze(String url) throws IOException {
        skins.clear();
        LOGGER.info("Starting " + url);
        doc = Jsoup.connect(url).get();
        if (invalidPage()) {
            LOGGER.error("Invalid URL");
            return;
        }
        caseUrl = url;
        scrapeInfo();
        scrapeItems();
        findKnifeUrl();
    }

    private boolean invalidPage() {
        return doc.select(".alert.alert-dismissible.alert-danger.fade.in.text-center").size() > 0;
    }

    private void scrapeInfo() {
        ArrayList<Element> elements = doc.select(".inline-middle.collapsed-top-margin, h1");
        ArrayList<Element> subelements = elements.get(0).getElementsByTag("h1");
        caseName = subelements.get(0).text();
        if (caseUrl.contains("Gloves")) {
            type = "gloves";
        } else {
            if (caseName.contains("Case")) {
                type = "case";
            } else if (caseName.contains("Collection")) {
                type = "collection";
            } else if (caseName.contains("Capsule")) {
                type = "capsule";
            }
        }
        elements = doc.select(".content-header-img-margin");
        caseImageUrl = elements.get(0).attributes().get("src");
    }

    private void scrapeItems() {
        String containing = type.equals("capsule") ? "/sticker/" : (type.equals("gloves") ? "/glove/" : "/skin/");
        ArrayList<Element> elements = doc.select(".well.result-box.nomargin");
        for (int i = 1; i < elements.size(); i++) {
            ArrayList<Element> linkElements = elements.get(i).getElementsByAttributeValueContaining("href", containing);
            if (!linkElements.isEmpty()) {
                skins.add(linkElements.get(0).attributes().get("href"));
            }
        }
    }

    private void findKnifeUrl() {
        if (type.equals("case")) {
            ArrayList<Element> elements = doc.select(".well.result-box.nomargin");
            knifeUrl = elements.get(0).getElementsByTag("a").get(0).attributes().get("href");
        }
    }

    public ArrayList<String> getItems() {
        return this.skins;
    }

    public String getKnivesUrl() {
        return knifeUrl;
    }

    public String getType() {
        return type;
    }

    public String getInfo() {
        return "Target: " + caseName + ", " + caseUrl + ", CID: " + caseId + ", KG: " + knifeGroup;
    }

    public void setCaseId(int id) {
        caseId = id;
    }

    public void setKnifeGroup(int group) {
        knifeGroup = group;
    }

    public boolean saveToDatabase() {
        Database database = Database.getInstance();
        return database.saveCaseToDatabase(caseName, caseUrl, caseImageUrl, knifeGroup);
    }
}
