package com.birtek.cashew.reactions;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.WhenRule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WhenExecutor extends ListenerAdapter {

    /**
     * Detects the first trigger - member joining a server
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 1);
        for (WhenRule rule : rules) {
            if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                EmbedBuilder embedToPass = new EmbedBuilder();
                embedToPass.setTitle("User joined the server " + event.getGuild().getName());
                embedToPass.setDescription(event.getUser().getName() + " (" + event.getUser().getId() + ")");
                performPassAction(rule, event.getGuild(), embedToPass.build());
            } else {
                performAction(rule, event.getGuild(), null, event.getUser());
            }
        }
    }

    /**
     * Detects the second trigger - member leaving a server
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 2);
        for (WhenRule rule : rules) {
            if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                EmbedBuilder embedToPass = new EmbedBuilder();
                embedToPass.setTitle("User left the server " + event.getGuild().getName());
                embedToPass.setDescription(event.getUser().getName() + " (" + event.getUser().getId() + ")");
                performPassAction(rule, event.getGuild(), embedToPass.build());
            } else {
                performAction(rule, event.getGuild(), null, event.getUser());
            }
        }
    }

    /**
     * Detects the third trigger - member adding a reaction to a message
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 3);
        for (WhenRule rule : rules) {
            if (event.getMessageId().equals(rule.getSourceMessageID()) && event.getEmoji().getAsReactionCode().equals(rule.getSourceReaction())) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("User " + event.retrieveUser().complete().getName() + " reacted with " + rule.getSourceReaction());
                    embedToPass.setDescription("In server " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");
                    embedToPass.addField("Message link", event.retrieveMessage().complete().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getMember(), null);
                }
            }
        }
    }

    /**
     * Detects the fourth trigger - member removing a reaction to a message
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 4);
        for (WhenRule rule : rules) {
            if (event.getMessageId().equals(rule.getSourceMessageID()) && event.getEmoji().getAsReactionCode().equals(rule.getSourceReaction())) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("User " + event.retrieveUser().complete().getName() + " removed a reaction " + rule.getSourceReaction());
                    embedToPass.setDescription("In server " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");
                    embedToPass.addField("Message link", event.retrieveMessage().complete().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getMember(), null);
                }
            }
        }
    }

    /**
     * Detects the fifth trigger - member editing their message
     */
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 5);
        for (WhenRule rule : rules) {
            if ((rule.getSourceChannelID() == null) || (rule.getSourceChannelID().equals(event.getChannel().getId()))) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("User " + event.getAuthor().getId() + " edited their message");
                    embedToPass.setDescription("In server " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");
                    embedToPass.addField("New content", event.getMessage().getContentRaw(), false);
                    embedToPass.addField("Message link", event.getMessage().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getMember(), null);
                }
            }
        }
    }

    /**
     * Detects the sixth trigger - member deleting their message
     */
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 6);
        for (WhenRule rule : rules) {
            if ((rule.getSourceChannelID() == null) || (rule.getSourceChannelID().equals(event.getChannel().getId()))) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("A message in channel " + event.getChannel().getName() + " (" + event.getChannel().getId() + ") was deleted");
                    embedToPass.setDescription("In server " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");
                } else {
                    performAction(rule, event.getGuild(), null, null);
                }
            }
        }
    }

    /**
     * Performs actions that don't involve passing information about the event somewhere
     *
     * @param rule   {@link WhenRule WhenRule} which specifies what needs to be done
     * @param server {@link Guild server} on which the action is going to be performed
     * @param member {@link Member member} of the server who might be affected by the action
     */
    private void performAction(WhenRule rule, Guild server, Member member, User user) {
        switch (rule.getActionType()) {
            case 1 -> {
                TextChannel targetChannel = server.getChannelById(TextChannel.class, rule.getTargetChannelID());
                actionSendMessage(rule.getTargetMessageContent(), targetChannel, user);
            }
            case 2 -> {
                if (member == null) return;
                actionAddRole(member, server, rule.getTargetRoleID());
            }
            case 3 -> {
                if (member == null) return;
                actionRemoveRole(member, server, rule.getTargetRoleID());
            }
        }
    }

    /**
     * Performs actions that involve passing information about the event somewhere
     *
     * @param rule        {@link WhenRule WhenRule} which specifies what needs to be done
     * @param server      {@link Guild server} where the action was triggered
     * @param embedToPass {@link MessageEmbed embed} that will be passed somewhere
     */
    private void performPassAction(WhenRule rule, Guild server, MessageEmbed embedToPass) {
        if (rule.getActionType() == 4) {
            User targetUser = server.getJDA().getUserById(rule.getTargetUserID());
            passToDM(embedToPass, targetUser);
        } else {
            TextChannel targetChannel = server.getChannelById(TextChannel.class, rule.getTargetChannelID());
            passToChannel(embedToPass, targetChannel);
        }
    }

    /**
     * Performs the first action - sends a message to the specified message channel
     *
     * @param messageContent content of the message to send, with all "@user"s replaced with a mention of the user
     * @param targetChannel  {@link TextChannel channel} in which the message will be sent
     * @param user           {@link User user} whose name will be placed in the message, replacing all "@user" occurrences
     */
    private void actionSendMessage(String messageContent, TextChannel targetChannel, User user) {
        if (targetChannel == null) return;
        messageContent = messageContent.replace("@user", user.getAsMention());
        targetChannel.sendMessage(messageContent).queue();
    }

    /**
     * Performs the second action - adds a role to a member
     *
     * @param targetMember {@link Member member} who will get a role assigned
     * @param server       {@link Guild server} on which the role assignment will take place
     * @param targetRoleID ID of the role to assign, if the role doesn't exist, nothing will happen
     */
    private void actionAddRole(Member targetMember, Guild server, String targetRoleID) {
        Role roleToAdd = server.getRoleById(targetRoleID);
        if (roleToAdd == null) return;
        server.addRoleToMember(targetMember.getUser(), roleToAdd).queue();
    }

    /**
     * Performs the third action - removes a member's role
     *
     * @param targetMember {@link Member member} who will get a role removed
     * @param server       {@link Guild server} on which the role removal will take place
     * @param targetRoleID ID of the role to remove, if the role doesn't exist, nothing will happen
     */
    private void actionRemoveRole(Member targetMember, Guild server, String targetRoleID) {
        Role roleToAdd = server.getRoleById(targetRoleID);
        if (roleToAdd == null) return;
        server.removeRoleFromMember(targetMember.getUser(), roleToAdd).queue();
    }

    /**
     * Passes the generated event embed to someone's DM
     *
     * @param embedToPass {@link MessageEmbed embed} with details about the event
     * @param targetUser  {@link User user} to whom the embed will be sent
     */
    private void passToDM(MessageEmbed embedToPass, User targetUser) {
        if (targetUser == null) return;
        try {
            targetUser.openPrivateChannel().complete().sendMessageEmbeds(embedToPass).queue();
        } catch (Exception ignored) {
        }
    }

    /**
     * Passes the generated event embed to a server text channel
     *
     * @param embedToPass   {@link MessageEmbed embed} with details about the event
     * @param targetChannel {@link TextChannel channel} in which the embed will be sent
     */
    private void passToChannel(MessageEmbed embedToPass, TextChannel targetChannel) {
        if (targetChannel == null) return;
        targetChannel.sendMessageEmbeds(embedToPass).queue();
    }
}
