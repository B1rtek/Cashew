package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class Help extends BaseCommand {

    private final ArrayList<String> commands = new ArrayList<>() {
        {
            add("bestneko");
            add("birthday");
            add("boburnham");
            add("casesim");
            add("clear");
            add("cuddle");
            add("dadjoke");
            add("feedback");
            add("gifts");
            add("help");
            add("hug");
            add("info");
            add("insp");
            add("kiss");
            add("korwin");
            add("kromer");
            add("nekoichi");
            add("pat");
            add("ping");
            add("poll");
            add("reminder");
            add("reactions");
            add("scheduler");
            add("socialcredit");
        }
    };


    private MessageEmbed createSpecificHelpEmbed(String command) {
        EmbedBuilder specificHelpEmbed = new EmbedBuilder();
        specificHelpEmbed.setColor(0xffd297);
        switch (command.toLowerCase()) {
            case "bestneko" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Best neko");
                specificHelpEmbed.addField("`/bestneko set <neko>`", "Sets your favourite neko", false);
                specificHelpEmbed.addField("`/bestneko send`", "Sends a gif of your favourite neko", false);
            }
            case "birthday" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Birthday");
                specificHelpEmbed.setDescription("Birthday reminders system command - let everyone else know that it's your birthday through a bot's message!");
                specificHelpEmbed.addField("`/birthday set <month> <day> [hour] [message] [channel]`", "Sets a message that will be sent on your birthday (assuming you provide the right month and day). By default it sends the reminder at noon CEST on the default server channel (or the one in which you set the reminder if the server doesn't have one), and the default message is \"Happy Birthday, @User\", but you can set any message you want. You can have only one reminder per server.", false);
                specificHelpEmbed.addField("`/birthday delete`", "Deletes your birthday reminder", false);
                specificHelpEmbed.addField("`/birthday check`", "Displays your birthday reminder", false);
                specificHelpEmbed.addField("`/birthday checkdefault`", "Displays server's default birthday reminders channel and whether it overrides user's settings", false);
                specificHelpEmbed.addField("`/birthday setdefault <channel> <type>`", "Sets the default birthday reminders channel and whether that channel should override user's settings. This subcommand can only be used by moderators", false);
            }
            case "boburnham" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Bo Burnham");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "boburnham [\"nsfw\"]`", "Sends a quote from one of Bo Burnham's songs/shows. Addiing the \"nsfw\" option will send an nsfw quote instead.", false);
            }
            case "casesim" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Case Simulator");
                specificHelpEmbed.setDescription("Cashew's built in CS:GO Case simulator");
                specificHelpEmbed.addField("`/casesim opencase <case>`", "Opens the selected case", false);
                specificHelpEmbed.addField("`/casesim opencollection <collection>`", "Opens the selected collection", false);
                specificHelpEmbed.addField("`/casesim opencapsule <capsule>`", "Opens the selected capsule", false);
            }
            case "clear" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Clear");
                specificHelpEmbed.setDescription("A tool for moderators able to remove many messages at once.");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "clear <amount(unsigned int<100)>`", "Removes the given amount of recent messages. Messages older than 2 weeks can't be removed.", false);
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "clear range <ranges>`", "Removes recent messages in the given range. For example, `" + Cashew.COMMAND_PREFIX + "clear range 1 3-9 -4 -6-8` will remove the first recent message, and all recent messages from 3rd to 9th excluding the 4th and all from 6th to 8th.", false);
            }
            case "counting" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Counting");
                specificHelpEmbed.setDescription("Counting game command, can only be used by moderators");
                specificHelpEmbed.addField("`/counting toggle [toggle]`", "Toggles the counting game in the current channel on or off", false);
                specificHelpEmbed.addField("`/counting setcount [count]`", "Sets the current count value for the counting game", false);
                specificHelpEmbed.addField("`/counting reset`", "Resets the current count to zero", false);
            }
            case "cuddle" -> {
                specificHelpEmbed.setTitle("Cuddle");
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "cuddle <user(doesn't have to be an @)>`", "Sends an embed with a gif with cuddling nekos and a message saying that you decided to cuddle the specified user.", false);
            }
            case "dadjoke" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Dad Joke");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "dadjoke`", "Displays a random dad joke from https://icanhazdadjoke.com/", false);
            }
            case "feedback" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Feedback");
                specificHelpEmbed.setDescription("Sends feedback to Cashew's creator");
                specificHelpEmbed.addField("`/feedback <content>`", "Sends feedback with the provided content to Cashew's creator", false);
            }
            case "gifts" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Gifts");
                specificHelpEmbed.setDescription("Gift system command");
                specificHelpEmbed.addField("`/gifts gift <gift>`", "Generates a gift for anyone in chat to claim", false);
                specificHelpEmbed.addField("`/gifts stats <@user> [gift]`", "Retrieves user's gifting stats for all or the specified gift", false);
                specificHelpEmbed.addField("`/gifts leaderboard [gift] [scoreboard] [page]`", "Shows the leaderboard of the desired type. You can select the gift to show the leaderboard of, whether it should be a leaderboard of most received or most gifted gifts, and the page number. By default displays top 10 of the total received gifts leaderboard.", false);
            }
            case "help" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Help");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "help`", "bruh.", false);
            }
            case "hug" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Hug");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "hug <user(doesn't have to be an @)>`", "Sends an embed with a gif with hugging nekos and a message saying that you decided to hug the specified user.", false);
            }
            case "info" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Info");
                specificHelpEmbed.addField("`/info`", "Displays info about Cashew", false);
            }
            case "insp" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Inspirobot");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "insp`", "Generates an inspiring quote using https://inspirobot.me/", false);
            }
            case "kiss" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Kiss");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "kiss <user(doesn't have to be an @)>`", "Sends an embed with a gif with kissing nekos and a message saying that you decided to kiss the specified user.", false);
            }
            case "korwin" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Korwin");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "korwin`", "Generates a quote from Janusz Korwin-Mikke.", false);
            }
            case "kromer" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Kromer");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "kromer`", "Sends a random Kromer related meme/gif.", false);
            }
            case "nekoichi" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Nekoichi");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "nekoichi`", "Sends the first two lines of \"Nekoichi\" by Duca, the opening song from Nekopara Vol. 3.", false);
            }
            case "pat" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Pat");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "pat <user(doesn't have to be an @)>`", "Sends an embed with a gif with nekos getting pats and a message saying that you decided to pat the specified user.", false);
            }
            case "ping" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Ping");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "ping`", "Measures bot's ping (most likely highly inaccurate)", false);
            }
            case "poll" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Poll");
                specificHelpEmbed.addField("`/poll <title> <option1> <option2> [option3..5] [timetovote] [unit]`", "Creates a poll. Poll can have between two and five options inclusive. By default polls have time to vote set to 24 hours, this can be changed by setting the `timetovote` and `unit` options.", false);
            }
            case "reactions" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Reactions");
                specificHelpEmbed.setDescription("Manages Cashew's reactions to user's messages");
                specificHelpEmbed.addField("`/reactions set <toggle> [reaction] [#channel]`", "Changes reactions settings of a certain channel or reaction. Leaving the reaction field blank applies the setting to all reactions, leaving the channel blank applies the setting to all channels, both can be combined. Can only be used by moderators", false);
                specificHelpEmbed.addField("`/reactions info <reaction>`", "Shows information about the selected reaction, describes how it works and what triggers it", false);
            }
            case "reminder" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Reminder");
                specificHelpEmbed.setDescription("Lets you set up to 10 reminders that will be sent to your DMs");
                specificHelpEmbed.addField("`/reminder set <content> <time> [unit] [ping]`", "Sets a reminder. You need to provide the reminder content and the time after which it will be sent (by default the delay is measured in hours). You can change the delay unit using the `unit` option. You can also specify whether the bot should ping you with the reminder or not (by default ping is set to `true`). The reminder is automatically deleted from the reminders list after it's delivered", false);
                specificHelpEmbed.addField("`/reminder list`", "Shows the list of your reminders", false);
                specificHelpEmbed.addField("`/reminder delete <id>`", "Deletes the reminder with the specified ID, or all of them if you select ID = 0. The ID can be obtained with `/reminder list`", false);
            }
            case "scheduler" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Scheduler");
                specificHelpEmbed.setDescription("Daily message scheduling system, only accessible by moderators");
                specificHelpEmbed.addField("`/scheduler <#channel> <timestamp(HH:MM:SS GMT+1)> <message(string)>`", "Schedules a message to be sent in the specified channel every day on the specified time.", false);
                specificHelpEmbed.addField("`/scheduler show <id(int)|\"all\">`", "Shows the timed message with the specified id/all scheduled messages on this server.", false);
                specificHelpEmbed.addField("`/scheduler delete <id(int)|\"all\">`", "Removes the timed message with the specified id/all of them.", false);
            }
            case "socialcredit" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Social credit");
                specificHelpEmbed.addField("`/socialcredit modify [user] [amount] <reason>`", "Removes or adds social credit from a user, a reason can be added to the generated embed as well. Can only be used by moderators.", false);
                specificHelpEmbed.addField("`/socialcredit check <user>`", "Checks user's social credit score, by default yours.", false);
                specificHelpEmbed.addField("`/socialcredit leaderboard <scoreboard> <page>`", "Displays server's social credit leaderboard", false);
            }
            default -> {
                specificHelpEmbed.setTitle("Unknown");
                specificHelpEmbed.addField("There is no such command", "Try again :(", false);
            }
        }
        return specificHelpEmbed.build();
    }

    private EmbedBuilder createGeneralHelpEmbed(String cashewAvatarUrl) {
        EmbedBuilder helpEmbed = new EmbedBuilder();
        helpEmbed.setAuthor("🥜 Cashew's commands 🥜");
        helpEmbed.setThumbnail(cashewAvatarUrl);
        helpEmbed.addField("🎭 Roleplay", "`cuddle`, `hug`, `kiss`, `pat`", false);
        helpEmbed.addField("\uD83D\uDD27 Utilities", "`/feedback`, `/reminder`", false);
        helpEmbed.addField("😂 Fun stuff", "`/bestneko`, `/birthday`, `boburnham`, `/casesim`, `dadjoke`, `/gifts`, `insp`, `kromer`, `korwin`, `nekoichi`, `ping`, `/socialcredit`", false);
        helpEmbed.addField("\uD83D\uDD27 Mod's tools", "clear, `/reactions`, `/poll`", false);
        helpEmbed.addField("❓ Help", "To learn more about a specific command, type `/help <command>`. Note that some of the commands only work as slash commands. To get more information about the bot use `/info`", false);
        helpEmbed.setColor(0xffd297);
        return helpEmbed;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "help")) {
            if (args.length == 2) {
                MessageEmbed helpEmbed = createSpecificHelpEmbed(args[1]);
                event.getChannel().sendMessageEmbeds(helpEmbed).queue();
            } else {
                String cashewAvatarUrl = event.getJDA().getSelfUser().getAvatarUrl();
                EmbedBuilder helpEmbed = createGeneralHelpEmbed(cashewAvatarUrl);
                helpEmbed.setFooter("Called by " + Objects.requireNonNull(event.getMember()).getUser().getName(), event.getMember().getUser().getAvatarUrl());
                event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();
                helpEmbed.clear();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("help")) {
            String command = event.getOption("command", "", OptionMapping::getAsString);
            if (command.isEmpty()) {
                String cashewAvatarUrl = event.getJDA().getSelfUser().getAvatarUrl();
                event.replyEmbeds(createGeneralHelpEmbed(cashewAvatarUrl).build()).queue();
            } else {
                MessageEmbed helpEmbed = createSpecificHelpEmbed(command);
                event.replyEmbeds(helpEmbed).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("help")) {
            if (event.getFocusedOption().getName().equals("command")) {
                String typed = event.getOption("command", "", OptionMapping::getAsString);
                event.replyChoiceStrings(autocompleteFromList(commands, typed)).queue();
            }
        }
    }
}