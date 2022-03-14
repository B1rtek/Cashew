package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
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
            "https://cdn.discordapp.com/attachments/519234942526292002/897572784417419405/00085OAGOGA06VVS-C324-F4.png"
    };

    String[] socialCreditLossURLs = {
            "https://cdn.discordapp.com/attachments/852811110158827533/897523146104651776/deepfried_1634056589191.jpg",
            "https://cdn.discordapp.com/attachments/852811110158827533/897524014296219740/deepfried_1634056796110.jpg",
            "https://cdn.discordapp.com/attachments/519234942526292002/897214371976052777/BC6FjYlKqhQAAAAAElFTkSuQmCC.png"
    };

    String extractUserID(String maybeUserID) {
        if(!maybeUserID.startsWith("<@!") || !maybeUserID.endsWith(">") || maybeUserID.length() != 22) {
            return "";
        } else {
            return maybeUserID.substring(3, 21);
        }
    }

    private MessageEmbed modifySocialCredit(String userID, Guild server, int socialCreditChange) {
        Database database = Database.getInstance();
        database.addSocialCredit(userID, server.getId(), socialCreditChange);
        EmbedBuilder socialCreditEmbed = new EmbedBuilder();
        String embedTitle = "**"+Objects.requireNonNull(server.getMemberById(userID)).getEffectiveName()+"**";
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
        return socialCreditEmbed.build();
    }

    private String checkSocialCredit(String userID, Guild server) {
        Database database = Database.getInstance();
        int socialCredit = database.getSocialCredit(userID, server.getId());
        if (socialCredit == 648294745) {
            database.addSocialCredit(userID, server.getId(), 0);
            socialCredit = 0;
        }
        String effectiveUserName = Objects.requireNonNull(server.getMemberById(userID)).getEffectiveName();
        return "User **" + effectiveUserName + "** has " + socialCredit + " social credit.";
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String[] display = event.getMessage().getContentDisplay().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "socialcredit") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "soc")) {
            if (checkPermissions(event, socialCreditCommandPermissions)) {
                if (event.isWebhookMessage()) return;
                boolean youFailed = false;
                if (args.length == 1) {
                    String message = "$socialcredit " + event.getMessage().getAuthor().getId();
                    args = message.split("\\s+");
                } else {
                    args[1] = extractUserID(args[1]);
                    if(args[1].isEmpty()) {
                        youFailed = true;
                    }
                }
                if (!youFailed) {
                    if (args.length == 2) {
                        event.getMessage().reply(checkSocialCredit(args[1], event.getGuild())).mentionRepliedUser(false).queue();
                    } else if (args.length == 3) {
                        if (checkPermissions(event, adminPermissions)) {
                            int socialCreditChange = 0;
                            try {
                                socialCreditChange = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                youFailed = true;
                            }
                            if (!youFailed) {
                                MessageEmbed socialCreditEmbed = modifySocialCredit(args[1], event.getGuild(), socialCreditChange);
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
                    if (!checkPermissions(event, adminPermissions)) {
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
            if (checkSlashCommandPermissions(event, socialCreditCommandPermissions)) {
                String targetUserID = event.getOption("user", event.getUser().getId(), OptionMapping::getAsString);
                int amount = event.getOption("amount", 0, OptionMapping::getAsInt);
                if (amount == 0) { // credit check
                    event.reply(checkSocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()))).queue();
                } else {
                    if (checkSlashCommandPermissions(event, adminPermissions)) {
                        MessageEmbed socialCreditEmbed = modifySocialCredit(targetUserID, Objects.requireNonNull(event.getGuild()), amount);
                        event.replyEmbeds(socialCreditEmbed).queue();
                    } else {
                        int amountLost = loseSocialCredit(event.getUser().getId(), Objects.requireNonNull(event.getGuild()).getId());
                        event.reply("You lose " + amountLost + " social credit for misuse of the social credit system.").queue();
                    }
                }
            } else {
                event.reply("It seems like Xi-sama doesn't want you in their country.").queue();
            }
        }
    }

    private int loseSocialCredit(String userID, String serverID) {
        Database database = Database.getInstance();
        Random random = new Random();
        int amountLost = random.nextInt(100) + 1;
        database.addSocialCredit(userID, serverID, -amountLost);
        return amountLost;
    }
}