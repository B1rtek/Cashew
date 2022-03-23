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

    public CaseWebscraper(String url) {
        System.out.println("Starting " + url);
        try {
            doc = Jsoup.connect(url).get();
            caseUrl = url;
            scrapeInfo();
            scrapeSkins();
            findKnifeUrl();
        } catch (IOException e) {
            System.err.println("Failed to connect to " + url);
        }
    }

    private void scrapeInfo() {
        ArrayList<Element> elements = doc.select(".margin-top-sm");
        caseName = elements.get(0).tagName("h1").text();
        elements = doc.select(".content-header-img-margin");
        caseImageUrl = elements.get(0).attributes().get("src");
    }

    private void scrapeSkins() {
        ArrayList<Element> elements = doc.select(".well.result-box.nomargin");
        for (int i=1; i<elements.size(); i++) {
            ArrayList<Element> linkElements = elements.get(i).getElementsByAttributeValueContaining("href", "/skin/");
            if(!linkElements.isEmpty()) {
                skins.add(linkElements.get(0).attributes().get("href"));
            }
        }
    }

    private void findKnifeUrl() {
        ArrayList<Element> elements = doc.select(".well.result-box.nomargin");
        knifeUrl = elements.get(0).getElementsByTag("a").get(0).attributes().get("href");
    }

    public ArrayList<String> getSkins() {
        return this.skins;
    }

    public String getKnivesUrl() {
        return knifeUrl;
    }

}
