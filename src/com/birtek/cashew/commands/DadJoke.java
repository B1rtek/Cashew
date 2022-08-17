package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DadJoke extends BaseCommand {

    /**
     * Creates a request for icanhazdadjoke.com and receives the reply from the api
     *
     * @return text of the joke gotten from icanhazdadjoke.com, or an error message, both will be displayed in the same
     * way
     */
    private String getADadJoke() {
        URL dadJokeURL;
        try {
            dadJokeURL = new URL("https://icanhazdadjoke.com/");
        } catch (MalformedURLException e) {
            return "Something went wrong while performing the command...";
        }
        URLConnection dadJokeConnection;
        try {
            dadJokeConnection = dadJokeURL.openConnection();
            dadJokeConnection.addRequestProperty("Accept", "text/plain");
            return readURL(dadJokeConnection);
        } catch (IOException e) {
            return "Something went wrong while requesting the joke :thinking:";
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "dadjoke")) {
            if(cantBeExecutedPrefix(event, "dadjoke", false)) {
                event.getMessage().reply("This command is turned off in this channel").mentionRepliedUser(false).queue();
                return;
            }
            event.getMessage().reply(getADadJoke()).mentionRepliedUser(false).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("dadjoke")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            event.reply(getADadJoke()).queue();
        }
    }
}
