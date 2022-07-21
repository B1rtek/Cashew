package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.SocialCreditDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "socialcredit") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "soc")) {
            if (checkPermissions(event, socialCreditCommandPermissions)) {
                if (event.isWebhookMessage()) return;
                boolean youFailed = false;
                if (args.length == 1) {
                    String message = "$socialcredit " + event.getMessage().getAuthor().getId();
                    args = message.split("\\s+");
                } else {
                    args[1] = extractUserID(args[1]);
                    if (args[1].isEmpty()) {
                        youFailed = true;
                    }
                }
                if (!youFailed) {
                    if (args.length == 2) {
                        event.getMessage().reply(checkSocialCredit(args[1], event.getGuild())).mentionRepliedUser(false).queue();
                    } else if (args.length == 3) {
                        if (checkPermissions(event, manageServerPermission)) {
                            long socialCreditChange = 0;
                            try {
                                socialCreditChange = Long.parseLong(args[2]);
                            } catch (NumberFormatException e) {
                                youFailed = true;
                            }
                            if (!youFailed) {
                                MessageEmbed socialCreditEmbed = modifySocialCredit(args[1], event.getGuild(), socialCreditChange, "");
                                event.getChannel().sendMessageEmbeds(socialCreditEmbed).queue();
                            }
                        } else {
                            youFailed = true;
                        }
                    } else {
                        youFailed = true;
                    }
                }
                if (youFailed) {
                    if (!checkPermissions(event, manageServerPermission)) {
                        int amountLost = loseSocialCredit(event.getAuthor().getId(), Objects.requireNonNull(event.getGuild()).getId());
                        event.getMessage().reply("You lose " + amountLost + " social credit for misuse of the social credit system.").mentionRepliedUser(false).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("socialcredit")) {
            if(isPrivateChannel(event)) {
                event.reply("Social credit doesn't work in DMs").setEphemeral(true).queue();
                return;
            }
            String targetUserID = event.getOption("user", event.getUser().getId(), OptionMapping::getAsString);
            String reason = event.getOption("reason", "", OptionMapping::getAsString);
            long amount;
            try {
                amount = event.getOption("amount", 0L, OptionMapping::getAsLong);
            } catch (ArithmeticException e) { // someone tried adding more than int allows lol
                targetUserID = event.getUser().getId();
                amount = -1;
                reason = "Stop trying to break the bot by adding or removing more than INT_MAX or INT_MIN";
            }
            if (amount == 0) { // credit check
                event.reply(checkSocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()))).queue();
            } else {
                if (checkSlashCommandPermissions(event, manageServerPermission)) {
                    MessageEmbed socialCreditEmbed = modifySocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()), amount, reason);
                    event.replyEmbeds(socialCreditEmbed).queue();
                } else {
                    int amountLost = loseSocialCredit(event.getUser().getId(), Objects.requireNonNull(event.getGuild()).getId());
                    event.reply("You lose " + amountLost + " social credit for misuse of the social credit system.").queue();
                }
            }
        }
    }

    private int loseSocialCredit(String userID, String serverID) {
        SocialCreditDatabase database = SocialCreditDatabase.getInstance();
        Random random = new Random();
        int amountLost = random.nextInt(100) + 1;
        database.addSocialCredit(userID, serverID, -amountLost);
        return amountLost;
    }
}