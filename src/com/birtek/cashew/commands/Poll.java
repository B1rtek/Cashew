package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Poll extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Poll.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("poll")) {
            EmbedBuilder pollEmbed = new EmbedBuilder();
            pollEmbed.setTimestamp(Instant.now());
            event.replyEmbeds(pollEmbed.build()).queue();
        }
    }
}
