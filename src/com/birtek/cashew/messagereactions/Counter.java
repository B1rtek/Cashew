package com.birtek.cashew.messagereactions;

import com.birtek.cashew.Database;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import kotlin.Pair;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class Counter extends BaseReaction {

    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        if(message.isEmpty()) {
            return;
        }
        Database database = Database.getInstance();
        CountingInfo countingData = database.getCountingData(event.getChannel().getId());
        if(countingData.getActive() && !Objects.equals(countingData.getUserID(), event.getAuthor().getId())) {
            ExpressionBuilder test = new ExpressionBuilder(message);
            int result, current = countingData.getValue();
            try {
                result = (int)Math.round(test.build().calculate());
            } catch (UnknownFunctionException e) {
                e.printStackTrace();
                System.err.println("Something weird happened idk counting failed");
                return;
            } catch (UnparsableExpressionException e) {
                return;
            } catch (ArithmeticException e) {
                event.getMessage().reply("This is illegal bruh").mentionRepliedUser(false).queue();
                return;
            }
            if(result == 0) {
                return;
            }
            if(result != current + 1) {
                event.getChannel().sendMessage("Wrong number! Counter reset!").queue();
                database.setCount(new CountingInfo(true, " ", 0), event.getChannel().getId());
            } else {
                event.getMessage().addReaction("✅").queue();
                database.setCount(new CountingInfo(true, event.getAuthor().getId(), result), event.getChannel().getId());
            }
        }
    }
}
