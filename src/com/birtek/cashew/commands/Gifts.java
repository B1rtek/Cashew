package com.birtek.cashew.commands;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class Gifts extends BaseCommand {

    ArrayList<GiftInfo> availableGifts;

    public Gifts() {
        Database database = Database.getInstance();
        availableGifts = database.getAvailableGifts();
    }

    MessageEmbed generateGiftErrorEmbed(String message) {
        EmbedBuilder giftNotFoundEmbed = new EmbedBuilder();
        giftNotFoundEmbed.setTitle("❌ " + message);
        return giftNotFoundEmbed.build();
    }

    MessageEmbed generateGiftSuccessEmbed(String giftName, String giftImageURL) {
        EmbedBuilder giftSuccessEmbed = new EmbedBuilder();
        giftSuccessEmbed.setTitle("A wild gift appears!");
        giftSuccessEmbed.setThumbnail(giftImageURL);
        giftSuccessEmbed.setDescription(giftName);
        return giftSuccessEmbed.build();
    }

    private long calculateTimeFromLastGift(long lastTime) {
        long now = Instant.now().getEpochSecond();
        return now - lastTime;
    }

    private String formatTime(long time) {
        int minutes = (int) (time / 60);
        int seconds = (int) (time - minutes*60);
        StringBuilder result = new StringBuilder();
        if(minutes >= 0) {
            result = new StringBuilder(minutes).append(" minute");
            if(minutes != 1) {
                result.append('s');
            }
            result.append(" and ");
        }
        result.append(seconds).append(" second");
        if(seconds != 1) {
            result.append('s');
        }
        return result.toString();
    }

    MessageEmbed processAndGift(String giftName, User user, Guild server) {
        int chosenGift = -1;
        String fullGiftName = "unknown", giftImageURL = "none :(";
        for (int i = 0; i < availableGifts.size(); i++) {
            if (availableGifts.get(i).getName().toLowerCase(Locale.ROOT).contains(giftName.toLowerCase(Locale.ROOT))) {
                chosenGift = i + 1;
                fullGiftName = availableGifts.get(i).getName();
                giftImageURL = availableGifts.get(i).getImageURL();
                break;
            }
        }
        if (chosenGift == -1) {
            return generateGiftErrorEmbed("Gift " + giftName + " doesn't exist");
        }
        Database database = Database.getInstance();
        GiftStats userGiftStats = database.getUserGiftStats(chosenGift, user.getId(), server.getId());
        if (userGiftStats == null) {
            return generateGiftErrorEmbed("Gift could not be obtained, there was an error while querying the gift database.");
        }
        long timeDifference = calculateTimeFromLastGift(userGiftStats.getLastGifted());
        if (timeDifference < 3600) {
            return generateGiftErrorEmbed("You need to wait for " + formatTime(timeDifference) + " before you can gift " + fullGiftName + " again.");
        }
        //success
        GiftStats newStats = new GiftStats(userGiftStats.getAmountGifted()+1, userGiftStats.getAmountReceived(), Instant.now().getEpochSecond());
        database.updateUserGiftStats(newStats, chosenGift, user.getId(), server.getId());
        return generateGiftSuccessEmbed(fullGiftName, giftImageURL);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("gifts")) {
            if (event.getSubcommandName() == null) {
                event.reply("Bad command specified (how???)").setEphemeral(true).queue();
            }
            String giftName = event.getOption("gift", "", OptionMapping::getAsString);
            if (event.getSubcommandName().equals("gift")) {
                MessageEmbed giftEmbed = processAndGift(giftName, event.getUser(), event.getGuild());
                if (giftEmbed != null) {
                    if(Objects.equals(giftEmbed.getTitle(), "A wild gift appears!")) {
                        String buttonID = event.getUser().getId() + ":gift:" + giftEmbed.getDescription();
                        event.replyEmbeds(giftEmbed).addActionRow(Button.success(buttonID, "ACCEPT")).queue();
                    } else {
                        event.replyEmbeds(giftEmbed).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Something went wrong while executing this comand").setEphemeral(true).queue();
                }
            } else if (event.getSubcommandName().equals("stats")) {
                event.reply("Not implemented yet lol").queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if(buttonID.length != 3) {
            return;
        }
        String type = buttonID[1];
        if(!type.equals("gift")) {
            return;
        }
        String userID = buttonID[0];
        String giftName = buttonID[2];
        event.reply("Obtaining gifts not implemented yet").queue();
    }
}
