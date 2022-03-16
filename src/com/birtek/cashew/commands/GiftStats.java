package com.birtek.cashew.commands;

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
