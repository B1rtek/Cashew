package com.birtek.casewebscraper;

import java.io.IOException;
import java.util.ArrayList;

public class KnivesWebscraper {

    String url;
    int knifeGroup;
    ArrayList<String> knives = new ArrayList<>();
    CaseWebscraper caseWebscraper;

    public KnivesWebscraper(String url) {
        int index = 1;
        while(true) {
            caseWebscraper = new CaseWebscraper();
            try {
                caseWebscraper.analyze(url+"&page="+index);
                if(caseWebscraper.getItems().isEmpty()) {
                    break;
                }
                knives.addAll(caseWebscraper.getItems());
            } catch (IOException e) {
                break;
            }
            index++;
        }
    }

    public ArrayList<String> getItems() {
        return knives;
    }

    public String getInfo() {
        return caseWebscraper.getInfo();
    }

}
