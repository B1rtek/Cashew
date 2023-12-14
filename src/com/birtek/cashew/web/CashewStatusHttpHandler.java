package com.birtek.cashew.web;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.PollsDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class CashewStatusHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if ("GET".equals(httpExchange.getRequestMethod())) {
            OutputStream outputStream = httpExchange.getResponseBody();
            String responseContent;
            if(checkIfUrlAllowed(httpExchange.getRequestURI())) {
                if(httpExchange.getRequestURI().toString().equals("/")) {
                    responseContent = generateStatusPage();
                } else {
                    responseContent = getCSS();
                }
                httpExchange.sendResponseHeaders(200, responseContent.length());
            } else {
                responseContent = "Nothing to see here...";
                httpExchange.sendResponseHeaders(403, responseContent.length());
            }
            outputStream.write(responseContent.getBytes());
            outputStream.flush();
            outputStream.close();
        }
    }

    private boolean checkIfUrlAllowed(URI uri) {
        return uri.toString().equals("/") || uri.toString().equals("/style.css");
    }

    private String generateStatusPage() {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><title>Cashew Status</title><link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /></head><body><h1 id=\"header\">Cashew status</h1>");
        content.append("<table><tbody><tr><td colspan=\"2\" class=\"thdr\">Database</td></tr>");
        content.append("<tr><td colspan=\"1\" class=\"lside\">Postgres</td><td colspan=\"1\" class=\"rside\">").append(getDatabaseStatus()).append("</td></tr>");
        content.append("<tr></tr>");
        content.append("<tr><td colspan=\"2\" class=\"thdr\">Bots</td></tr>");
        content.append("<tr><td colspan=\"1\" class=\"lside\">Cashew</td><td colspan=\"1\" class=\"rside\">").append(getCashewStatus()).append("</td></tr>");
        content.append("<tr><td colspan=\"1\" class=\"lside\">Bad Wayfarer Bot</td><td colspan=\"1\" class=\"rside\">").append(getBWBStatus()).append("</td></tr>");
        content.append("</tbody></table>");
        content.append("</body></html>");
        return content.toString();
    }

    private String getCSS() {
        return "";
    }

    private String statusToString(boolean status) {
        if(status) {
            return "Working";
        }
        return "Dead :(";
    }

    private String getDatabaseStatus() {
        PollsDatabase database = PollsDatabase.getInstance();
        return statusToString(database.getAllPolls() != null);
    }

    private String getCashewStatus() {
        boolean status = true;
        try {
            Cashew.whenSettingsManager.getWhenRulesPageCount("0");
        } catch (Exception e) {
            status = false;
        }
        return statusToString(status);
    }

    private String getBWBStatus() {
        boolean status = true;
        try {
            status = Cashew.postsManager.getNextPostTime() != null;
        } catch (Exception ignored) {}
        return statusToString(status);
    }
}
