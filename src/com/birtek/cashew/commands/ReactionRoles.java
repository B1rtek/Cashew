package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.WhenRule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class ReactionRoles extends BaseCommand {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("reactionroles")) {
            if (cantBeExecuted(event, true)) {
                event.reply("This command is only available to server moderators").setEphemeral(true).queue();
                return;
            }
            // get all roles
            ArrayList<Pair<String, Role>> reactionRoles = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                String reactionAndRole = event.getOption("role" + i, null, OptionMapping::getAsString);
                if (reactionAndRole == null) continue;
                String[] reactionRoleSplit = reactionAndRole.split("\\s+");
                int roleIDBegin = reactionRoleSplit[1].indexOf("<@&");
                if (roleIDBegin == -1) continue;
                roleIDBegin += 3;
                String roleID = reactionRoleSplit[1].substring(roleIDBegin, reactionRoleSplit[1].indexOf(">", roleIDBegin));
                Role targetRole = Objects.requireNonNull(event.getGuild()).getRoleById(roleID);
                if (targetRole == null) continue;
                reactionRoles.add(Pair.of(reactionRoleSplit[0], targetRole));
            }
            if (reactionRoles.isEmpty()) {
                event.reply("No valid emotes/roles were chosen!").setEphemeral(true).queue();
                return;
            }
            // create an embed
            EmbedBuilder reactionRolesEmbed = new EmbedBuilder();
            String title = event.getOption("title", "You can obtain these roles by reacting to the message", OptionMapping::getAsString);
            reactionRolesEmbed.setTitle(title);
            StringBuilder rolesList = new StringBuilder();
            for (Pair<String, Role> reactionRole : reactionRoles) {
                rolesList.append(reactionRole.getLeft()).append(" for ").append(reactionRole.getRight().getAsMention()).append('\n');
            }
            reactionRolesEmbed.setDescription(rolesList.toString());
            // send it
            String[] embedMessageID = new String[1];
            event.getChannel().sendMessageEmbeds(reactionRolesEmbed.build()).queue(embedMessage -> embedMessageID[0] = embedMessage.getId());
            // add rules matching the roles
            for(Pair<String, Role> reactionRole : reactionRoles) {
                WhenRule ruleAdd = new WhenRule(event.getGuild().getId());
                ruleAdd.memberReactsTrigger(embedMessageID[0], reactionRole.getLeft());
                ruleAdd.addRoleAction(reactionRole.getRight().getId());
                WhenRule ruleRemove = new WhenRule(event.getGuild().getId());
                ruleRemove.memberRemovesReactionTrigger(embedMessageID[0], reactionRole.getLeft());
                ruleRemove.removeRoleAction(reactionRole.getRight().getId());
                if(!Cashew.whenSettingsManager.addWhenRule(event.getGuild().getId(), ruleAdd) || !Cashew.whenSettingsManager.addWhenRule(event.getGuild().getId(), ruleRemove)) {
                    event.getChannel().retrieveMessageById(embedMessageID[0]).complete().delete().complete();
                    event.reply("Something went wrong while applying rules").setEphemeral(true).queue();
                    return;
                }
            }
            // communicate success
            event.reply("Reaction roles embed created!").setEphemeral(true).queue();
            // add reactions
            Message reactionRolesMessage = event.getChannel().retrieveMessageById(embedMessageID[0]).complete();
            for(Pair<String, Role> reactionRole : reactionRoles) {
                reactionRolesMessage.addReaction(Emoji.fromFormatted(reactionRole.getLeft())).queue();
            }
        }
    }
}
