package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Help extends BaseCommand {

    Permission[] helpCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "help")) {
            if (checkPermissions(event, helpCommandPermissions)) {
                if (args.length == 2) {
                    EmbedBuilder specificHelpEmbed = new EmbedBuilder();
                    specificHelpEmbed.setColor(0xffd297);
                    if (args[1].equalsIgnoreCase("cuddle")) {
                        specificHelpEmbed.setTitle("Cuddle");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"cuddle <user(doesn't have to be an @)>`", "Sends an embed with a gif with cuddling nekos and a message saying that you decided to cuddle the specified user.", false);
                    } else if (args[1].equalsIgnoreCase("hug")) {
                        specificHelpEmbed.setTitle("Hug");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"hug <user(doesn't have to be an @)>`", "Sends an embed with a gif with hugging nekos and a message saying that you decided to hug the specified user.", false);
                    } else if (args[1].equalsIgnoreCase("kiss")) {
                        specificHelpEmbed.setTitle("Kiss");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"kiss <user(doesn't have to be an @)>`", "Sends an embed with a gif with kissing nekos and a message saying that you decided to kiss the specified user.", false);
                    } else if (args[1].equalsIgnoreCase("pat")) {
                        specificHelpEmbed.setTitle("Pat");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"pat <user(doesn't have to be an @)>`", "Sends an embed with a gif with nekos getting pats and a message saying that you decided to pat the specified user.", false);
                    } else if (args[1].equalsIgnoreCase("opencase")) {
                        specificHelpEmbed.setTitle("Open case");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"opencase`", "Shows a list of CS:GO cases available to open.", false);
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"opencase <id(int)|name or part of the name of the case(string)>`", "Opens the specified case and shows the dropped item.", false);
                    } else if (args[1].equalsIgnoreCase("opencollection") || args[1].equalsIgnoreCase("opencol")) {
                        specificHelpEmbed.setTitle("Open collection");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"opencollection`", "Shows a list of CS:GO collections available to open.", false);
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"opencollection <id(int)|name or part of the name of the collection(string)>`", "Opens the specified collection and shows the dropped item.", false);
                        specificHelpEmbed.addField("Aliases", '`'+Cashew.COMMAND_PREFIX+"opencol`", false);
                    } else if (args[1].equalsIgnoreCase("bestneko")) {
                        specificHelpEmbed.setTitle("Best neko");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"bestneko`", "Sends a gif of Maple Minaduki", false);
                    } else if (args[1].equalsIgnoreCase("boburnham")) {
                        specificHelpEmbed.setTitle("Bo Burnham");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"boburnham [\"nsfw\"]`", "Sends a quote from one of Bo Burnham's songs/shows. Addiing the \"nsfw\" option will send an nsfw quote instead.", false);
                    } else if (args[1].equalsIgnoreCase("nekoichi")) {
                        specificHelpEmbed.setTitle("Nekoichi");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"nekoichi`", "Sends the first two lines of \"Nekoichi\" by Duca, the opening song from Nekopara Vol. 3.", false);
                    } else if (args[1].equalsIgnoreCase("socialcredit") || args[1].equalsIgnoreCase("soc")) {
                        specificHelpEmbed.setTitle("Social credit");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"socialcredit [@User]`", "Shows your social credit score, or the specified user's social credit score. Social credit can be added or removed only by the server admins.", false);
                        specificHelpEmbed.addField("Aliases", '`'+Cashew.COMMAND_PREFIX+"soc`", false);
                    } else if (args[1].equalsIgnoreCase("kromer")) {
                        specificHelpEmbed.setTitle("Kromer");
                        specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "kromer`", "Sends a random Kromer related meme/gif.", false);
                    } else if (args[1].equalsIgnoreCase("korwin")) {
                        specificHelpEmbed.setTitle("Korwin");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"korwin`", "Generates a quote from Janusz Korwin-Mikke.", false);
                    } else if (args[1].equalsIgnoreCase("help")) {
                        specificHelpEmbed.setTitle("Help");
                        specificHelpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"help`", "bruh.", false);
                    } else {
                        specificHelpEmbed.setTitle("Unknown");
                        specificHelpEmbed.addField("There is no such command", "Try again :(", false);
                    }
                    event.getChannel().sendMessageEmbeds(specificHelpEmbed.build()).queue();
                    specificHelpEmbed.clear();
                } else {
                    EmbedBuilder helpEmbed = new EmbedBuilder();
                    helpEmbed.setAuthor("🥜 Cashew's commands 🥜", null, Objects.requireNonNull(event.getGuild().getMemberById(Cashew.CASHEW_USER_ID)).getUser().getAvatarUrl());
                    helpEmbed.addField("🎭 Roleplay", "`cuddle`, `hug`, `kiss`, `pat`", false);
                    helpEmbed.addField("🔫 CS:GO", "`opencase`, `opencollection`", false);
                    helpEmbed.addField("😂 Fun stuff", "`bestneko`, `boburnham`, `nekoichi`, `socialcredit`, `kromer`, `korwin`", false);
                    helpEmbed.addField("❓ Help", "To learn more about a specific command, type "+Cashew.COMMAND_PREFIX+"help <command>.", false);
                    helpEmbed.setColor(0xffd297);
                    helpEmbed.setFooter("Called by " + Objects.requireNonNull(event.getMember()).getUser().getName(), event.getMember().getUser().getAvatarUrl());
                    event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();
                    helpEmbed.clear();
                    if (event.isWebhookMessage()) return;
                    if (checkPermissions(event, adminPermissions)) {
                        helpEmbed = new EmbedBuilder();
                        helpEmbed.setAuthor("🥜 Cashew's admin commands 🥜", null, Objects.requireNonNull(event.getGuild().getMemberById(Cashew.CASHEW_USER_ID)).getUser().getAvatarUrl());
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"clear <amount(unsigned int<100)>`", "Removes the given amount of recent messages. Messages older than 2 weeks can't be removed.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"clear range <ranges>`", "Removes recent messages in the given range. For example, `$clear range 1 3-9 -4 -6-8` will remove the first recent message, and all recent messages from 3rd to 9th excluding the 4th and all from 6th to 8th.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"reactions <\"off\"|\"on\"|\"all\">`", "Enables or disables reactions to messages containing \"69\", \"amogus\", etc in a text channel. Setting it to \"all\" enables the reactions with pings.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"reactions <#channel> <\"off\"|\"on\"|\"all\">`", "Enables or disables reactions in the specified channel.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"socialcredit <@User> <amount(int)>`", "Adds or removes someone's social credit on the server.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"timedmessage <#channel> <timestamp(HH:MM:SS GMT+1)> <message(string)>`", "Schedules a message to be sent in the specified channel every day on the specified time.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"timedmessage show <id(int)|\"all\">`", "Shows the timed message with the spcified id/all scheduled messages on this server.", false);
                        helpEmbed.addField('`'+Cashew.COMMAND_PREFIX+"timedmessage delete <id(int)|\"all\">`", "Removes the timed message with the specified id/all of them.", false);
                        helpEmbed.setColor(0xffd297);
                        event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(helpEmbed.build()).queue();
                        helpEmbed.clear();
                    }
                }
            }
        }
    }
}