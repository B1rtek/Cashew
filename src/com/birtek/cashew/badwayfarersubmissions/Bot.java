package com.birtek.cashew.badwayfarersubmissions;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Bot extends TelegramLongPollingBot {

    public final static String badWayfarerChannelID = "@hydrantyipompywodne";
    public final static String testChannelID = "@b1rtektestbotchannel";
    private final static String b1rtekDMID = "1819824656";
    private int currentlyVerified = 0;

    private void getNewSubmissionDetails(Message detailsMessage) {
        String description = detailsMessage.getText();
        if (description == null || description.isEmpty()) {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setText("Ta wiadomość nie zawiera tekstu, spróbuj ponownie.");
            errorMessage.setChatId(detailsMessage.getChatId().toString());
            sendMessage(errorMessage);
            return;
        }
        if (description.toLowerCase(Locale.ROOT).equals("żaden")) description = "";
        NewSubmission completedSubmission = activeNewSubmissions.get(detailsMessage.getChatId().toString());
        completedSubmission.setDescription(description);
        activeNewCommandChannels.remove(detailsMessage.getChatId().toString());
        PostsDatabase database = PostsDatabase.getInstance();
        boolean success = database.addSubmission(completedSubmission, detailsMessage.getFrom().getUserName());
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(detailsMessage.getChatId().toString());
        successMessage.setText(success ? "Pomyślnie dodano nowe zgłoszenie!" : "Coś poszło nie tak w trakcie dodawania zgłoszenia...");
        try {
            execute(successMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        if (success) {
            notifyAboutNewPost();
        }
    }

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

    private void getNewSubmissionPhoto(Message photoMessage) {
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
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(photoMessage.getChatId().toString());
        successMessage.setText("Jaki jest twój komentarz do tego zdjęcia? (napisz \"żaden\" aby pozostawić pusty)");
        try {
            execute(successMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
        if (!post.caption().isEmpty()) {
            testPost.setCaption(post.caption());
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

    private void finalizeVerification(String response) {
        SendMessage verificationConfirmation = new SendMessage();
        verificationConfirmation.setChatId(b1rtekDMID);
        PostsDatabase database = PostsDatabase.getInstance();
        if (response.toLowerCase(Locale.ROOT).equals("tak")) {
            if (!database.verifyPost(currentlyVerified)) {
                verificationConfirmation.setText("Coś poszło nie tak...");
                sendMessage(verificationConfirmation);
                return;
            }
        } else {
            if (database.removePost(currentlyVerified)) {
                verificationConfirmation.setText("Pomyślnie usunięto post!");
            } else {
                verificationConfirmation.setText("Coś poszło nie tak...");
            }
            sendMessage(verificationConfirmation);
            return;
        }
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
        return "Napisz \"tak\", aby zweryfikować post, lub \"nie\" aby go odrzucić";
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
                switch (message.split("\\s+")[0].substring(1)) {
                    case "nowy" -> {
                        activeNewCommandChannels.put(update.getMessage().getChatId().toString(), NewCommandStatus.GET_PHOTO);
                        replyMessage.setText("Proszę wyślij screena zgłoszenia");
                    }
                    case "ping" -> replyMessage.setText("Pong!");
                    case "weryfikuj" ->
                            replyMessage.setText(postVerification(update.getMessage().getChatId().toString()));
                }
                sendMessage(replyMessage);
            } else if (update.getMessage().hasPhoto() || update.getMessage().hasText()) {
                NewCommandStatus commandStatus = activeNewCommandChannels.get(update.getMessage().getChatId().toString());
                if (commandStatus == null) {
                    if (update.getMessage().getChatId().toString().equals(b1rtekDMID) && currentlyVerified != 0) {
                        finalizeVerification(update.getMessage().getText());
                    }
                } else {
                    switch (commandStatus) {
                        case GET_PHOTO -> getNewSubmissionPhoto(update.getMessage());
                        case GET_DETAILS -> getNewSubmissionDetails(update.getMessage());
                    }
                }
            }
        }
    }
}
