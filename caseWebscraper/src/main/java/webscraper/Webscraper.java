package webscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Webscraper {
    public static void main(String[] args) {
        Document doc;
        try {
            doc = Jsoup.connect("https://csgostash.com/case/315/Snakebite-Case").get();
        } catch (IOException e) {
            System.err.println("tłuste f");
            return;
        }
        ArrayList<Element> caseImg = doc.select(".content-header-img-margin");
        for (Element element : caseImg) {
            Attributes attributes = element.attributes();
            System.out.println(attributes.get("src"));
        }
    }
}