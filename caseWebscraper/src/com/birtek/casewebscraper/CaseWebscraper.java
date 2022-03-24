package com.birtek.casewebscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class CaseWebscraper {

    ArrayList<String> skins = new ArrayList<>();
    Document doc;

    String caseName, caseUrl, caseImageUrl, knifeUrl;
    String type;

    public CaseWebscraper(String url) {
        System.out.println("Starting " + url);
        try {
            doc = Jsoup.connect(url).get();
            caseUrl = url;
            scrapeInfo();
            System.out.println(caseName);
            scrapeItems();
            findKnifeUrl();
        } catch (IOException e) {
            System.err.println("Failed to connect to " + url);
        }
    }

    private void scrapeInfo() {
        ArrayList<Element> elements = doc.select(".inline-middle.collapsed-top-margin, h1");
        ArrayList<Element> subelements = elements.get(0).getElementsByTag("h1");
        caseName = subelements.get(0).text();
        if (caseName.contains("Case")) {
            type = "case";
        } else if (caseName.contains("Collection")) {
            type = "collection";
        } else if (caseName.contains("Capsule")) {
            type = "capsule";
        }
        elements = doc.select(".content-header-img-margin");
        caseImageUrl = elements.get(0).attributes().get("src");
    }

    private void scrapeItems() {
        String containing = type.equals("capsule") ? "/sticker/" : "/skin/";
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
}
