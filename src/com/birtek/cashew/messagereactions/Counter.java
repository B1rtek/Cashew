package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Checks;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Counter extends BaseReaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Counter.class);

    private HashMap<String, Boolean> correctedAfterOffline = new HashMap<>();

    public Counter() {
        Database database = Database.getInstance();
        ArrayList<String> countingChannels = database.getAllActiveCountingChannels();
        if (countingChannels != null && !countingChannels.isEmpty()) {
            for (String channel : countingChannels) {
                correctedAfterOffline.put(channel, false);
            }
        } else {
            LOGGER.error("Failed to obtain channels list!");
        }
    }

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
                    newTypo.setCharAt(i, '1');
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

    private String prepareMessage(String message) {
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
        return message;
    }

    private enum MessageAnalysisResultType {
        CORRECT, INCORRECT, TYPO, ERROR, DIV0
    }

    private record MessageAnalysisResult(MessageAnalysisResultType type, double result) {
    }

    private MessageAnalysisResult analyzeMessage(String message, int current) {
        ExpressionBuilder test = new ExpressionBuilder(message);
        double result;
        try {
            result = test.build().evaluate();
        } catch (IllegalArgumentException | EmptyStackException e) {
            return new MessageAnalysisResult(MessageAnalysisResultType.ERROR, 0);
        } catch (ArithmeticException e) {
            return new MessageAnalysisResult(MessageAnalysisResultType.DIV0, 0);
        }
        if (result == 0) {
            return new MessageAnalysisResult(MessageAnalysisResultType.ERROR, 0);
        }
        if (result != current + 1) {
            if (checkIfTypo(message, current + 1)) {
                return new MessageAnalysisResult(MessageAnalysisResultType.TYPO, current);
            } else {
                return new MessageAnalysisResult(MessageAnalysisResultType.INCORRECT, result);
            }
        } else {
            return new MessageAnalysisResult(MessageAnalysisResultType.CORRECT, current + 1);
        }
    }

    private boolean handleTypo(Message message, double previous, int typosLeft) {
        if (typosLeft <= 0) return false;
        int newTyposLeft = typosLeft - 1;
        message.addReaction("⚠️").queue();
        String plural = newTyposLeft == 1 ? "" : "s";
        message.getChannel().sendMessage("<@!" + message.getAuthor().getId() + "> made a typo, but the count was saved. Count has been corrected to ` " + (int) (previous + 1) + " `, **" + newTyposLeft + "** typo" + plural + " left! The next number is ` " + (int) (previous + 2) + " `!").queue();
        return true;
    }

    private void handleCorrectCount(Message message, double result, int typosLeft) {
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
        message.addReaction(reactionEmote).queue();
    }

    private void handleIncorrectCount(Message message, double result, int current) {
        double rounded = Math.round(result);
        String toOutput = String.valueOf(result);
        if (rounded == result) {
            toOutput = String.valueOf((int) result);
        }
        message.addReaction("❌").queue();
        message.getChannel().sendMessage("<@!" + message.getAuthor().getId() + "> screwed up by writing ` " + toOutput + " `! The next number should have been ` " + (current + 1) + " `! Counter has been reset!").queue();
    }

    private void updateCountingDatabase(CountingInfo newInfo, String channelID) {
        Database database = Database.getInstance();
        database.setCount(newInfo, channelID);
    }

    private boolean correctOfflineCount(MessageChannel channel, CountingInfo lastCount) {
        List<Message> messageHistory = null;
        try {
            messageHistory = channel.getHistoryAfter(lastCount.messageID(), 100).complete().getRetrievedHistory();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (messageHistory.isEmpty()) return false;
        int currentCount = lastCount.value(), currentTypos = lastCount.typosLeft();
        String lastCounterID = lastCount.userID();
        for (int i = messageHistory.size()-1; i>=0; i--) {
            Message messageFromHistory = messageHistory.get(i);
            if(Objects.equals(lastCounterID, messageFromHistory.getAuthor().getId())) continue;
            String message = prepareMessage(messageFromHistory.getContentDisplay().toLowerCase(Locale.ROOT));
            MessageAnalysisResult analysisResult = analyzeMessage(message, currentCount);
            switch (analysisResult.type()) {
                case DIV0 -> messageFromHistory.reply("This is illegal bruh").mentionRepliedUser(false).queue();
                case CORRECT -> handleCorrectCount(messageFromHistory, analysisResult.result(), currentTypos);
                case INCORRECT -> {
                    handleIncorrectCount(messageFromHistory, analysisResult.result(), currentCount);
                    updateCountingDatabase(new CountingInfo(true, " ", 0, " ", 3), channel.getId());
                    return true;
                }
                case TYPO -> {
                    if (!handleTypo(messageFromHistory, analysisResult.result(), currentTypos)) {
                        handleIncorrectCount(messageFromHistory, analysisResult.result(), currentCount);
                        updateCountingDatabase(new CountingInfo(true, " ", 0, " ", 3), channel.getId());
                        return true;
                    }
                    --currentTypos;
                }
            }
            ++currentCount;
            lastCounterID = messageFromHistory.getAuthor().getId();
        }
        updateCountingDatabase(new CountingInfo(true, messageHistory.get(0).getAuthor().getId(), currentCount, messageHistory.get(0).getId(), currentTypos), channel.getId());
        return true;
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
            message = prepareMessage(message);
            MessageAnalysisResult analysisResult = analyzeMessage(message, countingData.value());
            switch (analysisResult.type()) {
                case ERROR -> {
                    // do absolutely nothing
                }
                case DIV0 -> event.getMessage().reply("This is illegal bruh").mentionRepliedUser(false).queue();
                case CORRECT -> {
                    correctedAfterOffline.put(event.getChannel().getId(), true);
                    handleCorrectCount(event.getMessage(), analysisResult.result(), countingData.typosLeft());
                    updateCountingDatabase(new CountingInfo(true, event.getAuthor().getId(), (int) analysisResult.result(), event.getMessageId(), countingData.typosLeft()), event.getChannel().getId());
                }
                case INCORRECT -> {
                    if (correctedAfterOffline.get(event.getChannel().getId())) {
                        handleIncorrectCount(event.getMessage(), analysisResult.result(), countingData.value());
                        updateCountingDatabase(new CountingInfo(true, " ", 0, " ", 3), event.getChannel().getId());
                    } else {
                        if(correctOfflineCount(event.getChannel(), countingData)) {
                            correctedAfterOffline.put(event.getChannel().getId(), true);
                        }
                    }
                }
                case TYPO -> {
                    correctedAfterOffline.put(event.getChannel().getId(), true);
                    if (!handleTypo(event.getMessage(), analysisResult.result(), countingData.typosLeft())) {
                        handleIncorrectCount(event.getMessage(), analysisResult.result(), countingData.value());
                        updateCountingDatabase(new CountingInfo(true, " ", 0, " ", 3), event.getChannel().getId());
                    } else {
                        updateCountingDatabase(new CountingInfo(true, event.getAuthor().getId(), (int) (analysisResult.result() + 1), event.getMessageId(), countingData.typosLeft() - 1), event.getChannel().getId());
                    }
                }
            }
        }
    }
}
