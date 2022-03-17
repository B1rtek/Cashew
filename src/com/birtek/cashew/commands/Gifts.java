package com.birtek.cashew.commands;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
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
        giftNotFoundEmbed.setAuthor("❌ " + message);
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
        int seconds = (int) (time - minutes * 60);
        StringBuilder result = new StringBuilder();
        if (minutes >= 0) {
            result = new StringBuilder().append(minutes).append(" minute");
            if (minutes != 1) {
                result.append('s');
            }
            result.append(" and ");
        }
        result.append(seconds).append(" second");
        if (seconds != 1) {
            result.append('s');
        }
        return result.toString();
    }

    GiftInfo findGiftByName(String giftName) {
        for (GiftInfo availableGift : availableGifts) {
            if (availableGift.getName().toLowerCase(Locale.ROOT).contains(giftName.toLowerCase(Locale.ROOT))) {
                return availableGift;
            }
        }
        return null;
    }

    MessageEmbed processAndGift(String giftName, User user, Guild server) {
        GiftInfo chosenGift = findGiftByName(giftName);
        if (chosenGift == null) {
            return generateGiftErrorEmbed("Gift " + giftName + " doesn't exist");
        }
        Database database = Database.getInstance();
        GiftStats userGiftStats = database.getUserGiftStats(chosenGift.getId(), user.getId(), server.getId());
        if (userGiftStats == null) {
            return generateGiftErrorEmbed("Gift could not be obtained, there was an error while querying the gift database.");
        }
        long timeDifference = calculateTimeFromLastGift(userGiftStats.getLastGifted());
        if (timeDifference < 3600) {
            return generateGiftErrorEmbed("You need to wait for " + formatTime(3600 - timeDifference) + " before you can gift " + chosenGift.getName() + " again.");
        }
        //success
        GiftStats newStats = new GiftStats(userGiftStats.getAmountGifted() + 1, userGiftStats.getAmountReceived(), Instant.now().getEpochSecond());
        database.updateUserGiftStats(newStats, chosenGift.getId(), user.getId(), server.getId());
        return generateGiftSuccessEmbed(chosenGift.getName(), chosenGift.getImageURL());
    }

    MessageEmbed generateGiftStatsEmbed(GiftStats userGiftStats, GiftInfo giftInfo, User user, Guild server) {
        EmbedBuilder giftStatsEmbed = new EmbedBuilder();
        giftStatsEmbed.setTitle(Objects.requireNonNull(server.getMemberById(user.getId())).getEffectiveName() + "'s " + giftInfo.getName() + " gift stats");
        giftStatsEmbed.setThumbnail(giftInfo.getImageURL());
        giftStatsEmbed.addField("Amount gifted", String.valueOf(userGiftStats.getAmountGifted()), true);
        giftStatsEmbed.addField("Amount received", String.valueOf(userGiftStats.getAmountReceived()), true);
        return giftStatsEmbed.build();
    }

    MessageEmbed obtainStats(String giftName, User user, Guild server) {
        GiftInfo chosenGift = findGiftByName(giftName);
        if (chosenGift == null) {
            return generateGiftErrorEmbed("Gift " + giftName + " doesn't exist");
        }
        Database database = Database.getInstance();
        GiftStats userGiftStats = database.getUserGiftStats(chosenGift.getId(), user.getId(), server.getId());
        if(userGiftStats == null) {
            return generateGiftErrorEmbed("Gift stats could not be obtained, there was an error while querying the gift database.");
        }
        return generateGiftStatsEmbed(userGiftStats, chosenGift, user, server);
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
                    if (Objects.equals(giftEmbed.getTitle(), "A wild gift appears!")) {
                        String buttonID = event.getUser().getId() + ":gift:" + giftEmbed.getDescription();
                        event.replyEmbeds(giftEmbed).addActionRow(Button.success(buttonID, "ACCEPT")).queue();
                    } else {
                        event.replyEmbeds(giftEmbed).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Something went wrong while executing this comand").setEphemeral(true).queue();
                }
            } else if (event.getSubcommandName().equals("stats")) {
                String targetUserID = event.getOption("user", "", OptionMapping::getAsString);
                User user = targetUserID.isEmpty()?event.getUser(): Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getMemberById(targetUserID)).getUser();
                MessageEmbed statsEmbed = obtainStats(giftName, user, event.getGuild());
                if (statsEmbed != null) {
                    if (!Objects.requireNonNull(statsEmbed.getTitle()).startsWith("❌")) {
                        event.replyEmbeds(statsEmbed).queue();
                    } else {
                        event.replyEmbeds(statsEmbed).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Something went wrong while executing this comand").setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":");
        if (buttonID.length != 3) {
            return;
        }
        String type = buttonID[1];
        if (!type.equals("gift")) {
            return;
        }
        String userID = buttonID[0];
        if(event.getUser().getId().equals(userID)) {
            event.reply("You can't accept a gift from yourself!").setEphemeral(true).queue();
            return;
        }
        String giftName = buttonID[2];
        GiftInfo giftInfo = findGiftByName(giftName);
        if(giftInfo == null) {
            event.reply("This gift doesn't exist").setEphemeral(true).queue();
            return;
        }
        //success
        String line1 = giftInfo.reactionLine1(), line2 = giftInfo.reactionLine2();
        line1 = line1.replace("<@!mention>", event.getUser().getAsMention());
        line2 = line2.replace("<@!mention>", event.getUser().getAsMention());
        String finalLine = line2;
        event.getChannel().sendMessage(line1).queue(message -> message.reply(finalLine).queue());
        event.getMessage().delete().queue();
        Database database = Database.getInstance();
        GiftStats oldGiftStats = database.getUserGiftStats(giftInfo.getId(), event.getUser().getId(), Objects.requireNonNull(event.getGuild()).getId());
        if(oldGiftStats != null) {
            database.updateUserGiftStats(new GiftStats(oldGiftStats.getAmountGifted(), oldGiftStats.getAmountReceived()+1, oldGiftStats.getLastGifted()), giftInfo.getId(), event.getUser().getId(), event.getGuild().getId());
        } else {
            event.reply("Updating gift stats failed :(").setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().startsWith("gifts")) {
            if(event.getFocusedOption().getName().equals("gift")) {
                String typed = event.getOption("gift", "", OptionMapping::getAsString);
                ArrayList<String> matching = new ArrayList<>();
                for (GiftInfo gift : availableGifts) {
                    if(gift.getName().toLowerCase().contains(typed.toLowerCase(Locale.ROOT))) {
                        matching.add(gift.getName());
                    }
                }
                if(matching.size() > 25) {
                    event.replyChoiceStrings("There's more than 25 matching choices").queue();
                } else {
                    event.replyChoiceStrings(matching).queue();
                }
            }
        }
    }
}
