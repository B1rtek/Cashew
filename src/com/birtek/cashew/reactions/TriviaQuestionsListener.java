package com.birtek.cashew.reactions;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TriviaQuestionsListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(Cashew.triviaQuestionsManager.isBeingPlayedIn(event.getChannel().getId())) {
            if(Cashew.triviaQuestionsManager.checkAnswer(event.getAuthor().getId(), event.getMessage().getContentRaw().toLowerCase(Locale.ROOT))) {
                event.getMessage().reply("Correct!").mentionRepliedUser(false).queue();
            }
        }
    }
}
