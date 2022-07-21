package com.birtek.cashew.database;

public record GiftStats(int amountGifted, int amountReceived, long lastGifted) {

    public int getAmountGifted() {
        return amountGifted;
    }

    public int getAmountReceived() {
        return amountReceived;
    }

    public long getLastGifted() {
        return lastGifted;
    }
}
