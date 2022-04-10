package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Objects;

public class Counter extends BaseReaction {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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
            // C++ patch lmao
            if (message.length() > 2) {
                if (message.startsWith("++")) {
                    message = "1+" + message.substring(2);
                } else if (message.startsWith("--")) {
                    message = "-1+" + message.substring(2);
                }
                String substring = message.substring(0, message.length() - 2);
                if (message.endsWith("++")) {
                    message = substring + "+1";
                } else if (message.endsWith("--")) {
                    message = substring + "-1";
                }
            }
            ExpressionBuilder test = new ExpressionBuilder(message);
            double result, current = countingData.getValue();
            try {
                result = test.build().evaluate();
            } catch (IllegalArgumentException | EmptyStackException e) {
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
                event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + "> screwed up by writing ` " + result + " `! The next number should have been ` " + (int) (current + 1) + " `! Counter has been reset!").queue();
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
                database.setCount(new CountingInfo(true, event.getAuthor().getId(), (int) result, event.getMessageId()), event.getChannel().getId());
            }
        }
    }
}
