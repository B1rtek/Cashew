package com.birtek.cashew;

import com.birtek.cashew.commands.*;
import com.birtek.cashew.reactions.CountingMessageDeletionDetector;
import com.birtek.cashew.reactions.CountingMessageModificationDetector;
import com.birtek.cashew.reactions.Counter;
import com.birtek.cashew.reactions.ReactionsExecutor;
import com.birtek.cashew.reactions.WhenExecutor;
import com.birtek.cashew.timings.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Cashew {

    public static String COMMAND_PREFIX = "$";
    public static String BIRTEK_USER_ID = "288000870187139073";
    public static ScheduledMessagesManager scheduledMessagesManager;
    public static BirthdayRemindersManager birthdayRemindersManager;
    public static RemindersManager remindersManager;
    public static PollManager pollManager;
    public static ReactionsSettingsManager reactionsSettingsManager;
    public static CommandsSettingsManager commandsSettingsManager;
    public static WhenSettingsManager whenSettingsManager;
    public static final Permission moderatorPermission = Permission.MANAGE_SERVER;
    public static final DefaultMemberPermissions moderatorPermissions = DefaultMemberPermissions.enabledFor(moderatorPermission);


    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault(System.getenv().get("TOKEN"))
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("NEKOPARA Vol. 3"))
                .setCompression(Compression.NONE)
                .addEventListeners(new Help(), new Clear(), new BestNeko(), new Nekoichi(), new Reactions(), new BoBurnham(), new Scheduler(),
                        new Cuddle(), new Hug(), new Kiss(), new Pat(), new SocialCredit(), new Korwin(), new Inspirobot(), new DadJoke(), new Counting(), new Ping(),
                        new Kromer(), new Gifts(), new CaseSim(), new Info(), new Birthday(), new Reminder(), new Feedback(), new Poll(), new Roll(), new CmdSet(), new When(), //commands
                        new CountingMessageDeletionDetector(), new CountingMessageModificationDetector(), new WhenExecutor(), //events
                        new ReactionsExecutor(), new Counter()) //messagereations
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("ping", "Measures bot's ping"),
                Commands.slash("help", "Shows the help embed containing information about commands")
                        .addOption(STRING, "command", "Command to show help of", false, true),
                Commands.slash("info", "Shows info about the bot"),
                Commands.slash("bestneko", "Set and send a gif of your favourite neko!")
                        .addSubcommands(new SubcommandData("set", "Set your favourite neko")
                                .addOption(STRING, "neko", "Neko to set as favourite", true, true))
                        .addSubcommands(new SubcommandData("send", "Send a random gif of your favourite neko"))
                        .addSubcommands(new SubcommandData("chart", "Shows a piechart of the globak favourite nekos distribution")),
                Commands.slash("boburnham", "Sends you a random quote from Bo Burnham's songs")
                        .addOption(STRING, "nsfw", "Decide whether you want an nsfw quote or not", false, true),
                Commands.slash("dadjoke", "Sends you a random dad joke from icanhazdadjoke.com"),
                Commands.slash("insp", "Sends you an inspiring quote from inspirobot.me"),
                Commands.slash("korwin", "Sends you a totally legit quote from an infamous politician Janusz Korwin-Mikke"),
                Commands.slash("nekoichi", "Sends two first lines of Nekoichi by Duca, the Nekopara Vol. 3 opening"),
                Commands.slash("clear", "Purges messages from chat")
                        .addOption(INTEGER, "recent", "Number of recent messages to remove")
                        .addOption(STRING, "range", "Range(s) of messages to remove, see help for more information")
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true),
                Commands.slash("counting", "Manages the counting game")
                        .addSubcommands(new SubcommandData("toggle", "Toggles counting on or off")
                                .addOption(STRING, "toggle", "Toggles counting on or off", false, true))
                        .addSubcommands(new SubcommandData("setcount", "Sets a new current count value")
                                .addOption(INTEGER, "count", "New count value", true))
                        .addSubcommands(new SubcommandData("reset", "Resets the counter"))
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true),
                Commands.slash("reactions", "Toggles Cashew's reactions to messages like 69 or amogus")
                        .addSubcommands(new SubcommandData("set", "Turns on or off reactions for certain messages")
                                .addOption(STRING, "toggle", "New state of the reaction - either ON or OFF", true, true)
                                .addOption(STRING, "reaction", "Reaction to change the settings of - leave blank to apply to all reactions", false, true)
                                .addOption(CHANNEL, "channel", "Channel to apply the setting to - leave blank to apply to all channels", false, false))
                        .addSubcommands(new SubcommandData("info", "Shows information about the reaction")
                                .addOption(STRING, "reaction", "Reaction to get the info about", true, true))
                        .setGuildOnly(true),
                Commands.slash("cuddle", "Cuddle someone!")
                        .addOption(STRING, "tocuddle", "A person (or a group of people) to cuddle", true)
                        .setGuildOnly(true),
                Commands.slash("hug", "Hug someone!")
                        .addOption(STRING, "tohug", "A person (or a group of people) to hug", true)
                        .setGuildOnly(true),
                Commands.slash("pat", "Pat someone!")
                        .addOption(STRING, "topat", "A person (or a group of people) to pat", true)
                        .setGuildOnly(true),
                Commands.slash("kiss", "Kiss someone!")
                        .addOption(STRING, "tokiss", "A person (or a group of people) to kiss", true)
                        .setGuildOnly(true),
                Commands.slash("kromer", "Sends you a random kromer gif"),
                Commands.slash("socialcredit", "The social credit system command, used to check and assign social credit")
                        .addSubcommands(new SubcommandData("modify", "Modifies user's social credit score")
                                .addOption(USER, "user", "User to modify the social credit of", true)
                                .addOption(INTEGER, "amount", "Amount of social credit to add (a negative number will mean that the social credit will be deducted)", true)
                                .addOption(STRING, "reason", "Reason for modifying the social credit score", false))
                        .addSubcommands(new SubcommandData("check", "Checks the social credit score of a user")
                                .addOption(USER, "user", "User to check the social credit score of, yours by default", false))
                        .addSubcommands(new SubcommandData("leaderboard", "Shows the leaderboard of the best citizens")
                                .addOption(STRING, "scoreboard", "Select the scoreboard to show (by default shows the one with the best citizens)", false, true)
                                .addOption(INTEGER, "page", "Number of the page to show (by default 1, which shows the top 10", false, false))
                        .setGuildOnly(true),
                Commands.slash("scheduler", "Message scheduler command")
                        .addSubcommands(new SubcommandData("add", "Schedule a new message")
                                .addOption(CHANNEL, "channel", "Destination channel of the message", true, false)
                                .addOption(STRING, "time", "Exact hour to send the message on (HH:MM:SS CET)", true, false)
                                .addOption(STRING, "content", "Content of the message", true, false))
                        .addSubcommands(new SubcommandData("list", "Shows all messages scheduled on this server in an interactive list")
                                .addOption(INTEGER, "page", "Page of the scheduled messages list to display"))
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true),
                Commands.slash("gifts", "Gift system command")
                        .addSubcommands(new SubcommandData("gift", "Give someone a gift!")
                                .addOption(STRING, "gift", "Name of the gift to gift", true, true))
                        .addSubcommands(new SubcommandData("stats", "Show your gifting stats")
                                .addOption(STRING, "gift", "Gift to show stats of", false, true)
                                .addOption(USER, "user", "User to show stats of"))
                        .addSubcommands(new SubcommandData("leaderboard", "Displays leaderboards of most gifted gifts etc")
                                .addOption(STRING, "gift", "Gift to display the leaderboard of (default is all)", false, true)
                                .addOption(STRING, "scoreboard", "Leaderboard to display", false, true)
                                .addOption(INTEGER, "page", "Page of the leaderboard to display (Default = 1, which is top 10)", false)),
                Commands.slash("casesim", "CS:GO opening simulator")
                        .addSubcommands(
                                new SubcommandData("opencase", "Opens a CS:GO Case")
                                        .addOption(STRING, "case", "Name of the case to open", true, true),
                                new SubcommandData("opencollection", "Opens a CS:GO Collection")
                                        .addOption(STRING, "collection", "Name of the collection to open", true, true),
                                new SubcommandData("opencapsule", "Opens a CS:GO Capsule")
                                        .addOption(STRING, "capsule", "Name of the capsule to open", true, true),
                                new SubcommandData("inventory", "Manage your CaseSim 4.0 inventory")
                                        .addOption(USER, "user", "User to check the inventory of (by default yours)", false, false)
                                        .addOption(INTEGER, "page", "Page to display, between 1 and 10, by default 1", false, false),
                                new SubcommandData("stats", "Shows CaseSim 4.0 stats")
                                        .addOption(USER, "user", "User to check the stats of", false, false)),
                Commands.slash("birthday", "Birthday reminder system command")
                        .addSubcommands(
                                new SubcommandData("set", "Set your birthday date and the reminder")
                                        .addOption(STRING, "month", "Month of your birthday", true, true)
                                        .addOption(INTEGER, "day", "Day of your birthday (as a number)", true, false)
                                        .addOption(STRING, "hour", "Hour to send the reminder at (optional, in HH:MM:SS CET, format, otherwise default is noon)", false, false)
                                        .addOption(STRING, "message", "Message to send as the reminder")
                                        .addOption(CHANNEL, "channel", "Channel to send the reminder in (otherwise it'll be the server default)", false),
                                new SubcommandData("delete", "Removes the reminder"),
                                new SubcommandData("setdefault", "Sets or displays the default reminders channel")
                                        .addOption(CHANNEL, "channel", "Channel to set the default/override to", true, false)
                                        .addOption(STRING, "type", "Default channel behaviour - should it override channels set by members or just be default?", true, true),
                                new SubcommandData("check", "Shows your birthday reminder"),
                                new SubcommandData("checkdefault", "Shows the default birthday reminders server settings"))
                        .setGuildOnly(true),
                Commands.slash("reminder", "Set reminders that will be delivered to your DMs!")
                        .addSubcommands(
                                new SubcommandData("set", "Set a reminder")
                                        .addOption(STRING, "content", "Content of the reminder", true)
                                        .addOption(INTEGER, "time", "Time after which the reminder will be sent", true)
                                        .addOption(STRING, "unit", "Unit of the specified time (hours is default)", false, true)
                                        .addOption(BOOLEAN, "ping", "Should the bot ping you with this reminder (default is yes)")
                        )
                        .addSubcommands(
                                new SubcommandData("list", "Show an interactive list of your reminders")
                        ),
                Commands.slash("feedback", "Send feedback to Cashew's creator!")
                        .addOption(STRING, "content", "Content of your feedback message", true, false),
                Commands.slash("poll", "Create a poll")
                        .addOption(STRING, "title", "Title of the poll", true, false)
                        .addOption(STRING, "option1", "Option 1 of the poll", true, false)
                        .addOption(STRING, "option2", "Option 2 of the poll", true, false)
                        .addOption(STRING, "option3", "Option 3 of the poll", false, false)
                        .addOption(STRING, "option4", "Option 4 of the poll", false, false)
                        .addOption(STRING, "option5", "Option 5 of the poll", false, false)
                        .addOption(STRING, "option6", "Option 6 of the poll", false, false)
                        .addOption(STRING, "option7", "Option 7 of the poll", false, false)
                        .addOption(STRING, "option8", "Option 8 of the poll", false, false)
                        .addOption(STRING, "option9", "Option 9 of the poll", false, false)
                        .addOption(STRING, "option10", "Option 10 of the poll", false, false)
                        .addOption(INTEGER, "timetovote", "Time after which the poll will conclude, by default 24 hours", false, false)
                        .addOption(STRING, "unit", "Unit of the time to vote, hours by default", false, true)
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true),
                Commands.slash("roll", "Roll a dice")
                        .addOption(INTEGER, "sides", "Number of sides of the dice, 6 by default", false, false)
                        .addOption(INTEGER, "rolls", "Number of rolls to perform", false, false),
                Commands.slash("cmdset", "Enable or disable commands")
                        .addOption(STRING, "toggle", "New state of the command - either ON or OFF", true, true)
                        .addOption(STRING, "command", "Command to change the settings of - leave blank to apply to all commands", false, true)
                        .addOption(CHANNEL, "channel", "Channel to apply the setting to - leave blank to apply to all channels", false, false)
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true),
                Commands.slash("when", "Cashew's custom trigger-action rules system")
                        .addSubcommands(
                                new SubcommandData("when", "Create a new rule")
                                        .addOption(STRING, "trigger", "Trigger for the action", true, true)
                                        .addOption(STRING, "action", "Action to perform", true, true)
                                        .addOption(STRING, "sourcemessageid", "ID of the source message for the trigger")
                                        .addOption(STRING, "sourcereactionemote", "Emote for the reaction trigger")
                                        .addOption(CHANNEL, "sourcechannel", "Source channel for the trigger")
                                        .addOption(STRING, "targetmessage", "Message content for the action")
                                        .addOption(CHANNEL, "targetchannel", "Target channel for the action")
                                        .addOption(ROLE, "targetrole", "Target role for the action"),
                                new SubcommandData("list", "Displays an interactive list of custom rules set on this server")
                                        .addOption(INTEGER, "page", "Number of the page of the rules to display, by default set to 1", false, false))
                        .setDefaultPermissions(moderatorPermissions)
                        .setGuildOnly(true)
        ).queue();
        scheduledMessagesManager = new ScheduledMessagesManager(jda);
        birthdayRemindersManager = new BirthdayRemindersManager(jda);
        remindersManager = new RemindersManager();
        remindersManager.setJDA(jda);
        pollManager = new PollManager();
        pollManager.start(jda);
        reactionsSettingsManager = new ReactionsSettingsManager();
        commandsSettingsManager = new CommandsSettingsManager();
        whenSettingsManager = new WhenSettingsManager();
    }
}