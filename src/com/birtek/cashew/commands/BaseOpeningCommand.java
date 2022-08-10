package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Random;

/**
 * Used by the old OpenCase and OpenCollections commands which were replaced by /casesim
 */
public class BaseOpeningCommand extends BaseCommand {
    protected String getCaseItemCondition() {
        Random random = new Random();
        int skinFloat = random.nextInt(10000);
        if (skinFloat < 1471) {
            return "fn";
        } else if (skinFloat < 3939) {
            return "mw";
        } else if (skinFloat < 8257) {
            return "ft";
        } else if (skinFloat < 9049) {
            return "ww";
        } else {
            return "bs";
        }
    }

    protected TwoStringsPair processCondition(String cond, String fn, String mw, String ft, String ww, String bs) {
        String condition = cond;
        if (condition.equals("fn") && fn.equals("empty")) {
            condition = "mw";
            if (mw.equals("empty")) {
                condition = "ft";
                if (ft.equals("empty")) {
                    condition = "ww";
                    if (ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if (condition.equals("mw") && mw.equals("empty")) {
            condition = "fn";
            if (fn.equals("empty")) {
                condition = "ft";
                if (ft.equals("empty")) {
                    condition = "ww";
                    if (ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if (condition.equals("ft") && ft.equals("empty")) {
            condition = "ww";
            if (ww.equals("empty")) {
                condition = "mw";
                if (mw.equals("empty")) {
                    condition = "bs";
                    if (bs.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if (condition.equals("ww") && ww.equals("empty")) {
            condition = "bs";
            if (bs.equals("empty")) {
                condition = "ft";
                if (ft.equals("empty")) {
                    condition = "mw";
                    if (mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if (condition.equals("bs") && bs.equals("empty")) {
            condition = "ww";
            if (ww.equals("empty")) {
                condition = "ft";
                if (ft.equals("empty")) {
                    condition = "mw";
                    if (mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        }
        String imageURL = "";
        switch (condition) {
            case "fn" -> {
                condition = "Factory New";
                imageURL = fn;
            }
            case "mw" -> {
                condition = "Minimal Wear";
                imageURL = mw;
            }
            case "ft" -> {
                condition = "Field-Tested";
                imageURL = ft;
            }
            case "ww" -> {
                condition = "Well-Worn";
                imageURL = ww;
            }
            case "bs" -> {
                condition = "Battle-Scarred";
                imageURL = bs;
            }
        }
        return new TwoStringsPair(condition, imageURL);
    }

    protected MessageEmbed generateDroppedItemEmbed(String selectedCollectionName, String selectedCollectionURL, String selectedCollectionIconURL, String condition, String itemName, int embedColor, String flavorText, String imageURL) {
        EmbedBuilder drop = new EmbedBuilder();
        drop.setAuthor(selectedCollectionName, selectedCollectionURL, selectedCollectionIconURL);
        drop.addField(itemName, condition, false);
        drop.setImage(imageURL);
        drop.setColor(embedColor);
        if (!flavorText.equals("emptyFlavorLOL")) {
            drop.setFooter(flavorText);
        }
        return drop.build();
    }
}
