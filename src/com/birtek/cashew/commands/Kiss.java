package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Kiss extends BaseCuddlyCommand {

    EmbedGif[] kissGifs = {
            new EmbedGif("https://c.tenor.com/3Y9B4si5mR4AAAAC/maple-cinnamon.gif", 0xF78ECB),
            new EmbedGif("https://media1.tenor.com/images/b1726d7c03317421fb504faa2deb674f/tenor.gif", 0xF6EDA7)
    };

    String[] reactions = {
            "UwU", "OwO", ":3", ";3", "Nyaaa!", "<3", "Yayy!", "Cute~", "Adorable~", "Hot", ">w<"
    };

    String action = "kisses";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "kiss")) {
            if(!event.isFromGuild()) {
                event.getMessage().reply("This command doesn't work in DMs").mentionRepliedUser(false).queue();
                return;
            }
            String cuddlyString = purifyFromMentionsAndMerge(args, event.getGuild(), true);
            if (cuddlyString.isEmpty()) {
                event.getMessage().reply("You can't kiss no one!").mentionRepliedUser(false).queue();
                return;
            }
            sendCuddlyEmbedFromPrefix(event, cuddlyString, kissGifs, action, reactions);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("kiss")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            String[] cuddlyStringSplit = event.getOption("tokiss", "", OptionMapping::getAsString).split("\\s+");
            String cuddlyString = purifyFromMentionsAndMerge(cuddlyStringSplit, event.getGuild(), false);
            if (!cuddlyString.isEmpty()) {
                event.replyEmbeds(createCuddlyEmbed(cuddlyString, Objects.requireNonNull(event.getMember()), kissGifs, action, reactions)).queue();
            } else {
                event.reply("You can't kiss no one!").setEphemeral(true).queue();
            }
        }
    }
}
