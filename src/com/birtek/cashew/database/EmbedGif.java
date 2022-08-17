package com.birtek.cashew.database;

public class EmbedGif {

    private final String gifURL;
    private final int color;

    public EmbedGif(String gifURL, int color) {
        this.gifURL = gifURL;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public String getGifURL() {
        return gifURL;
    }
}
