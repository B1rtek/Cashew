package com.birtek.cashew;

import com.birtek.cashew.commands.*;
import com.birtek.cashew.events.CountingMessageDeletionDetector;
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
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.text.ParseException;

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
                        new Cuddle(), new Hug(), new Kiss(), new Pat(), new SocialCredit(), new Korwin(), new Inspirobot(), new DadJoke(), new Counting(), //commands
                        new GuildMemberJoinAndLeave(), new GuildMessageReactionAdd(), new CountingMessageDeletionDetector(), //events
                        new ReactToMaple(), new OwosEtc(), new Counter()) //messagereations
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        timedMessagesManager = new TimedMessagesManager(jda); //initiate timed messages
    }
}