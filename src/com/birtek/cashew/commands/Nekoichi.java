package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Nekoichi extends BaseCommand {

    String[] nekoichi = {
            "ずっと大切だよ いつだって隣にいるよ",
            "同じ時間の中で そっと寄り添っていたい"
    };

    Permission[] nekoichiCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "nekoichi")) {
            if(checkPermissions(event, nekoichiCommandPermissions)) {
                for(String line:nekoichi) {
                    event.getChannel().sendMessage(line).queue();
                    try {
                        Thread.sleep(8 * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}