package com.birtek.cashew;

import com.birtek.cashew.commands.*;
import com.birtek.cashew.events.CountingMessageDeletionDetector;
import com.birtek.cashew.events.CountingMessageModificationDetector;
import com.birtek.cashew.events.GuildMemberJoinAndLeave;
import com.birtek.cashew.messagereactions.Counter;
import com.birtek.cashew.messagereactions.OwosEtc;
import com.birtek.cashew.messagereactions.ReactToMaple;
import com.birtek.cashew.timings.BirthdayRemindersManager;
import com.birtek.cashew.timings.RemindersManager;
import com.birtek.cashew.timings.ScheduledMessagesManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
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
    public static String CASHEW_USER_ID = "856980494175174747";
    public static String BIRTEK_USER_ID = "288000870187139073";
    public static String NEKOPARA_EMOTES_UWU_SERVER_ID = "852811110158827530";
    public static String PI_SERVER_ID = "848907956379582484";
    public static ScheduledMessagesManager scheduledMessagesManager;
    public static BirthdayRemindersManager birthdayRemindersManager;

    public static RemindersManager remindersManager;

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault(System.getenv().get("TOKEN"))
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("NEKOPARA Vol. 3"))
                .setCompression(Compression.NONE)
                .addEventListeners(new Help(), new Clear(), new BestNeko(), new Nekoichi(), new Reactions(), new BoBurnham(), /*new OpenCase(), new OpenCollection(),*/ new TimedMessageCommand(),
                        new Cuddle(), new Hug(), new Kiss(), new Pat(), new SocialCredit(), new Korwin(), new Inspirobot(), new DadJoke(), new Counting(), new Ping(),
                        new Kromer(), new Gifts(), new CaseSim(), new Info(), new Birthday(), new Reminder(), new Feedback(), //commands
                        new GuildMemberJoinAndLeave(), new CountingMessageDeletionDetector(), new CountingMessageModificationDetector(), //events
                        new ReactToMaple(), new OwosEtc(), new Counter()) //messagereations
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("ping", "Measures bot's ping"),
                Commands.slash("help", "Shows the help embed containing information about commands")
                        .addOption(STRING, "command", "Command to show help of", false, true),
                Commands.slash("info", "Shows info about the bot"),
                Commands.slash("bestneko", "Sends you a gif of the best neko <3"),
                Commands.slash("boburnham", "Sends you a random quote from Bo Burnham's songs")
                        .addOption(STRING, "nsfw", "Decide whether you want an nsfw quote or not", false, true),
                Commands.slash("dadjoke", "Sends you a random dad joke from icanhazdadjoke.com"),
                Commands.slash("insp", "Sends you an inspiring quote from inspirobot.me"),
                Commands.slash("korwin", "Sends you a totally legit quote from an infamous politician Janusz Korwin-Mikke"),
                Commands.slash("nekoichi", "Sends two first lines of Nekoichi by Duca, the Nekopara Vol. 3 opening"),
                Commands.slash("clear", "Purges messages from chat")
                        .addOption(INTEGER, "recent", "Number of recent messages to remove")
                        .addOption(STRING, "range", "Range(s) of messages to remove, see help for more information"),
                Commands.slash("counting", "Manages the counting game")
                        .addOption(STRING, "toggle", "Toggles counting on or off", false, true)
                        .addOption(INTEGER, "setcount", "Sets the current count to the specified number")
                        .addOption(STRING, "reset", "Resets the counter (only if it's a definitive decision)", false, true),
                Commands.slash("reactions", "Toggles Cashew's reactions to messages like 69 or amogus")
                        .addOption(STRING, "toggle", "Toggles reactions on, off or turns even the annoying ones (all) on", false, true)
                        .addOption(CHANNEL, "channel", "The channel in which the change takes place, leave empty for the current one"),
                //Commands.slash("choccymilk", "Gift someone some Choccy Milk!"),
                Commands.slash("cuddle", "Cuddle someone!")
                        .addOption(STRING, "tocuddle", "A person (or a group of people) to cuddle", true),
                Commands.slash("hug", "Hug someone!")
                        .addOption(STRING, "tohug", "A person (or a group of people) to hug", true),
                Commands.slash("pat", "Pat someone!")
                        .addOption(STRING, "topat", "A person (or a group of people) to pat", true),
//                        .addOption(STRING, "patdevice", "Set to \"hand\" if you want the ruler pat gif", false, true),
                Commands.slash("kiss", "Kiss someone!")
                        .addOption(STRING, "tokiss", "A person (or a group of people) to kiss", true),
                Commands.slash("kromer", "Sends you a random kromer gif"),
                Commands.slash("socialcredit", "The social credit system command, used to check and assign social credit")
                        .addOption(USER, "user", "User to check or modify social credit of (to check yours, leave blank)")
                        .addOption(INTEGER, "amount", "Amount of social credit to add or subtract")
                        .addOption(STRING, "reason", "Reason for adding or removing social credit"),
                Commands.slash("scheduler", "Message scheduler command")
                        .addSubcommands(new SubcommandData("add", "Schedule a new message")
                                .addOption(CHANNEL, "channel", "Destination channel of the message", true, false)
                                .addOption(STRING, "time", "Exact hour to send the message on (HH:MM:SS CET)", true, false)
                                .addOption(STRING, "content", "Content of the message", true, false))
                        .addSubcommands(new SubcommandData("list", "Shows all messages scheduled on this server")
                                .addOption(INTEGER, "id", "ID of the message to display (optional)"))
                        .addSubcommands(new SubcommandData("delete", "Deletes the specified messages")
                                .addOption(STRING, "all", "Deletes ALL scheduled messages (type \"definitely\" to confirm)")
                                .addOption(INTEGER, "id", "ID of the messsage to delete")),
//                Commands.slash("opencase", "CS:GO case opening simulator")
//                        .addOption(STRING, "case", "Name of the case to open", false, true)
//                        .addOption(INTEGER, "id", "ID of the case to open (IDs are assigned in chronological order)"),
//                Commands.slash("opencollection", "CS:GO collection opening simulator")
//                        .addOption(STRING, "collection", "Name of the collection to open", false, true)
//                        .addOption(INTEGER, "id", "ID of the collection to open (IDs are assigned in chronological order)"),
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
                                new SubcommandData("inventory", "Manage your CaseSim 4.0 inventory")),
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
                                new SubcommandData("checkdefault", "Shows the default birthday reminders server settings")),
                Commands.slash("reminder", "Set reminders that will be delivered to your DMs!")
                        .addSubcommands(
                                new SubcommandData("set", "Set a reminder")
                                        .addOption(STRING, "content", "Content of the reminder", true)
                                        .addOption(INTEGER, "time", "Time after which the reminder will be sent", true)
                                        .addOption(STRING, "unit", "Unit of the specified time (hours is default)", false, true)
                                        .addOption(BOOLEAN, "ping", "Should the bot ping you with this reminder (default is yes)")
                        )
                        .addSubcommands(
                                new SubcommandData("list", "List your reminders")
                        )
                        .addSubcommands(
                                new SubcommandData("delete", "Delete a reminder")
                                        .addOption(INTEGER, "id", "ID of the reminder to delete", true)
                        ),
                Commands.slash("feedback", "Send feedback to Cashew's creator!")
                        .addOption(STRING, "content", "Content of your feedback message", true, false)
        ).queue();
        scheduledMessagesManager = new ScheduledMessagesManager(jda);
        birthdayRemindersManager = new BirthdayRemindersManager(jda);
        remindersManager = new RemindersManager();
        remindersManager.setJDA(jda);
    }
}