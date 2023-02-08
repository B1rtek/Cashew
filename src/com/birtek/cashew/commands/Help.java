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

    public static final ArrayList<String> commands = new ArrayList<>() {
        {
            add("cmdset");
            add("help");
            add("bestneko");
            add("birthday");
            add("boburnham");
            add("cah");
            add("casesim");
            add("clear");
            add("counting");
            add("cuddle");
            add("dadjoke");
            add("feedback");
            add("gifts");
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
            add("reactionroles");
            add("reactions");
            add("reminder");
            add("roll");
            add("scheduler");
            add("socialcredit");
            add("trivia");
            add("when");
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
                specificHelpEmbed.addField("`/bestneko chart`", "Shows a piechart of global favourite nekos distribution", false);
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
            case "cah" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Cashew Against Humanity");
                specificHelpEmbed.setDescription("Cashew's clone of the card game Cards Against Humanity");
                specificHelpEmbed.addField("`/cah create`", "Creates a new game", false);
                specificHelpEmbed.addField("`/cah deck <deck code>`", "Adds a deck from ManyDecks to the game", false);
                specificHelpEmbed.addField("`/cah join <game code>`", "Joins a game", false);
                specificHelpEmbed.addField("`/cah leave`", "Leaves the game", false);
            }
            case "casesim" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Case Simulator");
                specificHelpEmbed.setDescription("Cashew's built in CS:GO Case simulator");
                specificHelpEmbed.addField("`/casesim opencase <case>`", "Opens the selected case", false);
                specificHelpEmbed.addField("`/casesim opencollection <collection>`", "Opens the selected collection", false);
                specificHelpEmbed.addField("`/casesim opencapsule <capsule>`", "Opens the selected capsule", false);
                specificHelpEmbed.addField("`/casesim inventory [user] [page]`", "Shows the selected user's inventory, by default yours. You can select the page to open, by default the first one is shown. You can inspect and delete items in your inventory", false);
                specificHelpEmbed.addField("`/casesim stats [user]`", "Shows the selected user's CaseSim stats, by default yours. You can change the visibility of your inventory here", false);
            }
            case "clear" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "Yes", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Clear");
                specificHelpEmbed.setDescription("A tool for moderators able to remove many messages at once.");
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "clear <amount(unsigned int<100)>`", "Removes the given amount of recent messages. Messages older than 2 weeks can't be removed.", false);
                specificHelpEmbed.addField('`' + Cashew.COMMAND_PREFIX + "clear range <ranges>`", "Removes recent messages in the given range. For example, `" + Cashew.COMMAND_PREFIX + "clear range 1 3-9 -4 -6-8` will remove the first recent message, and all recent messages from 3rd to 9th excluding the 4th and all from 6th to 8th.", false);
            }
            case "cmdset" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("CmdSet");
                specificHelpEmbed.setDescription("Turns on or off Cashew's commands");
                specificHelpEmbed.addField("`/reactions set <toggle> [reaction] [#channel]`", "Turns the selected command on or off. Leaving the reaction field blank applies the setting to all commands, leaving the channel blank applies the setting to all channels, both can be combined. Can only be used by moderators. Moderators can still use the command even if it's turned off.", false);
            }
            case "counting" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Counting");
                specificHelpEmbed.setDescription("Counting game command, can only be used by moderators");
                specificHelpEmbed.addField("`/counting toggle <toggle>`", "Toggles the counting game in the current channel on or off", false);
                specificHelpEmbed.addField("`/counting setcount <count>`", "Sets the current count value for the counting game", false);
                specificHelpEmbed.addField("`/counting reset`", "Resets the current count to zero", false);
                specificHelpEmbed.addField("`/counting mute <user>`", "Mutes or unmutes a user in the current channel's counting game. Muted users will get \uD83E\uDD28 as a reaction to their message instead", false);
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
                specificHelpEmbed.addField("`/poll <title> <option1> <option2> [option3..10] [timetovote] [unit]`", "Creates a poll. Poll can have between two and ten options inclusive. By default polls have time to vote set to 24 hours, this can be changed by setting the `timetovote` and `unit` options.", false);
            }
            case "reactionroles" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Reaction Roles");
                specificHelpEmbed.setDescription("Create an embed where server members can obtain roles by reacting");
                specificHelpEmbed.addField("`/reactionroles create [title] [role1..20]`", "Creates an embed with the list of obtainable roles. You can set a custom title of the embed if you feel like it. The role fields should be filled with an emote and a @role that will be obtained by reacting with the emote, separated by space, in order emote, role. It's a shortcut for setting up a bunch of WhenRules that would do the same thing, all WhenRules that make this work will be added to the list when this command is successfully executed.", false);
                specificHelpEmbed.addField("`/reactionroles remove <messageID>`", "Removes WhenRules associated with reaction roles given out from the message with provided ID", false);
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
                specificHelpEmbed.addField("`/reminder list`", "Shows the list of your reminders in an interactive embed where you can show the details of the reminder and delete the reminders", false);
            }
            case "roll" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "Yes", true);
                specificHelpEmbed.setTitle("Roll");
                specificHelpEmbed.addField("`/roll [sides] [rolls]`", "Rolls a dice. By default, a single roll of a d6 dice is performed, but you can roll up to 10000 times at once with a dice as big as INT_MAX", false);
            }
            case "scheduler" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Scheduler");
                specificHelpEmbed.setDescription("Daily message scheduling system, only accessible by moderators");
                specificHelpEmbed.addField("`/scheduler <#channel> <timestamp(HH:MM:SS GMT+1)> <message(string)>`", "Schedules a message to be sent in the specified channel every day on the specified time.", false);
                specificHelpEmbed.addField("`/scheduler list`", "Shows an interactive list of all scheduled message set on the server, the list allows for deleting the messages and seeing their full content and details", false);
            }
            case "socialcredit" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Social credit");
                specificHelpEmbed.addField("`/socialcredit modify <user> <amount> [reason]`", "Removes or adds social credit from a user, a reason can be added to the generated embed as well. Can only be used by moderators.", false);
                specificHelpEmbed.addField("`/socialcredit check [user]`", "Checks user's social credit score, by default yours.", false);
                specificHelpEmbed.addField("`/socialcredit leaderboard [scoreboard] [page]`", "Displays server's social credit leaderboard", false);
            }
            case "trivia" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("Trivia");
                specificHelpEmbed.setDescription("Test your Nekopara knowledge!");
                specificHelpEmbed.addField("`/trivia question [difficulty]`", "Gives you a random question regarding Nekopara to answer, you can select the difficulty if you want as well", false);
                specificHelpEmbed.addField("`/trivia stats [user]`", "Shows the Trivia game stats of a requested user (by default yours)", false);
                specificHelpEmbed.addField("`/trivia suggest <question> <answers> [image] [notes]`", "Suggest a new question to be included in the Trivia questions set. If your question has multiple answers, separate them with commas", false);
            }
            case "when" -> {
                specificHelpEmbed.addField("Works with prefix " + Cashew.COMMAND_PREFIX, "No", true);
                specificHelpEmbed.addField("Works in DMs", "No", true);
                specificHelpEmbed.setTitle("When");
                specificHelpEmbed.setDescription("Custom trigger-action rules system");
                specificHelpEmbed.addField("`/when when <trigger> <action> [options relevant to the rule]`", "Creates a new rule. All options that need to be set are included in the trigger and action descriptions. The message deletion trigger does not pass information about the user who deleted the message, so certain features such as assigning roles to that user might not work if the message isn't in message cache anymore (usually that means that the message is older than 1 hour)", false);
                specificHelpEmbed.addField("`/when list [page]`", "Shows an interactive list of all rules, the list allows for deleting the rules", false);
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
        helpEmbed.addField("\uD83D\uDD27 Utilities", "`/feedback`, `/reminder`, `/roll`", false);
        helpEmbed.addField("😂 Fun stuff", "`/bestneko`, `/birthday`, `boburnham`, `/casesim`, `/cah`, `/counting`, `dadjoke`, `/gifts`, `insp`, `korwin`, `kromer`, `nekoichi`, `ping`, `/socialcredit`, `/trivia`", false);
        helpEmbed.addField("\uD83D\uDD27 Mod's tools", "`/clear`, `/cmdset`, `/poll`, `/reactionroles`, `/reactions`, `/scheduler`, `/when`", false);
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