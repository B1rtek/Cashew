package com.birtek.cashew.events;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class GuildMemberJoinAndLeave extends ListenerAdapter {

    String[] joinMessages = {
            "[member] joined. You must construct addition pylons!",
            "Never gonna give [member] up! Never gonna let [member] down!",
            "Hey! Listen! [member] has joined!",
            "Ha! [member] has joined! You activated my trap card!",
            "We've been expecting you, [member].",
            "It's dangerous to go alone, take [member]!",
            "Swoooosh. [member] just landed.",
            "Brace yourselves. [member] just joined the server.",
            "A wild [member] appeared.",
            "[member] just slid into the server!",
            "Ermagherd. [member] is here.",
            "[member] joined your party.",
            "[member] just joined the server. - glhf",
            "[member] just joined. Everyone, look busy!",
            "[member]  just joined. Can I get a heal?",
            "Welcome, [member]. Stay awhile and listen",
            "Welcome, [member]. Leave your weapons by the door.",
            "Welcome, [member]. We hope you brought pizza.",
            "Brace yourselves. [member] just joined the server.",
            "[member] just joined. Hide your bananas.",
            "[member] just arrived. Seems OP - please nerf.",
            "A [member] has spawned in the server.",
            "Big [member] showed up!",
            "Where’s [member]? In the server!",
            "[member] hopped into the server. Kangaroo!!",
            "[member] just showed up. Hold my beer.",
            "Challenger approaching - [member] has appeared!",
            "It's a bird! It's a plane! Nevermind, it's just [member].",
            "It's [member]! Praise the sun!",
            "Roses are red, violets are blue, [member] joined this server with you",
            "Hello. Is it [member] you're looking for?",
            "[member] is here to kick butt and chew bubblegum. And [member] is all out of gum.",
            "[member] has arrived. Party's over.",
            "Ready player [member]"
    };

    String[] leaveMessages = {
            "n00b [member] just left. lol :joy:",
            "fukkin n00b [member] just got removed lmaooooo :joy: :joy: :joy:"
    };

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Random rand = new Random();
        int number = rand.nextInt(joinMessages.length);
        if(event.getGuild().getId().equals(Cashew.NEKOPARA_EMOTES_UWU_SERVER_ID)) {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage(event.getMember().getAsMention() + ", welcome to La Soleil :3").queue();
            if(!event.getUser().isBot()) {
                event.getGuild().addRoleToMember(event.getMember(), Objects.requireNonNull(event.getGuild().getRoleById("852817429980512256"))).complete(); //dodanie catgirls enjoyera
            }
        } else if(event.getGuild().getId().equals(Cashew.PI_SERVER_ID)) {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage("Welcome to the cum zone, " + event.getMember().getAsMention()).queue();
        } /*else {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage(joinMessages[number].replace("[member]", event.getMember().getAsMention())).queue();
        }*/
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Random rand = new Random();
        int number = rand.nextInt(leaveMessages.length);
        if(event.getGuild().getId().equals(Cashew.NEKOPARA_EMOTES_UWU_SERVER_ID)) {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage("See you soon, " + event.getUser().getAsMention()).queue();
        } else if(event.getGuild().getId().equals(Cashew.PI_SERVER_ID)) {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage("n00b " + event.getUser().getAsMention() + " właśnie wyszedł. lol :joy:").queue();
        } /*else {
            Objects.requireNonNull(event.getGuild().getSystemChannel()).sendMessage(leaveMessages[number].replace("[member]", event.getUser().getAsMention())).queue();
        }*/
    }
}