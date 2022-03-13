package com.birtek.cashew.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChoccyMilk extends BaseCommand {

    Permission[] choccyMilkCommandPermissions = {
            Permission.MESSAGE_SEND
    };

    String choccyMilkURL = "https://cdn.discordapp.com/attachments/857711843282649158/952645669598945290/unknown.png";
    String choccyMilkButtonID = "choccyMilkGet";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("choccymilk")) {
            if (checkSlashCommandPermissions(event, choccyMilkCommandPermissions)) {
                String buttonID = Objects.requireNonNull(event.getMember()).getId() + ":" + choccyMilkButtonID;
                EmbedBuilder choccyMilkEmbed = new EmbedBuilder();
                choccyMilkEmbed.setTitle("A wild gift appears!");
                choccyMilkEmbed.setThumbnail(choccyMilkURL);
                choccyMilkEmbed.setDescription("Choccy Milk");
                event.replyEmbeds(choccyMilkEmbed.build()).addActionRow(Button.success(buttonID, "ACCEPT")).queue();
                choccyMilkEmbed.clear();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] buttonID = event.getComponentId().split(":"); // this is the custom id we specified in our button
        String userID = buttonID[0];
        String type = buttonID[1];
        if (type.equals(choccyMilkButtonID)) {
            if (!Objects.equals(userID, event.getUser().getId())) {
                event.getChannel().sendMessage("\uD83C\uDF6B \uD83E\uDD5B \uD83D\uDE0B").queue(message -> message.reply(event.getUser().getAsMention() + ", your Choccy Milk has arrived :3").queue());
                event.getMessage().delete().queue();
            } else {
                event.reply("You can't accept a gift from yourself!").setEphemeral(true).queue();
            }
        }
    }
}
