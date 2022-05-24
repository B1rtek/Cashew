package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Objects;

public class Counter extends BaseReaction {

    private boolean checkIfTypo(String message, double target) {
        // generate all possible typos
        ArrayList<String> typos = new ArrayList<>();
        for (int i = 0; i < message.length(); i++) {
            StringBuilder newTypo = new StringBuilder(message);
            switch (message.charAt(i)) {
                case '0' -> {
                    newTypo.setCharAt(i, '9');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '2');
                    typos.add(newTypo.toString());
                }
                case '1' -> {
                    newTypo.setCharAt(i, '0');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '3');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '2');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '4');
                    typos.add(newTypo.toString());
                }
                case '2' -> {
                    newTypo.setCharAt(i, '0');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '3');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '1');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '5');
                    typos.add(newTypo.toString());
                }
                case '3' -> {
                    newTypo.setCharAt(i, '2');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '6');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '4');
                    typos.add(newTypo.toString());
                }
                case '4' -> {
                    newTypo.setCharAt(i, '1');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '3');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '7');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '5');
                    typos.add(newTypo.toString());
                }
                case '5' -> {
                    newTypo.setCharAt(i, '2');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '6');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '8');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '4');
                    typos.add(newTypo.toString());
                }
                case '6' -> {
                    newTypo.setCharAt(i, '5');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '3');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '9');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '7');
                    typos.add(newTypo.toString());
                }
                case '7' -> {
                    newTypo.setCharAt(i, '4');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '8');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '6');
                    typos.add(newTypo.toString());
                }
                case '8' -> {
                    newTypo.setCharAt(i, '7');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '9');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '5');
                    typos.add(newTypo.toString());
                }
                case '9' -> {
                    newTypo.setCharAt(i, '0');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '8');
                    typos.add(newTypo.toString());
                    newTypo.setCharAt(i, '6');
                    typos.add(newTypo.toString());
                }
            }
        }
        for (String typo : typos) { // check if any typo matches
            ExpressionBuilder test = new ExpressionBuilder(typo);
            try {
                double result = test.build().evaluate();
                if (result == target) {
                    return true;
                }
            } catch (IllegalArgumentException | EmptyStackException | ArithmeticException ignored) {
            }
        }
        return false; // no matches were found
    }

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
        if (countingData.active() && !Objects.equals(countingData.userID(), event.getAuthor().getId())) {
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
            double result, current = countingData.value();
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
                if (checkIfTypo(message, current + 1)) {
                    int typosLeft = countingData.typosLeft() - 1;
                    event.getMessage().addReaction("⚠️").queue();
                    String plural = typosLeft == 1 ? "" : "s";
                    event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + "> made a typo, but the count was saved. Count has been corrected to ` " + (int) (current + 1) + " `, ` " + typosLeft + " ` typo" + plural + " left!").queue();
                    database.setCount(new CountingInfo(true, event.getAuthor().getId(), (int) current + 1, event.getMessageId(), typosLeft), event.getChannel().getId());
                } else {
                    double rounded = Math.round(result);
                    String toOutput = String.valueOf(result);
                    if (rounded == result) {
                        toOutput = String.valueOf((int) result);
                    }
                    event.getMessage().addReaction("❌").queue();
                    event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + "> screwed up by writing ` " + toOutput + " `! The next number should have been ` " + (int) (current + 1) + " `! Counter has been reset!").queue();
                    database.setCount(new CountingInfo(true, " ", 0, " ", 3), event.getChannel().getId());

                }
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
                database.setCount(new CountingInfo(true, event.getAuthor().getId(), (int) result, event.getMessageId(), countingData.typosLeft()), event.getChannel().getId());
            }
        }
    }
}
