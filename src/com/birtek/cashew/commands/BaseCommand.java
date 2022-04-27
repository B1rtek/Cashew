package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

public class BaseCommand extends ListenerAdapter {

    public Permission[] adminPermissions = {
            Permission.ADMINISTRATOR
    };

    public Permission[] moderateMembersPermission = {
            Permission.MODERATE_MEMBERS
    };

    public Permission[] manageServerPermission = {
            Permission.MANAGE_SERVER
    };

    public static boolean checkPermissions(MessageReceivedEvent event, Permission[] neededPermissions) {
        if (event.isWebhookMessage()) {
            return true;
        }
        if (event.isFromType(ChannelType.PRIVATE)) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        if (event.getAuthor().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        if (event.getAuthor().isBot() || event.getAuthor().getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for (Permission neededPermission : neededPermissions) {
            if (!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSlashCommandPermissions(SlashCommandInteractionEvent event, Permission[] neededPermissions) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return true;
        }
        if (event.getUser().getId().equals(Cashew.BIRTEK_USER_ID)) {
            return true;
        }
        EnumSet<Permission> permissionsSet = Objects.requireNonNull(event.getMember()).getPermissions();
        return verifyCommonPermissionSubset(neededPermissions, permissionsSet, event.getUser());
    }

    public static boolean verifyCommonPermissionSubset(Permission[] neededPermissions, EnumSet<Permission> permissionsSet, User user) {
        if (user.isBot() || user.getId().equals(Cashew.CASHEW_USER_ID)) {
            return false;
        }
        for (Permission neededPermission : neededPermissions) {
            if (!(permissionsSet.contains(neededPermission) || permissionsSet.contains(Permission.ADMINISTRATOR))) {
                return false;
            }
        }
        return true;
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        if(strNum.isEmpty()) {
            return true;
        }
        int start = 0;
        if (strNum.charAt(0) == '-') {
            start = 1;
        }
        for (int i = start; i < strNum.length(); i++) {
            if (strNum.charAt(i) > '9' || strNum.charAt(i) < '0') {
                return false;
            }
        }
        return true;
    }

    public String readURL(URLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

    public static ArrayList<String> autocompleteFromList(ArrayList<String> options, String typed) {
        ArrayList<String> matching = new ArrayList<>();
        for (String option : options) {
            if(option.toLowerCase().contains(typed.toLowerCase(Locale.ROOT))) {
                matching.add(option);
            }
        }
        if(matching.size() > 25) {
            matching = new ArrayList<>() {
                {
                    add("There's more than 25 matching options");
                }
            };
        }
        return matching;
    }
}