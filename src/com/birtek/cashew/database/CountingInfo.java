package com.birtek.cashew.database;

public record CountingInfo(boolean active, String userID, int value, String messageID, int typosLeft) {
}
