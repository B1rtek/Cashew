package com.birtek.cashew;

import com.birtek.cashew.commands.*;
import com.birtek.cashew.events.CountingMessageDeletionDetector;
import com.birtek.cashew.events.CountingMessageModificationDetector;
import com.birtek.cashew.events.GuildMemberJoinAndLeave;
import com.birtek.cashew.events.GuildMessageReactionAdd;
import com.birtek.cashew.messagereactions.Counter;
import com.birtek.cashew.messagereactions.OwosEtc;
import com.birtek.cashew.messagereactions.ReactToMaple;
import com.birtek.cashew.timings.TimedMessagesManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.text.ParseException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Cashew {

    public static String COMMAND_PREFIX = "$";
    public static String CASHEW_USER_ID = "856980494175174747";
    public static String BIRTEK_USER_ID = "288000870187139073";
    public static String NEKOPARA_EMOTES_UWU_SERVER_ID = "852811110158827530";
    public static String PI_SERVER_ID = "848907956379582484";
    public static TimedMessagesManager timedMessagesManager;

    public static void main(String[] args) throws LoginException, ParseException {

        KeyLoader keyLoader = new KeyLoader();
        String TOKEN;
        if (keyLoader.loadKey()) {
            TOKEN = keyLoader.getKey();
        } else {
            System.err.println("The API key is missing.");
            return;
        }
        JDA jda = JDABuilder.createDefault(TOKEN)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("NEKOPARA Vol. 3"))
                .setCompression(Compression.NONE)
                .addEventListeners(new Help(), new Clear(), new BestNeko(), new Nekoichi(), new Reactions(), new BoBurnham(), new OpenCase(), new OpenCollection(), new TimedMessageCommand(),
                        new Cuddle(), new Hug(), new Kiss(), new Pat(), new SocialCredit(), new Korwin(), new Inspirobot(), new DadJoke(), new Counting(), new Ping(), new ChoccyMilk(), //commands
                        new GuildMemberJoinAndLeave(), new GuildMessageReactionAdd(), new CountingMessageDeletionDetector(), new CountingMessageModificationDetector(), //events
                        new ReactToMaple(), new OwosEtc(), new Counter()) //messagereations
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("ping", "Measures bot's ping"),
                Commands.slash("help", "Shows the help embed containing information about commands")
                        .addOption(STRING, "command", "Command to show help of"),
                Commands.slash("info", "Shows the help embed containing information about commands")
                        .addOption(STRING, "command", "Command to show help of"),
                Commands.slash("bestneko", "Sends you a gif of the best neko <3"),
                Commands.slash("boburnham", "Sends you a random quote from Bo Burnham's songs")
                        .addOption(STRING, "nsfw", "Decide whether you want an nsfw quote or not", false, true),
                Commands.slash("dadjoke", "Sends you a random dad joke from icanhazdadjoke.com"),
                Commands.slash("inspirobot", "Sends you an inspiring quote from inspirobot.me"),
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
                Commands.slash("choccymilk", "Gift someone some Choccy Milk!"),
                Commands.slash("cuddle", "Cuddle someone!")
                        .addOption(STRING, "tocuddle", "A person (or a group of people) to cuddle")
                ).queue();
        timedMessagesManager = new TimedMessagesManager(jda); //initiate timed messages
    }
}