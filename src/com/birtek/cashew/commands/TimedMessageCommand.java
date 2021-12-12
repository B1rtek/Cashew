package com.birtek.cashew.commands;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class TimedMessageCommand extends BaseCommand {

    Permission[] timedMessageCommandPermissions = {
            Permission.MANAGE_CHANNEL
    };

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        if (args[0].equalsIgnoreCase(Cashew.COMMAND_PREFIX + "timedmessage")) {
            if(checkPermissions(event, timedMessageCommandPermissions)) {
                if(event.isWebhookMessage()) return;
                if(args.length<4 && args.length != 3 && args.length != 2) {
                    event.getMessage().reply("Incorrect syntax. Please specify all the arguments (<destination channel> <time> <message> or <action> <id>).").mentionRepliedUser(false).queue();
                } else if(args.length == 3 || args.length == 2) {
                    Database database = Database.getInstance();
                    if(args[1].equalsIgnoreCase("show") || args[1].equalsIgnoreCase("list")) {
                        ResultSet timedMessages;
                        if(args.length == 2) {
                            timedMessages = database.showTimedMessages(">0", event.getGuild().getId());
                        } else {
                            if(args[2].equalsIgnoreCase("all")) {
                                timedMessages = database.showTimedMessages(">0", event.getGuild().getId());
                            } else {
                                if(!isNumeric(args[2])) {
                                    event.getMessage().reply("The provided ID is not a number.").mentionRepliedUser(false).queue();
                                    return;
                                }
                                timedMessages = database.showTimedMessages("="+args[2], event.getGuild().getId());
                            }
                        }
                        if(timedMessages!=null) {
                            StringBuilder table = new StringBuilder("```prolog\n");
                            table.append("ID        |Time    |Channel             |Message                       \n");
                            int rowCount = 0;
                            try {
                                while(timedMessages.next()) {
                                    rowCount++;
                                    StringBuilder id = new StringBuilder(String.valueOf(timedMessages.getInt("_id")));
                                    id.setLength(10);
                                    String stringId = id.toString().replace('\u0000', ' ');
                                    table.append(stringId).append("|");
                                    table.append(timedMessages.getString("executionTime")).append("|");
                                    String channelID = timedMessages.getString("destinationChannelID");
                                    StringBuilder channel = new StringBuilder(Objects.requireNonNull(event.getGuild().getGuildChannelById(channelID)).getName());
                                    String stringChannel;
                                    if(channel.length()>20) {
                                        channel.setLength(17);
                                        stringChannel = channel.toString().replace('\u0000', ' ')+"...";
                                        //channel.append("...");
                                    } else {
                                        channel.setLength(20);
                                        stringChannel = channel.toString().replace('\u0000', ' ');
                                    }
                                    table.append(stringChannel).append("|");
                                    StringBuilder message = new StringBuilder(timedMessages.getString("messageContent"));
                                    String stringMessage;
                                    if(message.length()>80) {
                                        message.setLength(77);
                                        stringMessage = message.toString().replace('\u0000', ' ')+"...";
                                        //message.append("...");
                                    } else {
                                        message.setLength(80);
                                        stringMessage = message.toString().replace('\u0000', ' ');
                                    }
                                    stringMessage = stringMessage.replace("\n", "\\n");
                                    table.append(stringMessage).append("\n");
                                }
                                table.append("```");
                            } catch (SQLException e) {
                                event.getMessage().reply("[4] Something went wrong...").mentionRepliedUser(false).queue();
                                return;
                            }
                            if(rowCount==0) {
                                if(args[2].equalsIgnoreCase("all")) {
                                    event.getMessage().reply("There are no defined TimedMessages yet.").mentionRepliedUser(false).queue();
                                } else {
                                    event.getMessage().reply("Message with this ID doesn't exist.").mentionRepliedUser(false).queue();
                                }
                            } else {
                                event.getMessage().reply(table).mentionRepliedUser(false).queue();
                            }
                        } else {
                            event.getMessage().reply("[3] Something went wrong...").mentionRepliedUser(false).queue();
                        }
                    } else if((args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove")) && args.length == 3) {
                        if(args[2].equalsIgnoreCase("all")) {
                            int deletionResult = database.deleteTimedMessages(">0", event.getGuild().getId());
                            if(deletionResult==0) {
                                event.getMessage().reply("Successfully deleted all timed messages!").mentionRepliedUser(false).queue();
                            } else if (deletionResult==1) {
                                event.getMessage().reply("[1] Something went wrong...").mentionRepliedUser(false).queue();
                            } else if (deletionResult==-1) {
                                event.getMessage().reply("The message you're trying to delete doesn't exist.").mentionRepliedUser(false).queue();
                            }
                        } else {
                            if(!isNumeric(args[2])) {
                                event.getMessage().reply("The provided ID is not a number.").mentionRepliedUser(false).queue();
                                return;
                            }
                            int deletionResult = database.deleteTimedMessages("="+args[2], event.getGuild().getId());
                            if(deletionResult==0) {
                                event.getMessage().reply("Successfully deleted timed message "+ args[2] +"!").mentionRepliedUser(false).queue();
                            } else if (deletionResult==1) {
                                event.getMessage().reply("[2] Something went wrong...").mentionRepliedUser(false).queue();
                            } else if (deletionResult==-1) {
                                event.getMessage().reply("The message you're trying to delete doesn't exist.").mentionRepliedUser(false).queue();
                            }
                        }
                    } else {
                        event.getMessage().reply("Incorrect syntax. Please specify all the arguments (<action> <id>).").mentionRepliedUser(false).queue();
                    }
                } else {
                    StringBuilder messageContent = new StringBuilder(args[3]);
                    for(int i=4; i<args.length; i++) {
                        messageContent.append(" ").append(args[i]);
                    }
                    String messageContentString = messageContent.toString().replace("\"", "''");
                    messageContentString = messageContentString.replace("\\n", "\n");
                    if(messageContentString.length()>2000) {
                        event.getMessage().reply("The specified message is too long.").mentionRepliedUser(false).queue();
                        return;
                    }
                    String channelID = args[1].substring(2, args[1].length()-1);
                    if(event.getGuild().getGuildChannelById(channelID)==null) {
                        event.getMessage().reply("Invalid channel specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    if(args[2].length()!=8 || Integer.parseInt(args[2].substring(0, 2))>23 || Integer.parseInt(args[2].substring(3, 5))>59 || Integer.parseInt(args[2].substring(6, 8))>59) {
                        event.getMessage().reply("Invalid timestamp specified").mentionRepliedUser(false).queue();
                        return;
                    }
                    Database database = Database.getInstance();
                    int insertID = database.addTimedMessage(messageContentString, args[2], "86400", channelID, event.getGuild().getId());
                    if(insertID!=0) {
                        event.getMessage().reply("Successfully added a new timed message! ID = "+insertID).mentionRepliedUser(false).queue();
                    } else {
                        event.getMessage().reply("[5] Something went wrong...").mentionRepliedUser(false).queue();
                    }
                }
            } else {
                event.getMessage().delete().complete();
            }
        }
    }
}