package com.birtek.cashew.database;

import com.birtek.cashew.commands.CaseSim;

public record SkinInfo(int id, int caseId, String name, CaseSim.SkinRarity rarity, float minFloat, float maxFloat, String description,
                       String flavorText, String finishStyle, String wearImg1, String wearImg2, String wearImg3,
                       String inspectFN, String inspectMW, String inspectFT, String inspectWW, String inspectBS,
                       String stashUrl) {
}

