package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.messagereactions.ReactToMaple;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class BestNeko extends BaseCommand {

    Permission[] bestNekoCommandPermissions = {
            Permission.MESSAGE_WRITE
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "bestneko")) {
            if(checkPermissions(event, bestNekoCommandPermissions)) {
                event.getChannel().sendMessage("Maple Minaduki <3").queue();
                ReactToMaple reactToMaple = new ReactToMaple();
                reactToMaple.sendTheBestNekoGif(event);
            }
        }
    }
}