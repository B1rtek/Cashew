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
        if(event.getUser().getId().equals(event.getJDA().getSelfUser().getId())) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 1);
        for (WhenRule rule : rules) {
            if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                EmbedBuilder embedToPass = new EmbedBuilder();
                String title = "User joined the server" + (rule.getActionType() == 5 ? "" : " " + event.getGuild().getName());
                embedToPass.setTitle(title);
                embedToPass.setDescription(event.getUser().getAsMention() + " (" + event.getUser().getId() + ")");
                performPassAction(rule, event.getGuild(), embedToPass.build());
            } else {
                performAction(rule, event.getGuild(), event.getUser());
            }
        }
    }

    /**
     * Detects the second trigger - member leaving a server
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if(event.getUser().getId().equals(event.getJDA().getSelfUser().getId())) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 2);
        for (WhenRule rule : rules) {
            if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                EmbedBuilder embedToPass = new EmbedBuilder();
                String title = "User left the server" + (rule.getActionType() == 5 ? "" : " " + event.getGuild().getName());
                embedToPass.setTitle(title);
                embedToPass.setDescription(event.getUser().getAsMention() + " (" + event.getUser().getId() + ")");
                performPassAction(rule, event.getGuild(), embedToPass.build());
            } else {
                performAction(rule, event.getGuild(), event.getUser());
            }
        }
    }

    /**
     * Detects the third trigger - member adding a reaction to a message
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(event.retrieveUser().complete().getId().equals(event.getJDA().getSelfUser().getId())) return;
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 3);
        for (WhenRule rule : rules) {
            if (event.getMessageId().equals(rule.getSourceMessageID()) && event.getEmoji().getFormatted().equals(rule.getSourceReaction())) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("Member reacted with " + rule.getSourceReaction());
                    User interactingUser = event.retrieveUser().complete();
                    String description = interactingUser.getAsMention() + " (" + interactingUser.getId() + ")";
                    if (rule.getActionType() != 5) {
                        description += ", in server " + event.getGuild().getName();
                    }
                    embedToPass.setDescription(description);
                    embedToPass.addField("Message link", event.retrieveMessage().complete().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getUser());
                }
            }
        }
    }

    /**
     * Detects the fourth trigger - member removing a reaction to a message
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if(event.retrieveUser().complete().getId().equals(event.getJDA().getSelfUser().getId())) return;
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 4);
        for (WhenRule rule : rules) {
            if (event.getMessageId().equals(rule.getSourceMessageID()) && event.getEmoji().getFormatted().equals(rule.getSourceReaction())) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("Member removed a reaction " + rule.getSourceReaction());
                    User interactingUser = event.retrieveUser().complete();
                    String description = interactingUser.getAsMention() + " (" + interactingUser.getId() + ")";
                    if (rule.getActionType() != 5) {
                        description += ", in server " + event.getGuild().getName();
                    }
                    embedToPass.setDescription(description);
                    embedToPass.addField("Message link", event.retrieveMessage().complete().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getUser());
                }
            }
        }
    }

    /**
     * Detects the fifth trigger - member editing their message
     */
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) return;
        if (!event.isFromGuild()) return;
        String serverID = event.getGuild().getId();
        ArrayList<WhenRule> rules = Cashew.whenSettingsManager.getRulesOfTriggerType(serverID, 5);
        for (WhenRule rule : rules) {
            if ((rule.getSourceChannelID() == null) || (rule.getSourceChannelID().equals(event.getChannel().getId()))) {
                if (rule.getActionType() == 4 || rule.getActionType() == 5) {
                    EmbedBuilder embedToPass = new EmbedBuilder();
                    embedToPass.setTitle("Member edited their message");
                    String description = event.getAuthor().getAsMention() + " (" + event.getAuthor().getId() + "), in " + (rule.getActionType() != 5 ? ("server " + event.getGuild().getName()) + ", " : "") + "<#" + event.getChannel().getId() + ">";
                    embedToPass.setDescription(description);
                    embedToPass.addField("New content", event.getMessage().getContentRaw(), false);
                    embedToPass.addField("Message link", event.getMessage().getJumpUrl(), false);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), event.getAuthor());
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
                    embedToPass.setTitle("A message was deleted");
                    String description = "In " + (rule.getActionType() != 5 ? ("server " + event.getGuild().getName()) + ", in " : "") + "<#" + event.getChannel().getId() + ">";
                    embedToPass.setDescription(description);
                    performPassAction(rule, event.getGuild(), embedToPass.build());
                } else {
                    performAction(rule, event.getGuild(), null);
                }
            }
        }
    }

    /**
     * Performs actions that don't involve passing information about the event somewhere
     *
     * @param rule   {@link WhenRule WhenRule} which specifies what needs to be done
     * @param server {@link Guild server} on which the action is going to be performed
     * @param user   {@link User user (member)} of the server who might be affected by the action
     */
    private void performAction(WhenRule rule, Guild server, User user) {
        switch (rule.getActionType()) {
            case 1 -> {
                TextChannel targetChannel = server.getChannelById(TextChannel.class, rule.getTargetChannelID());
                actionSendMessage(rule.getTargetMessageContent(), targetChannel, user);
            }
            case 2 -> {
                if (user == null) return;
                actionAddRole(user, server, rule.getTargetRoleID());
            }
            case 3 -> {
                if (user == null) return;
                actionRemoveRole(user, server, rule.getTargetRoleID());
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
        String userMention = user == null ? "A user" : user.getAsMention();
        messageContent = messageContent.replace("@user", userMention);
        try {
            targetChannel.sendMessage(messageContent).complete();
        } catch (Exception ignored) {
        }
    }

    /**
     * Performs the second action - adds a role to a member
     *
     * @param targetUser   {@link User user} who will get a role assigned
     * @param server       {@link Guild server} on which the role assignment will take place
     * @param targetRoleID ID of the role to assign, if the role doesn't exist, nothing will happen
     */
    private void actionAddRole(User targetUser, Guild server, String targetRoleID) {
        Role roleToAdd = server.getRoleById(targetRoleID);
        if (roleToAdd == null) return;
        try {
            server.addRoleToMember(targetUser, roleToAdd).complete();
        } catch (Exception ignored) {
        }
    }

    /**
     * Performs the third action - removes a member's role
     *
     * @param targetUser   {@link User user} who will get a role removed
     * @param server       {@link Guild server} on which the role removal will take place
     * @param targetRoleID ID of the role to remove, if the role doesn't exist, nothing will happen
     */
    private void actionRemoveRole(User targetUser, Guild server, String targetRoleID) {
        Role roleToAdd = server.getRoleById(targetRoleID);
        if (roleToAdd == null) return;
        try {
            server.removeRoleFromMember(targetUser, roleToAdd).complete();
        } catch (Exception ignored) {
        }
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
            targetUser.openPrivateChannel().complete().sendMessageEmbeds(embedToPass).complete();
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
        try {
            targetChannel.sendMessageEmbeds(embedToPass).complete();
        } catch (Exception ignored) {
        }
    }
}
