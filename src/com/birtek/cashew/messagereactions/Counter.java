package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Objects;

public class Counter extends BaseReaction {

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!checkIfNotBot(event) || event.isWebhookMessage()) {
            return;
        }
        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        if (message.isEmpty()) {
            return;
        }
        Database database = Database.getInstance();
        CountingInfo countingData = database.getCountingData(event.getChannel().getId());
        if (countingData.getActive() && !Objects.equals(countingData.getUserID(), event.getAuthor().getId())) {
            message = message.replace(',', '.');
            ExpressionBuilder test = new ExpressionBuilder(message);
            int result, current = countingData.getValue();
            try {
                result = (int) Math.round(test.build().calculate());
            } catch (UnknownFunctionException e) {
                e.printStackTrace();
                System.err.println("Something weird happened idk counting failed");
                return;
            } catch (UnparsableExpressionException | EmptyStackException e) {
                return;
            } catch (ArithmeticException e) {
                event.getMessage().reply("This is illegal bruh").mentionRepliedUser(false).queue();
                return;
            }
            if (result == 0) {
                return;
            }
            if (result != current + 1) {
                event.getMessage().addReaction("❌").queue();
                event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + "> screwed up! The next number should have been ` " + (current + 1) + " `! Counter has been reset!").queue();
                database.setCount(new CountingInfo(true, " ", 0, " "), event.getChannel().getId());
            } else {
                String reactionEmote = "✅";
                if (result == 69) {
                    reactionEmote = "♋";
                } else if (result == 100) {
                    reactionEmote = "\uD83D\uDCAF";
                } else if (result == 420 || result == 2137) {
                    reactionEmote = "\uD83D\uDE01";
                } else if (result == 1337) {
                    reactionEmote = "\uD83D\uDE0E";
                }
                event.getMessage().addReaction(reactionEmote).queue();
                database.setCount(new CountingInfo(true, event.getAuthor().getId(), result, event.getMessageId()), event.getChannel().getId());
            }
        }
    }
}
