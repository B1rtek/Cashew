package com.birtek.cashew.commands;

public class GiftStats {
    private final int amountGifted;
    private final int amountReceived;
    private final long lastGifted;

    public GiftStats(int amountGifted, int amountReceived, long lastGifted) {
        this.amountGifted = amountGifted;
        this.amountReceived = amountReceived;
        this.lastGifted = lastGifted;
    }

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
