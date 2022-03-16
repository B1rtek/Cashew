package com.birtek.cashew.commands;

public class GiftInfo {
    int id;
    String name, imageURL;

    public GiftInfo(int id, String name, String imageURL) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }
}
