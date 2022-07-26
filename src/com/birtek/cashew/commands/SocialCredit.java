package com.birtek.cashew.commands;

import com.birtek.cashew.database.LeaderboardRecord;
import com.birtek.cashew.database.SocialCreditDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static java.lang.Math.abs;

public class SocialCredit extends BaseCommand {

    Permission[] socialCreditCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    String[] socialCreditGainURLs = {
            "https://cdn.discordapp.com/attachments/852811110158827533/897521731621769296/e65a53e6b7a5e0-945-560-18-26-1481-888.png",
            "https://cdn.discordapp.com/attachments/519234942526292002/897219539606077470/yKuJ11ouzZNXb8GAqfCv-F0xYkPD8P5ZEDdYvzRmo4MXELHB-4XQA0VezqW8Cx9OU50ic0gBTnOXjM0vuk8MV_uxoL5JBxC5ncoF.png",
            "https://cdn.discordapp.com/attachments/852811110158827533/897522529634246707/W020150908531421976510.png",
            "https://cdn.discordapp.com/attachments/852811110158827533/897523105075961886/5db9a2942100006f3ead458c.png",
            "https://cdn.discordapp.com/attachments/852811110158827533/897522694021591100/elmHouu0uIgGmYTGuRRqe-AKtw1q5qIz4LbS1I_mkX04byVUaLA0or2PFrxThzeE5651Xj72WoauxLZsqZgJ9j3b2ElK6-4stl0o.png",
            "https://cdn.discordapp.com/attachments/852811110158827533/897523170712633364/medvidek-pu-foto.png",
            "https://cdn.discordapp.com/attachments/852811110158827533/897523745101586482/1136_000_1f33d3.png",
            "https://cdn.discordapp.com/attachments/519234942526292002/897529296443310120/Screenshot_20211012-185915_Chrome.jpg",
            "https://cdn.discordapp.com/attachments/519234942526292002/897572550501077022/120488765_gettyimages-453444611.png",
            "https://cdn.discordapp.com/attachments/519234942526292002/897572784417419405/00085OAGOGA06VVS-C324-F4.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968969310603206656/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968969671384649808/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968969826305441862/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968969975949844530/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970057151569950/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970127737487360/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970191100837934/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970268385103882/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970684208402442/unknown.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/968970864185995274/unknown.png"
    };

    String[] socialCreditLossURLs = {
            "https://cdn.discordapp.com/attachments/852811110158827533/897523146104651776/deepfried_1634056589191.jpg",
            "https://cdn.discordapp.com/attachments/852811110158827533/897524014296219740/deepfried_1634056796110.jpg",
            "https://cdn.discordapp.com/attachments/519234942526292002/897214371976052777/BC6FjYlKqhQAAAAAElFTkSuQmCC.png"
    };

    ArrayList<String> socialCreditLeaderboardChoices = new ArrayList<>() {
        {
            add("highest");
            add("lowest");
        }
    };

    String extractUserID(String maybeUserID) {
        if (!maybeUserID.startsWith("<@!") || !maybeUserID.endsWith(">") || maybeUserID.length() != 22) {
            return "";
        } else {
            return maybeUserID.substring(3, 21);
        }
    }

    private MessageEmbed modifySocialCredit(String userID, Guild server, long socialCreditChange, String reason) {
        SocialCreditDatabase database = SocialCreditDatabase.getInstance();
        database.addSocialCredit(userID, server.getId(), socialCreditChange);
        EmbedBuilder socialCreditEmbed = new EmbedBuilder();
        String embedTitle = "**" + Objects.requireNonNull(server.getMemberById(userID)).getEffectiveName() + "**";
        Random random = new Random();
        if (socialCreditChange < 0) {
            embedTitle += " loses " + abs(socialCreditChange) + " social credit! :(";
            socialCreditEmbed.setTitle(embedTitle);
            socialCreditEmbed.setImage(socialCreditLossURLs[random.nextInt(socialCreditLossURLs.length)]);
            socialCreditEmbed.setColor(Color.red);
        } else {
            embedTitle += " gains " + socialCreditChange + " social credit! ;)";
            socialCreditEmbed.setTitle(embedTitle);
            socialCreditEmbed.setImage(socialCreditGainURLs[random.nextInt(socialCreditGainURLs.length)]);
            socialCreditEmbed.setColor(Color.green);
        }
        if (!reason.isEmpty()) {
            socialCreditEmbed.setDescription(reason);
        }
        return socialCreditEmbed.build();
    }

    private String checkSocialCredit(String userID, Guild server) {
        SocialCreditDatabase database = SocialCreditDatabase.getInstance();
        long socialCredit = database.getSocialCredit(userID, server.getId());
        String effectiveUserName = Objects.requireNonNull(server.getMemberById(userID)).getEffectiveName();
        return "User **" + effectiveUserName + "** has " + socialCredit + " social credit.";
    }

//    @Override
//    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
//        String[] args = event.getMessage().getContentRaw().split("\\s+");
//        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "socialcredit") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "soc")) {
//            if (checkPermissions(event, socialCreditCommandPermissions)) {
//                if (event.isWebhookMessage()) return;
//                boolean youFailed = false;
//                if (args.length == 1) {
//                    String message = "$socialcredit " + event.getMessage().getAuthor().getId();
//                    args = message.split("\\s+");
//                } else {
//                    args[1] = extractUserID(args[1]);
//                    if (args[1].isEmpty()) {
//                        youFailed = true;
//                    }
//                }
//                if (!youFailed) {
//                    if (args.length == 2) {
//                        event.getMessage().reply(checkSocialCredit(args[1], event.getGuild())).mentionRepliedUser(false).queue();
//                    } else if (args.length == 3) {
//                        if (checkPermissions(event, manageServerPermission)) {
//                            long socialCreditChange = 0;
//                            try {
//                                socialCreditChange = Long.parseLong(args[2]);
//                            } catch (NumberFormatException e) {
//                                youFailed = true;
//                            }
//                            if (!youFailed) {
//                                MessageEmbed socialCreditEmbed = modifySocialCredit(args[1], event.getGuild(), socialCreditChange, "");
//                                event.getChannel().sendMessageEmbeds(socialCreditEmbed).queue();
//                            }
//                        } else {
//                            youFailed = true;
//                        }
//                    } else {
//                        youFailed = true;
//                    }
//                }
//                if (youFailed) {
//                    if (!checkPermissions(event, manageServerPermission)) {
//                        int amountLost = loseSocialCredit(event.getAuthor().getId(), Objects.requireNonNull(event.getGuild()).getId());
//                        event.getMessage().reply("You lose " + amountLost + " social credit for misuse of the social credit system.").mentionRepliedUser(false).queue();
//                    }
//                }
//            }
//        }
//    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("socialcredit")) {
            if (isPrivateChannel(event)) {
                event.reply("Social credit doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            String targetUserID = event.getOption("user", event.getUser().getId(), OptionMapping::getAsString);
            if (Objects.equals(event.getSubcommandName(), "modify")) {
                if (!checkSlashCommandPermissions(event, manageServerPermission)) {
                    int amountLost = loseSocialCredit(event.getUser().getId(), Objects.requireNonNull(event.getGuild()).getId());
                    event.reply("You lose " + amountLost + " social credit for misuse of the social credit system.").queue();
                    return;
                }
                String reason = event.getOption("reason", "", OptionMapping::getAsString);
                long amount;
                try {
                    amount = event.getOption("amount", 0L, OptionMapping::getAsLong);
                } catch (ArithmeticException e) { // someone tried adding more than int allows lol
                    targetUserID = event.getUser().getId();
                    amount = -1;
                    reason = "Stop trying to break the bot by adding or removing more than INT_MAX or INT_MIN";
                }
                MessageEmbed socialCreditEmbed = modifySocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()), amount, reason);
                event.replyEmbeds(socialCreditEmbed).queue();
            } else if (Objects.equals(event.getSubcommandName(), "check")) {
                event.reply(checkSocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()))).queue();
            } else if (Objects.equals(event.getSubcommandName(), "leaderboard")) {
                boolean top = Objects.equals(event.getOption("scoreboard", "highest", OptionMapping::getAsString), "highest");
                int pageNumber = event.getOption("page", 1, OptionMapping::getAsInt);
                if (pageNumber <= 0) {
                    event.reply("Invalid page number").setEphemeral(true).queue();
                    return;
                }
                SocialCreditDatabase database = SocialCreditDatabase.getInstance();
                ArrayList<LeaderboardRecord> leaderboardPage = database.getSocialCreditLeaderboardPage(top, pageNumber, Objects.requireNonNull(event.getGuild()).getId());
                if (leaderboardPage == null) {
                    event.reply("Something went wrong while fetching the leaderboard, try again later").setEphemeral(true).queue();
                    return;
                } else if (leaderboardPage.isEmpty()) {
                    if (pageNumber == 1) {
                        event.reply("The social credit leaderboard for this server doesn't yet exist!").setEphemeral(true).queue();
                    } else {
                        event.reply("This page doesn't exist!").setEphemeral(true).queue();
                    }
                    return;
                }
                LeaderboardRecord callersStats = database.getSocialCreditLeaderboardUserStats(top, event.getGuild().getId(), event.getUser().getId());
                if (callersStats == null) {
                    event.reply("Failed to obtain your social credit score from this leaderboard, try again later").setEphemeral(true).queue();
                    return;
                }
                int totalPages = database.getSocialCreditLeaderboardPageCount(event.getGuild().getId());
                if (totalPages == -1) {
                    event.reply("Failed to obtain the amount of pages of the leaderboard, try again later").setEphemeral(true).queue();
                    return;
                }
                generateAndSendLeaderboardEmbed(event, top, leaderboardPage, callersStats, pageNumber, totalPages);
            } else {
                event.reply("Invalid subcommand (how?!)").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().startsWith("socialcredit")) {
            if(event.getFocusedOption().getName().equals("scoreboard")) {
                String typed = event.getOption("scoreboard", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(socialCreditLeaderboardChoices, typed)).queue();
            }
        }
    }

    /**
     * Generates an embed with the social credit leaderboard
     *
     * @param event           {@link SlashCommandInteractionEvent event} that triggered the leaderboard generation, can be replied
     *                        to with error messages in case something goes wrong
     * @param top             if set to true, the leaderboard is for highest social credit, otherwise it's for lowest
     * @param leaderboardPage an ArrayList of {@link LeaderboardRecord LeaderboardRecords} making up the page of the
     *                        leaderboard
     * @param callersStats    {@link LeaderboardRecord LeaderboardRecord} of the user who generated the leaderboard
     * @param pageNumber      the number of the generated page
     * @param totalPages      the total number of pages in the leaderboard
     */
    private void generateAndSendLeaderboardEmbed(SlashCommandInteractionEvent event, boolean top, ArrayList<LeaderboardRecord> leaderboardPage, LeaderboardRecord callersStats, int pageNumber, int totalPages) {
        InputStream generatedTableImage = generateLeaderboard(leaderboardPage, "Social Credit", event.getJDA(), Objects.requireNonNull(event.getGuild()).getId(), new Color(0xd63737), 18);
        if(generatedTableImage == null) {
            event.reply("Something went wrong while generating the leaderboard table image").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder leaderboardEmbed = new EmbedBuilder();
        leaderboardEmbed.setTitle("Leaderboard for " + (top?"highest":"lowest") + " social credit");
        if(callersStats.place() != 0) {
            leaderboardEmbed.addField("Your position", "#" + callersStats.place() + " with " + callersStats.count() + " credit", false);
        } else {
            leaderboardEmbed.addField("Your position", "You don't have a social credit record", false);
        }
        leaderboardEmbed.setThumbnail(event.getGuild().getIconUrl());
        leaderboardEmbed.setColor(new Color(0xd63737));
        leaderboardEmbed.setImage("attachment://leaderboard.png");
        leaderboardEmbed.setFooter("Page " + pageNumber + " out of " + totalPages);
        event.replyFile(generatedTableImage, "leaderboard.png").addEmbeds(leaderboardEmbed.build()).queue();
    }

    private int loseSocialCredit(String userID, String serverID) {
        SocialCreditDatabase database = SocialCreditDatabase.getInstance();
        Random random = new Random();
        int amountLost = random.nextInt(100) + 1;
        database.addSocialCredit(userID, serverID, -amountLost);
        return amountLost;
    }
}