package com.birtek.cashew.commands;

public record SkinInfo(int caseId, String name, int rarity, float minFloat, float maxFloat, String description,
                       String flavorText, String finishStyle, String wearImg1, String wearImg2, String wearImg3,
                       String inspectFN, String inspectMW, String inspectFT, String inspectWW, String inspectBS,
                       String stashUrl) {
}

