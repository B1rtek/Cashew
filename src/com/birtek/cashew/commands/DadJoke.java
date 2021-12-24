package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DadJoke extends BaseCommand {

    Permission[] dadJokeCommandPermissions = {Permission.MESSAGE_WRITE};

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "dadjoke")) {
            if (checkPermissions(event, dadJokeCommandPermissions)) {
                URL dadJokeURL;
                try {
                    dadJokeURL = new URL("https://icanhazdadjoke.com/");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    event.getMessage().reply("Something went wrong while performing the command...").mentionRepliedUser(false).queue();
                    return;
                }
                URLConnection dadJokeConnection;
                String joke;
                try {
                    dadJokeConnection = dadJokeURL.openConnection();
                    dadJokeConnection.addRequestProperty("Accept", "text/plain");
                    joke = readURL(dadJokeConnection);
                } catch (IOException e) {
                    e.printStackTrace();
                    event.getMessage().reply("Something went wrong while requesting the joke :thinking:").mentionRepliedUser(false).queue();
                    return;
                }
                event.getMessage().reply(joke).mentionRepliedUser(false).queue();
            }
        }
    }
}
