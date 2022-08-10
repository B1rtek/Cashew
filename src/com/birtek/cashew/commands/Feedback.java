package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Feedback extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Feedback.class);

    /**
     * Creates a feedback {@link MessageEmbed embed} that will be later received by the bot's creator
     *
     * @param content message created by the user who is sending this feedback
     * @param event   {@link SlashCommandInteractionEvent event} triggered by executing this command
     * @return a {@link MessageEmbed MessagEmbed} with fields containing the user's feedback content, user's server (or
     * DM) name, ID, and if the command was executed on a server also the server's name, ID and an invite link if the
     * bot was able to get it. All of that information might help with solving problems that were shared through the
     * command
     */
    private MessageEmbed createFeedbackEmbed(String content, SlashCommandInteractionEvent event) {
        EmbedBuilder feedbackEmbed = new EmbedBuilder();
        feedbackEmbed.setTitle("New feedback!");
        String fromString, serverString = null;
        Member caller = event.getMember();
        if (caller == null || event.getGuild() == null) { // not from guild
            fromString = event.getUser().getAsTag() + " (" + event.getUser().getId() + ")";
        } else {
            fromString = caller.getEffectiveName() + " (" + caller.getUser().getAsTag() + ", " + caller.getUser().getId() + ")";
            serverString = event.getGuild().getName() + " (" + event.getGuild().getId();
            try {
                List<Invite> invites = event.getGuild().retrieveInvites().complete();
                if (invites.size() != 0) {
                    serverString += ", Invite link: " + invites.get(0).getUrl();
                }
            } catch (InsufficientPermissionException | NullPointerException ignored) {
            }
            serverString += ")";
        }
        feedbackEmbed.addField("From", fromString, false);
        if (serverString != null) feedbackEmbed.addField("From server", serverString, false);
        feedbackEmbed.addField("Content", content, false);
        return feedbackEmbed.build();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("feedback")) {
            String content = event.getOption("content", null, OptionMapping::getAsString);
            if (content == null) {
                event.reply("Content cannot be null").setEphemeral(true).queue();
                return;
            }
            event.reply("Feedback sent!").setEphemeral(true).queue();
            event.getJDA().openPrivateChannelById(Cashew.BIRTEK_USER_ID).complete().sendMessageEmbeds(createFeedbackEmbed(content, event)).queue();
        }
    }
}
