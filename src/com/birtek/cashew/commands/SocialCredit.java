package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

import static java.lang.Math.abs;

public class SocialCredit extends BaseCommand {

    Permission[] socialCreditCommandPermissions = {
            Permission.MESSAGE_WRITE
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

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String[] display = event.getMessage().getContentDisplay().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "socialcredit") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "soc")) {
            if(checkPermissions(event, socialCreditCommandPermissions)) {
                if(event.isWebhookMessage()) return;
                boolean youFailed=false;
                Database database = Database.getInstance();
                if(args.length==1) {
                    String message = "$socialcredit "+event.getMessage().getAuthor().getId();
                    args = message.split("\\s+");
                } else {
                    try {
                        int notSpecial=0;
                        while(args[1].charAt(notSpecial)=='<' || args[1].charAt(notSpecial)=='@' || args[1].charAt(notSpecial)=='!') {
                            notSpecial++;
                        }
                        args[1] = args[1].substring(notSpecial, args[1].length()-1);
                        long test = MiscUtil.parseSnowflake(args[1]);
                        if(args[1].length()!=18) {
                            youFailed=true;
                        }
                    } catch (Exception e) {
                        youFailed = true;
                    }
                }
                if(!youFailed) {
                    if(args.length==2) {
                        int socialCredit = database.getSocialCredit(args[1], event.getGuild().getId());
                        if(socialCredit==648294745) {
                            database.addSocialCredit(args[1], event.getGuild().getId(), 0);
                            socialCredit=0;
                        }
                        String messageContent;
                        try {
                            messageContent = "User "+Objects.requireNonNull(event.getGuild().getMemberById(args[1])).getUser().getName()
                                    +" has "+ socialCredit +" social credit.";
                        } catch (Exception e) {
                            messageContent = "User "+CombineName(display) +" has "+ socialCredit +" social credit.";
                        }
                        event.getMessage().reply(messageContent).mentionRepliedUser(false).queue();
                    } else if (args.length==3) {
                        if(checkPermissions(event, adminPermissions)) {
                            int socialCreditChange=0;
                            try {
                                socialCreditChange = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                youFailed = true;
                            }
                            if(!youFailed) {
                                database.addSocialCredit(args[1], event.getGuild().getId(), socialCreditChange);
                                EmbedBuilder socialCreditEmbed = new EmbedBuilder();
                                String embedTitle;
                                try {
                                    embedTitle = Objects.requireNonNull(event.getGuild().getMemberById(args[1])).getUser().getName();
                                } catch (Exception e) {
                                    embedTitle = CombineName(display);
                                }
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
                                event.getChannel().sendMessageEmbeds(socialCreditEmbed.build()).queue();
                            }
                        } else {
                            youFailed=true;
                        }
                    } else {
                        youFailed=true;
                    }
                }
                if(youFailed) {
                    if(!checkPermissions(event, adminPermissions)) {
                        database.addSocialCredit(event.getAuthor().getId(), event.getGuild().getId(), -1);
                        event.getMessage().reply("You lose 1 social credit for misuse of the social credit system.").mentionRepliedUser(false).queue();
                    }
                }
            }
        }
    }

    private String CombineName(String[] display) {
        StringBuilder name = new StringBuilder(display[1].substring(1));
        for(int i=2; i<display.length-1; i++) {
            name.append(" ").append(display[i]);
        }
        return name.toString();
    }
}