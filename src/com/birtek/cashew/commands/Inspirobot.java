package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Inspirobot extends BaseCommand {

    Permission[] inspirobotCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "inspirobot") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "insp")) {
            if (checkPermissions(event, inspirobotCommandPermissions)) {
                URL inspirobot;
                try {
                    inspirobot = new URL("https://inspirobot.me/api?generate=true");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    event.getMessage().reply("Something went wrong while performing the command...").mentionRepliedUser(false).queue();
                    return;
                }
                URLConnection inspirobotURLConnection;
                String quote;
                try {
                    inspirobotURLConnection = inspirobot.openConnection();
                    quote = readURL(inspirobotURLConnection);
                } catch (IOException e) {
                    e.printStackTrace();
                    event.getMessage().reply("Something went wrong while requesting the quote :thinking:").mentionRepliedUser(false).queue();
                    return;
                }
                event.getMessage().reply(quote).mentionRepliedUser(false).queue();
            }
        }
    }
}
