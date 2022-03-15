package com.birtek.cashew.events;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.birtek.cashew.commands.BaseCommand.verifyCommonPermissionSubset;

public class GuildMessageReactionAdd extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getReactionEmote().getName().equals("❌") && !Objects.requireNonNull(event.getUser()).getId().equals(Cashew.CASHEW_USER_ID)) {
            Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            List<MessageEmbed> messageEmbeds = message.getEmbeds();
            for (MessageEmbed embed : messageEmbeds) {
                try {
                    if (Objects.requireNonNull(embed.getDescription()).equals("React with ❌ to delete this message")) {
                        if (checkPermissions(event, adminPermissions)) {
                            event.getChannel().retrieveMessageById(event.getMessageId()).complete().delete().queue();
                        } else {
                            event.getReaction().removeReaction(event.getUser()).queue();
                        }
                    }
                    break;
                } catch (Exception ignored) {
                    
                }
            }
        }
    }

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public static boolean checkPermissions(MessageReactionAddEvent event, Permission[] neededPermissions) {
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        if (Objects.requireNonNull(event.getUser()).getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        return verifyCommonPermissionSubset(neededPermissions, permissionsSet, event.getUser());
    }
}