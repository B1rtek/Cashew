package com.birtek.cashew.commands;

import com.birtek.cashew.database.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

public class Gifts extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Gifts.class);

    public enum GiftsLeaderboardType {
        MOST_GIFTED,
        MOST_RECEIVED
    }

    ArrayList<String> leaderboardTypesStrings = new ArrayList<>() {
        {
            add("Most gifted");
            add("Most received");
        }
    };

    ArrayList<GiftInfo> availableGifts;

    ArrayList<String> availableGiftsNames = new ArrayList<>();

    HashSet<Integer> processedIDs = new HashSet<>();

    /**
     * Creates a Gifts command object and caches all gifts information as an ArrayList of {@link GiftInfo GiftInfos}
     * and saves the names of the gifts for option autocompletion
     */
    public Gifts() {
        GiftsDatabase database = GiftsDatabase.getInstance();
        availableGifts = database.getAvailableGifts();
        if (availableGifts == null) {
            LOGGER.error("GiftsDatabase.getAvailableGifts() returned null! No gifts are known!");
            availableGifts = new ArrayList<>();
        }
        for (GiftInfo gift : availableGifts) {
            availableGiftsNames.add(gift.getName());
        }
    }

    /**
     * Creates an {@link MessageEmbed embed} with an error message generated while requesting a gift
     *
     * @param message error message content
     * @return a {@link MessageEmbed MessageEmbed} with the error message in the author field
     */
    MessageEmbed generateGiftErrorEmbed(String message) {
        EmbedBuilder giftNotFoundEmbed = new EmbedBuilder();
        giftNotFoundEmbed.setAuthor("❌ " + message);
        return giftNotFoundEmbed.build();
    }

    /**
     * Creates an {@link MessageEmbed embed} containing a nitro-like gift
     *
     * @param giftName     name of the given gift
     * @param giftImageURL URL of the image to inlude in the embed
     * @return a {@link MessageEmbed MessageEmbed} with a message "A wild gift appears!" and the name of the gift in
     * the description, as well as it's image in the thumbnail
     */
    MessageEmbed generateGiftSuccessEmbed(String giftName, String giftImageURL) {
        EmbedBuilder giftSuccessEmbed = new EmbedBuilder();
        giftSuccessEmbed.setTitle("A wild gift appears!");
        giftSuccessEmbed.setThumbnail(giftImageURL);
        giftSuccessEmbed.setDescription(giftName);
        return giftSuccessEmbed.build();
    }

    /**
     * Calculates the difference between the given time and now
     *
     * @param lastTime timestamp representing time since epoch
     * @return difference in seconds between now and the given time in seconds
     */
    private long calculateTimeFromLastGift(long lastTime) {
        long now = Instant.now().getEpochSecond();
        return now - lastTime;
    }

    /**
     * Formats the time left into a message saying how much time left, for example 541 is turned into
     * "9 minutes and 1 second"
     *
     * @param time number of seconds representing the time to turn into a human-friendly string
     * @return String with the time formatted as a sentence
     */
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

    /**
     * Finds a gift by matching the provided name with the available gifts names
     *
     * @param giftName name entered by the user
     * @return a {@link GiftInfo GiftInfo} object matching the entered name, or null if none was found
     */
    GiftInfo findGiftByName(String giftName) {
        if (giftName.isEmpty()) {
            return new GiftInfo(0, "all", "", "", "", "", new Color(81, 195, 237));
        }
        for (GiftInfo availableGift : availableGifts) {
            if (availableGift.getName().toLowerCase(Locale.ROOT).contains(giftName.toLowerCase(Locale.ROOT))) {
                return availableGift;
            }
        }
        return null;
    }

    /**
     * Finds a gift by matching the provided display name with the provided String
     *
     * @param giftDescription String to match against display names of the gifts in the database
     * @return a {@link GiftInfo GiftInfo} object matching the entered display name, or null if none was found
     */
    GiftInfo findGiftByDescription(String giftDescription) {
        if (giftDescription.isEmpty()) {
            return new GiftInfo(0, "all", "", "", "", "", new Color(81, 195, 237));
        }
        for (GiftInfo availableGift : availableGifts) {
            if (availableGift.displayName().toLowerCase(Locale.ROOT).contains(giftDescription.toLowerCase(Locale.ROOT))) {
                return availableGift;
            }
        }
        return null;
    }

    /**
     * Checks whether the requested gift is valid, checks if the user isn't on the gift cooldown, and if the stats
     * update is successful, generates the gift embed, otherwise generates an error embed matching the description
     *
     * @param giftName name of the gift being gifted
     * @param user     {@link User user} who is gifting the gift
     * @param server   {@link Guild server} from which the gift request came
     * @return a {@link MessageEmbed MessageEmbed} with the gift or with an error message, both created either with
     * {@link #generateGiftErrorEmbed(String) generateGiftErrorEmbed()} or
     * {@link #generateGiftSuccessEmbed(String, String) generateGiftSuccessEmbed()}
     */
    MessageEmbed processAndGift(String giftName, User user, Guild server) {
        GiftInfo chosenGift = findGiftByName(giftName);
        if (chosenGift == null) {
            return generateGiftErrorEmbed("Gift " + giftName + " doesn't exist");
        }
        GiftHistoryDatabase database = GiftHistoryDatabase.getInstance();
        GiftStats userGiftStats = database.getGiftStats(chosenGift.getId(), user.getId(), server.getId());
        if (userGiftStats == null) {
            return generateGiftErrorEmbed("There was an error while obtaining your gift stats, try gifting again later.");
        }
        long timeDifference = calculateTimeFromLastGift(userGiftStats.getLastGifted());
        long cooldown = 600;
        if (timeDifference < cooldown) {
            return generateGiftErrorEmbed("You need to wait for " + formatTime(cooldown - timeDifference) + " before you can gift " + chosenGift.getName() + " again.");
        }
        //success
        GiftStats newStats = new GiftStats(userGiftStats.getAmountGifted() + 1, userGiftStats.getAmountReceived(), Instant.now().getEpochSecond());
        if (database.updateGiftStats(newStats, chosenGift.getId(), user.getId(), server.getId())) {
            return generateGiftSuccessEmbed(chosenGift.displayName(), chosenGift.getImageURL());
        } else {
            return generateGiftErrorEmbed("Something went wrong while updating your gift stats, try gifting again later.");
        }
    }

    /**
     * Generates an {@link MessageEmbed embed} with user's gift stats
     *
     * @param userGiftStats {@link GiftStats GiftStats} for the requested gift
     * @param giftInfo      {@link GiftInfo GiftInfo} of the requested gift
     * @param user          {@link User user} whose stats were requested
     * @param server        {@link Guild server} on which the stats were requested
     * @return a {@link MessageEmbed MessageEmbed} with the number of received and gifted gifts of the requested type
     */
    MessageEmbed generateGiftStatsEmbed(GiftStats userGiftStats, GiftInfo giftInfo, User user, Guild server) {
        EmbedBuilder giftStatsEmbed = new EmbedBuilder();
        giftStatsEmbed.setTitle("\uD83D\uDCCA " + Objects.requireNonNull(server.getMemberById(user.getId())).getEffectiveName() + "'s " + giftInfo.getName() + " gift stats");
        giftStatsEmbed.setThumbnail(giftInfo.getId() != 0 ? giftInfo.getImageURL() : Objects.requireNonNull(server.getMemberById(user.getId())).getEffectiveAvatarUrl());
        giftStatsEmbed.addField("Amount gifted", String.valueOf(userGiftStats.getAmountGifted()), true);
        giftStatsEmbed.addField("Amount received", String.valueOf(userGiftStats.getAmountReceived()), true);
        giftStatsEmbed.setColor(giftInfo.color().getRGB());
        return giftStatsEmbed.build();
    }

    /**
     * Creates a {@link MessageEmbed embed} with user's gift stats after checking if an existing gift was requested
     *
     * @param giftName name of the gift for which the stats were requested
     * @param user     {@link User user} whose stats were requested
     * @param server   {@link Guild server} on which the stats were requested
     * @return a {@link MessageEmbed MessageEmbed} with the stats of the gift or with an error message, both created
     * either with {@link #generateGiftErrorEmbed(String) generateGiftErrorEmbed()} or
     * {@link #generateGiftStatsEmbed(GiftStats, GiftInfo, User, Guild)  generateGiftStatsEmbed()}
     */
    MessageEmbed obtainStats(String giftName, User user, Guild server) {
        GiftInfo chosenGift = findGiftByName(giftName);
        if (chosenGift == null) {
            return generateGiftErrorEmbed("Gift " + giftName + " doesn't exist");
        }
        GiftHistoryDatabase database = GiftHistoryDatabase.getInstance();
        GiftStats userGiftStats = database.getGiftStats(chosenGift.getId(), user.getId(), server.getId());
        if (userGiftStats == null) {
            return generateGiftErrorEmbed("Gift stats could not be obtained, there was an error while querying the gift database.");
        }
        return generateGiftStatsEmbed(userGiftStats, chosenGift, user, server);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("gifts")) {
            if (!event.isFromGuild()) {
                event.reply("Gifts doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            if (!event.getChannel().canTalk()) {
                event.reply("This command won't work in here, Cashew can't see or/and write in this channel!").setEphemeral(true).queue();
                return;
            }
            if (event.getSubcommandName() == null) {
                event.reply("Bad command specified (how???)").setEphemeral(true).queue();
                return;
            }
            String giftName = event.getOption("gift", "", OptionMapping::getAsString);
            if (event.getSubcommandName().equals("gift")) {
                MessageEmbed giftEmbed = processAndGift(giftName, event.getUser(), event.getGuild());
                if (giftEmbed != null) {
                    if (Objects.equals(giftEmbed.getTitle(), "A wild gift appears!")) {
                        Random random = new Random();
                        String buttonID = event.getUser().getId() + ":gift:" + giftEmbed.getDescription() + ':' + random.nextInt(1048576);
                        event.replyEmbeds(giftEmbed).addActionRow(Button.success(buttonID, "ACCEPT")).queue();
                    } else {
                        event.replyEmbeds(giftEmbed).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Something went wrong while executing this command").setEphemeral(true).queue();
                }
            } else if (event.getSubcommandName().equals("stats")) {
                String targetUserID = event.getOption("user", "", OptionMapping::getAsString);
                User user = targetUserID.isEmpty() ? event.getUser() : Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getMemberById(targetUserID)).getUser();
                MessageEmbed statsEmbed = obtainStats(giftName, user, event.getGuild());
                if (statsEmbed != null) {
                    if (!Objects.requireNonNull(statsEmbed.getTitle()).startsWith("❌")) {
                        event.replyEmbeds(statsEmbed).queue();
                    } else {
                        event.replyEmbeds(statsEmbed).setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Something went wrong while executing this command").setEphemeral(true).queue();
                }
            } else if (event.getSubcommandName().equals("leaderboard")) {
                String leaderboard = event.getOption("scoreboard", leaderboardTypesStrings.get(1), OptionMapping::getAsString);
                int leaderboardIndex = leaderboardTypesStrings.indexOf(leaderboard);
                if (leaderboardIndex == -1) {
                    event.reply("This leaderboard doesn't exist").setEphemeral(true).queue();
                    return;
                }
                GiftsLeaderboardType leaderboardType = GiftsLeaderboardType.values()[leaderboardIndex];
                int pageNumber = event.getOption("page", 1, OptionMapping::getAsInt);
                if (pageNumber <= 0) {
                    event.reply("Invalid page number").setEphemeral(true).queue();
                    return;
                }
                GiftInfo chosenGift = findGiftByName(giftName);
                if (chosenGift == null) {
                    event.reply("This gift doesn't exist!").setEphemeral(true).queue();
                    return;
                }
                int giftID = chosenGift.id();
                GiftHistoryDatabase database = GiftHistoryDatabase.getInstance();
                ArrayList<LeaderboardRecord> leaderboardPage = database.getGiftsLeaderboardPage(leaderboardType, pageNumber, Objects.requireNonNull(event.getGuild()).getId(), giftID);
                if (leaderboardPage == null) {
                    event.reply("Something went wrong while fetching the chosen leaderboard page, try again later").setEphemeral(true).queue();
                    return;
                } else if (leaderboardPage.isEmpty()) {
                    if (pageNumber == 1) {
                        event.reply("This leaderboard doesn't yet exist, you can be the first person on it!").setEphemeral(true).queue();
                    } else {
                        event.reply("This page doesn't exist!").setEphemeral(true).queue();
                    }
                    return;
                }
                LeaderboardRecord callersStats = database.getGiftsLeaderboardUserStats(leaderboardType, Objects.requireNonNull(event.getGuild()).getId(), giftID, event.getUser().getId());
                if (callersStats == null) {
                    event.reply("Failed to obtain your stats from this leaderboard, try again later").setEphemeral(true).queue();
                    return;
                }
                int totalPages = database.getGiftsLeaderboardPageCount(leaderboardType, Objects.requireNonNull(event.getGuild()).getId(), giftID);
                if (totalPages == -1) {
                    event.reply("Failed to obtain the amount of pages of the leaderboard, try again later").setEphemeral(true).queue();
                    return;
                }
                generateAndSendLeaderboardEmbed(leaderboard, leaderboardIndex, leaderboardPage, event, chosenGift, callersStats, pageNumber, totalPages);
            }
        }
    }

    /**
     * Generates and sends an {@link MessageEmbed embed} with the requested gifts leaderboard
     *
     * @param leaderboard      title of the leaderboard type, for example "most gifted", when combined with the gift name creates
     *                         the whole title of the leaderboard
     * @param leaderboardIndex index of the type of the leaderboard that selects a leaderboard from the
     *                         leaderboardTypesStrings ArrayList
     * @param leaderboardPage  an ArrayList or {@link LeaderboardRecord LeaderboardRecords} which represent a page of
     *                         the leaderboard
     * @param event            {@link SlashCommandInteractionEvent event} that triggered this command
     * @param gift             {@link GiftInfo GiftInfo} object representing the gift of which the leaderboard was requested
     * @param callersStats     {@link GiftStats GiftStats} object with caller's stats
     * @param pageNumber       requested page number of the leaderboard, pages contain 10 entries
     * @param totalPages       total amount of pages of the requested leaderboard
     */
    private void generateAndSendLeaderboardEmbed(String leaderboard, int leaderboardIndex, ArrayList<LeaderboardRecord> leaderboardPage, SlashCommandInteractionEvent event, GiftInfo gift, LeaderboardRecord callersStats, int pageNumber, int totalPages) {
        String pointsName = leaderboardTypesStrings.get(leaderboardIndex).split("\\s+")[1];
        String capitalPointsName = Character.toString(pointsName.charAt(0) - 32) + pointsName.substring(1);
        InputStream generatedTableImage = generateLeaderboard(leaderboardPage, capitalPointsName, event.getJDA(), Objects.requireNonNull(event.getGuild()).getId(), gift.color());
        if (generatedTableImage == null) {
            event.reply("Something went wrong while generating the table image").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder leaderboardEmbed = new EmbedBuilder();
        if (gift.getId() != 0) {
            leaderboardEmbed.setTitle("Leaderboard for " + leaderboard.toLowerCase(Locale.ROOT) + " " + gift.getName() + "s");
        } else {
            leaderboardEmbed.setTitle("Leaderboard for " + leaderboard.toLowerCase(Locale.ROOT) + " gifts");
        }
        if (callersStats.place() != 0) {
            leaderboardEmbed.addField("Your position", "#" + callersStats.place() + " with " + callersStats.count() + " " + pointsName, false);
        } else {
            leaderboardEmbed.addField("Your position", "You haven't yet " + pointsName + " any " + (!Objects.equals(gift.getName(), "all") ? gift.getName() : "gift") + "s!", false);
        }
        if (gift.getId() != 0) {
            leaderboardEmbed.setThumbnail(gift.imageURL());
        } else {
            leaderboardEmbed.setThumbnail(event.getGuild().getIconUrl());
        }
        leaderboardEmbed.setColor(gift.color().getRGB());
        leaderboardEmbed.setImage("attachment://leaderboard.png");
        leaderboardEmbed.setFooter("Page " + pageNumber + " out of " + totalPages);
        event.replyFile(generatedTableImage, "leaderboard.png").addEmbeds(leaderboardEmbed.build()).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //userID:gift:giftName:randomGiftID or userID:gift:giftName (old format)
        String[] buttonID = event.getComponentId().split(":");
        if (!(buttonID.length == 4 || buttonID.length == 3)) {
            return;
        }
        String type = buttonID[1];
        if (!type.equals("gift")) {
            return;
        }
        String userID = buttonID[0];
        if (event.getUser().getId().equals(userID)) {
            event.reply("You can't accept a gift from yourself!").setEphemeral(true).queue();
            return;
        }
        String giftName = buttonID[2];
        GiftInfo giftInfo = findGiftByDescription(giftName);
        if (giftInfo == null) {
            event.reply("This gift doesn't exist").setEphemeral(true).queue();
            return;
        }
        int randomID = -1;
        // backwards compatibility moment
        if (buttonID.length == 4) {
            randomID = Integer.parseInt(buttonID[3]);
            if (processedIDs.contains(randomID)) {
                event.reply("This gift is already being obtained by someone else").setEphemeral(true).queue();
                return;
            }
            processedIDs.add(randomID);
        }
        //success
        String line1 = giftInfo.reactionLine1(), line2 = giftInfo.reactionLine2();
        line1 = line1.replace("<@!mention>", event.getUser().getAsMention());
        line2 = line2.replace("<@!mention>", event.getUser().getAsMention());
        String finalLine = line2;
        event.getChannel().sendMessage(line1).queue(message -> message.reply(finalLine).queue());
        try {
            event.getMessage().delete().queue();
        } catch (Exception e) {
            LOGGER.error("Exception thrown in Gifts.java:");
            System.err.println("Unknown Message");
            System.err.println("In server " + Objects.requireNonNull(event.getGuild()).getName());
            System.err.println("In channel " + event.getChannel().getName());
            System.err.println("While trying to accept gift: " + giftInfo.getName());
            return;
        }
        GiftHistoryDatabase database = GiftHistoryDatabase.getInstance();
        GiftStats oldGiftStats = database.getGiftStats(giftInfo.getId(), event.getUser().getId(), Objects.requireNonNull(event.getGuild()).getId());
        if (oldGiftStats == null) {
            event.reply("Failed to obtain your gift stats, try obtaining the gift later").setEphemeral(true).queue();
            return;
        }
        if (!database.updateGiftStats(new GiftStats(oldGiftStats.getAmountGifted(), oldGiftStats.getAmountReceived() + 1, oldGiftStats.getLastGifted()), giftInfo.getId(), event.getUser().getId(), event.getGuild().getId())) {
            event.reply("Updating gift stats failed, try obtaining the gift later").setEphemeral(true).queue();
            return;
        }
        if (randomID != -1) {
            processedIDs.remove(randomID);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().startsWith("gifts")) {
            if (event.getFocusedOption().getName().equals("gift")) {
                String typed = event.getOption("gift", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(availableGiftsNames, typed)).queue();
            } else if (event.getFocusedOption().getName().equals("scoreboard")) {
                String typed = event.getOption("scoreboard", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(leaderboardTypesStrings, typed)).queue();
            }
        }
    }
}
