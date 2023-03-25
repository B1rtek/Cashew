package com.birtek.cashew.badwayfarersubmissions;

import com.birtek.cashew.Cashew;
import com.birtek.cashew.database.LeaderboardRecord;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public final static String badWayfarerChannelID = "@hydrantyipompywodne";
    public final static String testChannelID = "@b1rtektestbotchannel";
    private final static String b1rtekDMID = "1819824656";
    private int currentlyVerified = 0;

    private final static HashMap<String, NewCommandStatus> activeNewCommandChannels = new HashMap<>();
    private final static HashMap<String, NewSubmission> activeNewSubmissions = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "Ciekawe zgłoszenia wayfarerowe";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_TOKEN");
    }

    private void sendMessage(SendMessage messageToSend) {
        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private java.io.File getPhoto(String photoFileID) {
        String photoFilePath = null;
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photoFileID);
        try {
            // We execute the method using AbsSender::execute method.
            File file = execute(getFileMethod);
            // We now have the file_path
            photoFilePath = file.getFilePath();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        if (photoFilePath == null) return null;
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(photoFilePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null; // Just in case
    }

    private void getNewSubmission(Message photoMessage) {
        List<PhotoSize> photos = photoMessage.getPhoto();
        if (photos.isEmpty()) {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setText("Ta wiadomość nie zawiera żadnego zdjęcia, spróbuj ponownie.");
            errorMessage.setChatId(photoMessage.getChatId().toString());
            sendMessage(errorMessage);
            return;
        }
        PhotoSize maxResPhoto = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
        activeNewSubmissions.put(photoMessage.getChatId().toString(), new NewSubmission(maxResPhoto.getFileId()));
        activeNewCommandChannels.put(photoMessage.getChatId().toString(), NewCommandStatus.GET_DETAILS);
        String description = photoMessage.getCaption();
        if (description == null) description = "";
        NewSubmission completedSubmission = activeNewSubmissions.get(photoMessage.getChatId().toString());
        completedSubmission.setDescription(description);
        activeNewCommandChannels.remove(photoMessage.getChatId().toString());
        PostsDatabase database = PostsDatabase.getInstance();
        int postID = database.addSubmission(completedSubmission, photoMessage.getFrom().getUserName());
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(photoMessage.getChatId().toString());
        successMessage.setText(postID != -1 ? "Pomyślnie dodano nowe zgłoszenie! ID zgłoszenia: " + postID : "Coś poszło nie tak w trakcie dodawania zgłoszenia...");
        try {
            execute(successMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        if (postID != -1) {
            notifyAboutNewPost();
        }
    }

    public boolean postSubmission(Post post, String targetChannelID) {
        SendPhoto testPost = new SendPhoto();
        testPost.setChatId(targetChannelID);
        java.io.File photo = getPhoto(post.fileID());
        if (photo == null) {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setText("Nie udało się dostarczyć \"" + post.caption() + "\", file_id = " + post.fileID() + " - nie udało się pobrać zdjęcia");
            errorMessage.setChatId(b1rtekDMID);
            sendMessage(errorMessage);
            return false;
        }
        InputFile photoInputFile = new InputFile();
        photoInputFile.setMedia(photo);
        testPost.setPhoto(photoInputFile);
        PostsDatabase database = PostsDatabase.getInstance();
        Submitter author = database.getSubmitterStats(post.author());
        String caption = "";
        if(!post.caption().isEmpty()) caption += post.caption();
        if(author.showNickname()) {
            if(!caption.isEmpty()) caption += '\n';
            caption += "Przesłane przez @" + post.author();
        }
        if (!caption.isEmpty()) {
            testPost.setCaption(caption);
        }
        try {
            execute(testPost);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            SendMessage errorMessage = new SendMessage();
            errorMessage.setText("Nie udało się dostarczyć \"" + post.caption() + "\", file_id = " + post.fileID() + " - nie udało się wysłać posta");
            errorMessage.setChatId(b1rtekDMID);
            sendMessage(errorMessage);
            return false;
        }
        return true;
    }

    private void notifyAboutNewPost() {
        SendMessage notification = new SendMessage();
        notification.setChatId(b1rtekDMID);
        notification.setText("Pojawił się nowy post do weryfikacji");
        sendMessage(notification);
    }

    private void notifySubmitter(Post post, String reason, boolean approved) {
        SendMessage notification = new SendMessage();
        notification.setChatId(post.authorDMID());
        if (approved) {
            notification.setText("Post " + post.id() + " został zaakceptowany!");
        } else {
            notification.setText("Post " + post.id() + " został odrzucony! Powód: " + reason);
            postSubmission(post, post.authorDMID());
        }
        sendMessage(notification);
    }

    private void finalizeVerification(String response) {
        SendMessage verificationConfirmation = new SendMessage();
        verificationConfirmation.setChatId(b1rtekDMID);
        PostsDatabase database = PostsDatabase.getInstance();
        String baseResponse = response.split("\\s+")[0];
        String reason = baseResponse.length() == response.length() ? "" : response.substring(baseResponse.length() + 1);
        Post postBeingVerified = database.getPostByID(currentlyVerified);
        if (!baseResponse.toLowerCase(Locale.ROOT).equals("nie")) {
            if (!database.verifyPost(currentlyVerified)) {
                verificationConfirmation.setText("Coś poszło nie tak...");
                sendMessage(verificationConfirmation);
                return;
            }
        } else {
            if (database.removePost(currentlyVerified)) {
                verificationConfirmation.setText("Pomyślnie usunięto post!");
                notifySubmitter(postBeingVerified, reason, false);
            } else {
                verificationConfirmation.setText("Coś poszło nie tak...");
            }
            sendMessage(verificationConfirmation);
            return;
        }
        notifySubmitter(postBeingVerified, "", true);
        currentlyVerified = 0;
        verificationConfirmation.setText("Pomyślnie zweryfikowano post!");
        sendMessage(verificationConfirmation);
    }

    private String postVerification(String chatID) {
        if (!chatID.equals(b1rtekDMID)) return "Nie masz uprawnień do weryfikacji postów";
        PostsDatabase database = PostsDatabase.getInstance();
        Post unverifiedPost = database.getUnverifiedPost();
        if (unverifiedPost == null) return "Coś poszło nie tak...";
        if (unverifiedPost.id() == 0) return "Nie ma żadnych niezweryfikowanych postów";
        postSubmission(unverifiedPost, b1rtekDMID);
        currentlyVerified = unverifiedPost.id();
        return "Przesłane przez @" + unverifiedPost.author() + "\nNapisz \"tak\", aby zweryfikować post, lub \"nie\" aby go odrzucić";
    }

    private String createLeaderboardTable(ArrayList<LeaderboardRecord> leaderboard) {
        Table leaderboardTable = new Table(2, BorderStyle.DESIGN_PAPYRUS, ShownBorders.SURROUND_HEADER_AND_COLUMNS);
        leaderboardTable.addCell("Agent");
        leaderboardTable.addCell("#");
        for (LeaderboardRecord record : leaderboard) {
            leaderboardTable.addCell(record.userID());
            leaderboardTable.addCell(String.valueOf(record.count()));
        }
        return leaderboardTable.render();
    }

    private String getSubmissionsStats() {
        PostsDatabase database = PostsDatabase.getInstance();
        ArrayList<LeaderboardRecord> leaderboard = database.getTopSubmitters();
        return "```\nLista agentów z największą ilością zgłoszeń\n\n" + createLeaderboardTable(leaderboard) + "\n```";
    }

    private String getQueueStats() {
        PostsDatabase database = PostsDatabase.getInstance();
        Pair<Integer, Integer> stats = database.getQueueStats();
        StringBuilder queueStats = new StringBuilder("Liczba postów w kolejce: " + stats.getLeft() + "\nLiczba postów czekających na weryfikację: " + stats.getRight());
        if (stats.getLeft() > 0) {
            queueStats.append("\nKolejny post zaplanowany jest na ||");
            queueStats.append(Cashew.postsManager.getNextPostTime().replaceAll("-", "\\-"));
            queueStats.append("||");
        }
        return queueStats.toString();
    }

    private enum NewCommandStatus {
        GET_PHOTO, GET_DETAILS
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String message = update.getMessage().getText();
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(update.getMessage().getChatId().toString());
            if (message != null && message.startsWith("/")) {
                PostsDatabase database = PostsDatabase.getInstance();
                database.saveChatID(update.getMessage().getFrom().getUserName(), String.valueOf(update.getMessage().getChatId()));
                switch (message.split("\\s+")[0].substring(1)) {
                    case "nowy" -> {
                        activeNewCommandChannels.put(update.getMessage().getChatId().toString(), NewCommandStatus.GET_PHOTO);
                        replyMessage.setText("Proszę wyślij screena zgłoszenia razem z opisem");
                    }
                    case "ping" -> replyMessage.setText("Pong!");
                    case "weryfikuj" ->
                            replyMessage.setText(postVerification(update.getMessage().getChatId().toString()));
                    case "staty" -> {
                        replyMessage.setText(getSubmissionsStats());
                        replyMessage.setParseMode("Markdown");
                    }
                    case "kolejka" -> {
                        replyMessage.setText(getQueueStats());
                        replyMessage.setParseMode("MarkdownV2");
                    }
                    case "shownick" -> {
                        int result = database.toggleNickVisibility(update.getMessage().getFrom().getUserName());
                        if (result != -1) {
                            replyMessage.setText("Twój nick" + (result == 0 ? " nie" : "") + " będzie teraz widoczny w postach.");
                        } else {
                            replyMessage.setText("Nie udało się zmienić widoczności nicku w postach!");
                        }
                    }
                }
                sendMessage(replyMessage);
            } else if (update.getMessage().hasPhoto() || update.getMessage().hasText()) {
                NewCommandStatus commandStatus = activeNewCommandChannels.get(update.getMessage().getChatId().toString());
                if (commandStatus == null) {
                    if (update.getMessage().getChatId().toString().equals(b1rtekDMID) && currentlyVerified != 0) {
                        finalizeVerification(update.getMessage().getText());
                    }
                } else {
                    getNewSubmission(update.getMessage());
                }
            }
        }
    }
}
