package com.birtek.cashew.events;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GuildMessageReceived extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        List<MessageEmbed> messageEmbeds = event.getMessage().getEmbeds();
        if(!messageEmbeds.isEmpty() && event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            if(Objects.requireNonNull(messageEmbeds.get(0).getTitle()).contains("successfully deleted")) {
                event.getMessage().addReaction("❌").queue();
            }
        }
    }
}