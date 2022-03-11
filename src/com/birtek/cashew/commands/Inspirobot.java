package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Inspirobot extends BaseCommand {

    Permission[] inspirobotCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    private String getAnInspirobotQuote() {
        URL inspirobot;
        try {
            inspirobot = new URL("https://inspirobot.me/api?generate=true");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Something went wrong while performing the command...";
        }
        URLConnection inspirobotURLConnection;
        try {
            inspirobotURLConnection = inspirobot.openConnection();
            return readURL(inspirobotURLConnection);
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong while requesting the quote :thinking:";
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "inspirobot") || args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "insp")) {
            if (checkPermissions(event, inspirobotCommandPermissions)) {
                event.getMessage().reply(getAnInspirobotQuote()).mentionRepliedUser(false).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("inspirobot") || event.getName().equals("insp")) {
            if(checkSlashCommandPermissions(event, inspirobotCommandPermissions)) {
                event.reply(getAnInspirobotQuote()).queue();
            }
        }
    }
}
