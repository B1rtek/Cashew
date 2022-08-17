package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Korwin extends BaseCommand {

    String[] cytaty1 = {
            "Proszę zwrócić uwagę, że",
            "I tak mam trzy razy mniej czasu, więc proszę mi pozwolić powiedzieć:",
            "Państwo się się śmieją, ale",
            "Ja nie potrzebowałem edukacji seksualnej, żeby wiedzieć, że",
            "No niestety,", "Gdzie leży przyczyna problemu? Ja państwu powiem:",
            "Państwo chyba nie widzą, że",
            "Oświadczam kategorycznie:",
            "Powtarzam:",
            "Powiedzmy to z całą mocą:",
            "W Polsce dzisiaj",
            "Państwo sobie nie zdają sprawy, że",
            "To ja przepraszam bardzo:",
            "Otóż nie wiem, czy pan wie, że",
            "Yyyyy... ",
            "Ja chcę powiedzieć jedną rzecz:",
            "Trzeba powiedzieć jasno:",
            "Jak powiedział wybitny krakowianin Stanisław Lem,",
            "Proszę mnie dobrze zrozumieć:",
            "Ja chciałem państwu przypomnieć, że",
            "Niech państwo nie mają złudzeń:",
            "Powiedzmy to wyraźnie:"
    };

    String[] cytaty2 = {
            "właściciele niewolników,",
            "związkowcy,",
            "trockiści,",
            "tak zwane dzieci kwiaty,",
            "rozmaici urzędnicy,",
            "federaści,",
            "etatyści,",
            "ci durnie i złodzieje,",
            "ludzie wybrani głosami meneli spod budki z piwem,",
            "socjaliści pobożni,",
            "socjaliści bezbożni,",
            "komuniści z krzyżem w zębach,",
            "agenci obcych służb,",
            "członkowie Bandy Czworga,",
            "pseudo-masoni z Wielkiego Wschodu Francji,",
            "przedstawiciele czerwonej hołoty,",
            "ci wszyscy (tfu!) geje,",
            "funkcjonariusze reżymowej telewizji,",
            "tak zwani ekolodzy,",
            "ci wszyscy (tfu!) demokraci,",
            "agenci bezpieki,",
            "feminazistki,"
    };

    String[] cytaty3 = {
            "po przeczytaniu Manifestu Komunistycznego,",
            "którymi się brzydzę,",
            "których nienawidzę,",
            "z okolic \"Gazety Wyborczej\",",
            "czyli taka żydokomuna,",
            "odkąd zniesiono karę śmierci,",
            "którymi pogardzam,",
            "których miejsce w normalnym kraju jest w więzieniu,",
            "na polecenie Brukseli,",
            "posłużnie",
            "bezmyślnie,",
            "z nieprawdopodobną pogardą dla człowieka,",
            "za pieniądze podatników,",
            "zgodnie z ideologią LGBTQZ,",
            "za wszelką cenę",
            "zupełnie bezkarnie,",
            "całkowicie bezczelnie,",
            "o poglądach na lewo od komunizmu,",
            "celowo i świadomie,",
            "z premedytacją,",
            "od czasów Okrągłego Stołu,",
            "w ramach postępu,"
    };

    String[] cytaty4 = {
            "udają homoseksualistów,",
            "niszczą rodzinę,",
            "idą do polityki,",
            "zakazują góralom robienia oscypków,",
            "organizują paraolimpiady,",
            "wprowadzają ustrój, w którym raz na cztery lata można wybrać sobie pana,",
            "ustawiają fotoradary,",
            "wprowadzają dotacje,",
            "wydzielają buspasy,",
            "podnoszą wiek emerytalny,",
            "rżną głupa,",
            "odbierają dzieci rodzicom,",
            "wprowadzają absurdalne przepisy,",
            "umieszczają dzieci w szkołach koedukacjyjnych,",
            "wprowadzają parytety,",
            "nawołują do podniesienia podatków,",
            "próbują wyrzucić kierowców z miast,",
            "próbują skłócić Polskę z Rosją,",
            "głoszą brednie o globalnym ociepleniu,",
            "zakazują posiadanie broni,",
            "nie dopuszczają prawicy do władzy,",
            "uczą dzieci homoseksualizmu,"
    };

    String[] cytaty5 = {
            "żeby poddawać wszystkich tresurze,",
            "bo taka jest ich natura,",
            "bo chcą wszystko kontrolować,",
            "bo nie rozumieją, że socjalizm nie działa,",
            "żeby wreszcie zapanował socjalizm,",
            "dokładnie tak jak towarzysz Janosik,",
            "zamiast pozwolić ludziom zarabiać,",
            "żeby wyrwać kobiety z domu,",
            "bo to jest w interesie tak zwanych ludzi pracy,",
            "zamiast pozwolić decydować konsumentowi,",
            "żeby nie opłacało się mieć dzieci,",
            "zamiast obniżyć podatki,",
            "bo nie rozumieją, że selekcja naturalna jest czymś dobrym,",
            "żeby mężczyźni przestali być agresywni,",
            "bo dzięki temu mogą brać łapówki,",
            "bo dzięki temu mogą kraść,",
            "bo dostają za to pieniądze,",
            "bo tak się uczy w państwowej szkole,",
            "bo bez tego (tfu!) demokracja nie może istnieć,",
            "bo głupich jest więcej niż mądrych,",
            "bo chcą tworzyć raj na ziemi,",
            "bo chcą niszczyć cywilizację białego człowieka,"
    };

    String[] cytaty6 = {
            "co ma zresztą tyle samo sensu, co zawody w szachach dla debili.",
            "co zostało dokładnie zaplanowane w Magdalence przez śp. generała Kiszczaka.",
            "i trzeba być idiotą, żeby ten system popierać.",
            "ale ja jeszcze dożyję normalnych czasów.",
            "co dowodzi, że wyskrobano nie tych, co trzeba.",
            "a zwykłym ludziom wmawiają, że im coś \"dadzą\".",
            " - cóż: chcieliście (tfu!) demokracji, to macie.",
            "dlatego trzeba zlikwidować koryto, a nie zmieniać świnie.",
            "a wystarczyłoby przestać wypłacać zasiłki.",
            "podczas, gdy normalni ludzie uważani są za dziwaków.",
            "co w wieku XIX po prostu by wyśmiano.",
            "- dlatego w społeczeństwie jest równość, a powinno być rozwarstwienie.",
            "co prowadzi Polskę do katastrofy.",
            "- dlatego trzeba przywrócić normalność.",
            "ale w wolnej Polsce pójdą siedzieć.",
            "przez kolejne kadencje.",
            "o czym się nie mówi.",
            "i dlatego właśnie Europa umiera.",
            "ale przyjdą muzułmanie i zrobią porządek.",
            "- tak samo zresztą jak za Hitlera.",
            "- proszę zobaczyć, co się dzieje na Zachodzie, jeśli państwo mi nie wierzą.",
            "co 100 lat temu nikomu nie przyszłoby nawet do głowy."
    };

    String[] korwiny = {
            "https://cdn.discordapp.com/attachments/857711843282649158/921167338080981022/korwin0.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167346838695976/korwin1.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167354635907092/korwin2.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167362718335008/korwin3.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167370536484884/korwin4.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167376551133224/korwin5.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167383564009542/korwin6.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167389947723886/korwin7.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167396394385498/korwin8.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167402740363364/korwin9.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167408910204938/korwin10.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167416665468949/korwin11.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167422696869898/korwin12.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167433463631903/korwin13.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167440673652746/korwin14.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167447229341696/korwin15.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167453889908766/korwin16.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167459145363497/korwin17.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167472747491348/korwin18.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167478346874880/korwin19.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167492729176104/korwin20.png",
            "https://cdn.discordapp.com/attachments/857711843282649158/921167498219495524/korwin21.png"
    };

    /**
     * Creates an {@link MessageEmbed embed} with a randomly generated JKM quote from parts in the arrays above
     */
    private MessageEmbed generateAKorwinQuote() {
        Random random = new Random();
        EmbedBuilder korwinEmbed = new EmbedBuilder();
        String cytat = cytaty1[random.nextInt(cytaty1.length)] + ' ' +
                cytaty2[random.nextInt(cytaty2.length)] + ' ' +
                cytaty3[random.nextInt(cytaty3.length)] + ' ' +
                cytaty4[random.nextInt(cytaty4.length)] + ' ' +
                cytaty5[random.nextInt(cytaty5.length)] + ' ' +
                cytaty6[random.nextInt(cytaty6.length)];
        korwinEmbed.addField("A 100% legit quote from Janusz Korwin-Mikke: ", cytat, false);
        korwinEmbed.setImage(korwiny[random.nextInt(korwiny.length)]);
        return korwinEmbed.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "korwin")) {
            if(cantBeExecutedPrefix(event, "korwin", false)) {
                event.getMessage().reply("This command is turned off in this channel").mentionRepliedUser(false).queue();
                return;
            }
            event.getChannel().sendMessageEmbeds(generateAKorwinQuote()).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("korwin")) {
            if(cantBeExecuted(event, false)) {
                event.reply("This command is turned off in this channel").setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(generateAKorwinQuote()).queue();
        }
    }
}
