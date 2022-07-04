package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Poll extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Poll.class);

    private static final ArrayList<Emoji> optionEmoji = new ArrayList<>() {
        {
            add(Emoji.fromUnicode("1️⃣"));
            add(Emoji.fromUnicode("2️⃣"));
            add(Emoji.fromUnicode("3️⃣"));
            add(Emoji.fromUnicode("4️⃣"));
            add(Emoji.fromUnicode("5️⃣"));
        }
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("poll") && checkSlashCommandPermissions(event, manageServerPermission)) {
            EmbedBuilder pollEmbed = new EmbedBuilder();
            String pollTitle = event.getOption("title", null, OptionMapping::getAsString);
            if(pollTitle == null) {
                event.reply("Poll must have a title").setEphemeral(true).queue();
                return;
            }
            ArrayList<String> options = new ArrayList<>();
            for(int i=1; i<=5; i++) {
                String optionName = "option"+i;
                String option = event.getOption(optionName, null, OptionMapping::getAsString);
                if(option != null) options.add(option);
            }
            if(options.size() < 2) {
                event.reply("Poll must have at least two options").setEphemeral(true).queue();
                return;
            }
            pollEmbed.setTitle("Poll: " + pollTitle);
            for(int i=0; i<options.size(); i++) {
                pollEmbed.addField(optionEmoji.get(i).getName(), options.get(i), false);
            }
            pollEmbed.setFooter("Ends at");
            pollEmbed.setTimestamp(Instant.now());
            event.reply("Poll created!").setEphemeral(true).queue();
            event.getChannel().sendMessageEmbeds(pollEmbed.build()).queue(pollMessage -> {
                for(int i=0; i<options.size(); i++) {
                    pollMessage.addReaction(optionEmoji.get(i)).queue();
                }
            });
        }
    }
}
