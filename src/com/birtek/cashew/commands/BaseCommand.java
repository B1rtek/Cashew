package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.EnumSet;
import java.util.Objects;

public class BaseCommand extends ListenerAdapter {

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public static boolean checkPermissions(GuildMessageReceivedEvent event, Permission[] neededPermissions) {
        if(event.isWebhookMessage()) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        if(event.getAuthor().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        if(event.getAuthor().isBot() || event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for(Permission neededPermission:neededPermissions) {
            if(!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}