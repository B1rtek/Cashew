package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BaseCommand extends ListenerAdapter {

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public static boolean checkPermissions(GuildMessageReceivedEvent event, Permission[] neededPermissions) {
        if(event.isWebhookMessage()) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        if(event.getAuthor().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        if(event.getAuthor().isBot() || event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for(Permission neededPermission:neededPermissions) {
            if(!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String readURL(URLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

    protected static void sendCuddlyCommandGif(@NotNull GuildMessageReceivedEvent event, Random random, int gifNumber, String[] betterArgs, StringBuilder embedMessage, String[] reactions, EmbedGif[] gifs) {
        for(int i=1; i<betterArgs.length; i++)
        {
            embedMessage.append(" ");
            embedMessage.append(betterArgs[i]);
        }
        embedMessage.append("! ").append(reactions[random.nextInt(reactions.length)]);
        EmbedBuilder cuddleEmbed = new EmbedBuilder();
        cuddleEmbed.setColor(gifs[gifNumber].getColor());
        cuddleEmbed.setImage(gifs[gifNumber].getGifURL());
        cuddleEmbed.setAuthor(embedMessage.toString(), null, event.getAuthor().getAvatarUrl());
        event.getChannel().sendMessageEmbeds(cuddleEmbed.build()).queue();
        cuddleEmbed.clear();
    }

    protected String getCaseItemCondition() {
        Random random = new Random();
        int skinFloat = random.nextInt(10000);
        if(skinFloat<1471) {
            return "fn";
        } else if(skinFloat<3939) {
            return "mw";
        } else if(skinFloat<8257) {
            return "ft";
        } else if(skinFloat<9049) {
            return "ww";
        } else {
            return "bs";
        }
    }

    protected Pair<String, String> processCondition(String cond, String fn, String mw, String ft, String ww, String bs) {
        String condition = cond;
        if(condition.equals("fn") && fn.equals("empty")) {
            condition = "mw";
            if(mw.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "ww";
                    if(ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if(condition.equals("mw") && mw.equals("empty")) {
            condition = "fn";
            if(fn.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "ww";
                    if(ww.equals("empty")) {
                        condition = "bs";
                    }
                }
            }
        } else if(condition.equals("ft") && ft.equals("empty")) {
            condition = "ww";
            if(ww.equals("empty")) {
                condition = "mw";
                if(mw.equals("empty")) {
                    condition = "bs";
                    if(bs.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if(condition.equals("ww") && ww.equals("empty")) {
            condition = "bs";
            if(bs.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "mw";
                    if(mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        } else if(condition.equals("bs") && bs.equals("empty")) {
            condition = "ww";
            if(ww.equals("empty")) {
                condition = "ft";
                if(ft.equals("empty")) {
                    condition = "mw";
                    if(mw.equals("empty")) {
                        condition = "fn";
                    }
                }
            }
        }
        String imageURL = "";
        switch (condition) {
            case "fn":
                condition = "Factory New";
                imageURL = fn;
                break;
            case "mw":
                condition = "Minimal Wear";
                imageURL = mw;
                break;
            case "ft":
                condition = "Field-Tested";
                imageURL = ft;
                break;
            case "ww":
                condition = "Well-Worn";
                imageURL = ww;
                break;
            case "bs":
                condition = "Battle-Scarred";
                imageURL = bs;
                break;
        }
        return new Pair<>(condition, imageURL);
    }

    protected static void sendDroppedItemEmbed(@NotNull GuildMessageReceivedEvent event, String selectedCollectionName, String selectedCollectionURL, String selectedCollectionIconURL, String condition, String itemName, int embedColor, String flavorText, String imageURL) {
        EmbedBuilder drop = new EmbedBuilder();
        drop.setAuthor(selectedCollectionName, selectedCollectionURL, selectedCollectionIconURL);
        drop.addField(itemName, condition, false);
        drop.setImage(imageURL);
        drop.setColor(embedColor);
        if(!flavorText.equals("emptyFlavorLOL")) {
            drop.setFooter(flavorText);
        }
        event.getChannel().sendTyping().queue();
        event.getMessage().replyEmbeds(drop.build()).mentionRepliedUser(false).queueAfter(250, TimeUnit.MILLISECONDS);
    }
}