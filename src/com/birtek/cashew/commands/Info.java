package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Info extends BaseCommand {

    MessageEmbed createInfoEmbed(Event event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(0xA86A61);
        embedBuilder.setTitle("Cashew");
        embedBuilder.setDescription("Nekopara bot with fun commands by B1rtek#2383");
        embedBuilder.addField("Commands", "To display all commands, type `/help` or `"+ Cashew.COMMAND_PREFIX +"help`", false);
        embedBuilder.addField("Support", "DM me `B1rtek#2383`", false);
        embedBuilder.addField("Bot development", "Cashew's trello board: https://trello.com/b/R432WEsW/cashew-bot", false);
//        embedBuilder.addField("Github repo", "soon :tm:", false);
        embedBuilder.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        return embedBuilder.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("info")) {
            event.replyEmbeds(createInfoEmbed(event)).setEphemeral(false).queue();
        }
    }
}
