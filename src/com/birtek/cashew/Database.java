package com.birtek.cashew;

import com.birtek.cashew.commands.GiftInfo;
import com.birtek.cashew.commands.GiftStats;
import com.birtek.cashew.commands.TwoStringsPair;
import com.birtek.cashew.messagereactions.CountingInfo;
import com.birtek.cashew.timings.TimedMessage;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static volatile Database instance;

    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String CHANNEL_ACTIVITY_DB = "jdbc:sqlite:databases/channelActivity.db";
    public static final String BO_BURNHAM_DB = "jdbc:sqlite:databases/boBurnhamQuotes.db";
    public static final String CASE_OPENING_DB = "jdbc:sqlite:databases/caseOpening.db";
    public static final String COLLECTION_OPENING_DB = "jdbc:sqlite:databases/collectionOpening.db";
    public static final String TIMED_MESSAGES_DB = "jdbc:sqlite:databases/timedMessages.db";
    public static final String SOCIALCREDIT_DB = "jdbc:sqlite:databases/socialCredit.db";
    public static final String COUNTING_DB = "jdbc:sqlite:databases/counting.db";
    public static final String GIFTS_DB = "jdbc:sqlite:databases/gifts.db";

    private Connection channelActivityConnection;
    private Statement channelActivityStatement;
    private Connection boBurnhamConnection;
    private Connection caseOpeningConnection;
    private Connection collectionOpeningConnection;
    private Connection timedMessagesConnection;
    private Connection socialCreditConnection;
    private Connection countingConnection;
    private Connection giftsConnection;

    private Database() {

        try {
            Class.forName(Database.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Missing JDBC driver");
            e.printStackTrace();
        }

        try {
            channelActivityConnection = DriverManager.getConnection(CHANNEL_ACTIVITY_DB);
            channelActivityStatement = channelActivityConnection.createStatement();
            boBurnhamConnection = DriverManager.getConnection(BO_BURNHAM_DB);
            caseOpeningConnection = DriverManager.getConnection(CASE_OPENING_DB);
            collectionOpeningConnection = DriverManager.getConnection(COLLECTION_OPENING_DB);
            timedMessagesConnection = DriverManager.getConnection(TIMED_MESSAGES_DB);
            socialCreditConnection = DriverManager.getConnection(SOCIALCREDIT_DB);
            countingConnection = DriverManager.getConnection(COUNTING_DB);
            giftsConnection = DriverManager.getConnection(GIFTS_DB);
        } catch (SQLException e) {
            System.err.println("There was a problem while establishing a connection with the databases");
            e.printStackTrace();
        }

        createTables();
    }

    public static Database getInstance() {
        Database result = instance;
        if (result != null) {
            return result;
        }
        synchronized (Database.class) {
            if (instance == null) {
                instance = new Database();
            }
            return instance;
        }
    }

    private void createTables() {
        String createTestTable = "CREATE TABLE IF NOT EXISTS ChannelActivity(_id INTEGER PRIMARY KEY AUTOINCREMENT, channelID TEXT, activity INTEGER)";
        try {
            channelActivityStatement.execute(createTestTable);

        } catch (SQLException e) {
            System.err.println("An error occurred while creating the tables");
            e.printStackTrace();
        }
    }

    public void channelActivityInsert(String channelID, int activitySetting) {
        try {
            PreparedStatement prepStmt = channelActivityConnection.prepareStatement("INSERT INTO ChannelActivity(channelID, activity) VALUES(?, ?);");
            prepStmt.setString(1, channelID);
            prepStmt.setInt(2, activitySetting);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("An error occured while inserting the values");
            e.printStackTrace();
        }
    }

    public ResultSet channelActivitySelect(String channelID) {
        try {
            PreparedStatement prepStmt = channelActivityConnection.prepareStatement("SELECT activity FROM ChannelActivity WHERE channelID = ?");
            prepStmt.setString(1, channelID);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while executing the select statement");
            e.printStackTrace();
            return null;
        }
    }

    public boolean channelActivityUpdate(String channelID, int activitySetting) {
        try {
            PreparedStatement prepStmt = channelActivityConnection.prepareStatement("UPDATE ChannelActivity SET activity = ? WHERE channelID = ?");
            prepStmt.setInt(1, activitySetting);
            prepStmt.setString(2, channelID);
            if (prepStmt.executeUpdate() != 1) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while updating the values");
            return false;
        }
        return true;
    }

    public boolean updateChannelActivity(String channelID, int activitySetting) throws SQLException {
        if ((activitySetting < 0) || (activitySetting > 2)) {
            return false;
        }
        try {
            int size = 0;
            ResultSet checkIfRecordExists = instance.channelActivitySelect(channelID);
            if (checkIfRecordExists != null) {
                while (checkIfRecordExists.next()) {
                    size++;
                }
            }
            if (size == 0) { //create new record
                instance.channelActivityInsert(channelID, activitySetting);
            } else { //apply new setting
                boolean result = instance.channelActivityUpdate(channelID, activitySetting);
                if (!result) {
                    System.err.println("An error occured while executing the update statement in the ChannelActivity table.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getQuoteCount(int nsfw) {
        try {
            PreparedStatement prepStmt = boBurnhamConnection.prepareStatement("SELECT Count(*) as il FROM Quotes WHERE nsfw = ?");
            prepStmt.setInt(1, nsfw);
            ResultSet results = prepStmt.executeQuery();
            int count = 0;
            if (results != null) {
                while (results.next()) {
                    count = results.getInt("il");
                }
            }
            return count;
        } catch (SQLException e) {
            System.err.println("An error occured while executing the select statement");
            e.printStackTrace();
            return 0;
        }
    }

    public ResultSet getQuotes(int nsfw) {
        try {
            PreparedStatement prepStmt = boBurnhamConnection.prepareStatement("SELECT * FROM Quotes WHERE nsfw = ?");
            prepStmt.setInt(1, nsfw);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while executing the select statement");
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getCases() {
        try {
            PreparedStatement prepStmt = caseOpeningConnection.prepareStatement("SELECT * FROM Cases");
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Cases table.");
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getCaseItems(int caseID) {
        try {
            PreparedStatement prepStmt = caseOpeningConnection.prepareStatement("SELECT DISTINCT itemName, rarity FROM Skins WHERE caseID = ?");
            prepStmt.setInt(1, caseID);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Skins table.");
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getCaseItem(String itemName) {
        try {
            PreparedStatement prepStmt = caseOpeningConnection.prepareStatement("SELECT * FROM Skins WHERE itemName = ?");
            prepStmt.setString(1, itemName);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Skins table.");
            e.printStackTrace();
            return null;
        }
    }

    public int getKnifeCount(int knifeGroup) {
        try {
            PreparedStatement prepStmt = caseOpeningConnection.prepareStatement("SELECT Count(*) as il FROM Knives WHERE knifeGroup = ?");
            prepStmt.setInt(1, knifeGroup);
            ResultSet results = prepStmt.executeQuery();
            int knifeCount = 0;
            while (results.next()) {
                knifeCount = results.getInt("il");
            }
            return knifeCount;

        } catch (SQLException e) {
            System.err.println("An error occured while querying the Knives table.");
            e.printStackTrace();
            return 0;
        }
    }

    public ResultSet getKnives(int knifeGroup) {
        try {
            PreparedStatement prepStmt = caseOpeningConnection.prepareStatement("SELECT * FROM Knives WHERE knifeGroup = ?");
            prepStmt.setInt(1, knifeGroup);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Knives table.");
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getCollections() {
        try {
            PreparedStatement prepStmt = collectionOpeningConnection.prepareStatement("SELECT * FROM Collections");
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Collections table.");
            e.printStackTrace();
            return null;
        }
    }

    public List<TwoStringsPair> getCollectionItems(int collectionID) {
        try {
            List<TwoStringsPair> skins = new ArrayList<>();
            PreparedStatement prepStmt = collectionOpeningConnection.prepareStatement("SELECT DISTINCT itemName, rarity FROM Skins WHERE collectionID = ?");
            prepStmt.setInt(1, collectionID);
            ResultSet results = prepStmt.executeQuery();
            if (results != null) {
                while (results.next()) {
                    skins.add(new TwoStringsPair(results.getString("itemName"), results.getString("rarity")));
                }
                return skins;
            } else return null;

        } catch (SQLException e) {
            System.err.println("An error occured while querying the Skins table.");
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getCollectionItem(String itemName) {
        try {
            PreparedStatement prepStmt = collectionOpeningConnection.prepareStatement("SELECT * FROM Skins WHERE itemName = ?");
            prepStmt.setString(1, itemName);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("An error occured while querying the Skins table.");
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getCollectionRarities(int collectionID) {
        try {
            PreparedStatement prepStmt = collectionOpeningConnection.prepareStatement("SELECT DISTINCT rarity FROM Skins WHERE collectionID = ?");
            prepStmt.setInt(1, collectionID);
            ResultSet results = prepStmt.executeQuery();
            List<String> availableRarities = new ArrayList<>();
            if (results != null) {
                while (results.next()) {
                    String currentRarity = results.getString("rarity");
                    availableRarities.add(currentRarity);
                }
                return availableRarities;
            } else return null;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the Skins table.");
            return null;
        }
    }

    public ResultSet showTimedMessages(String selection, String serverID) {
        try {
            PreparedStatement prepStmt;
            if (selection.startsWith(">")) {
                prepStmt = timedMessagesConnection.prepareStatement("SELECT _id, executionTime, destinationChannelID, messageContent FROM Messages WHERE _id > ? AND serverID = ?");
                prepStmt.setInt(1, 0);
            } else if (selection.startsWith("=")) {
                prepStmt = timedMessagesConnection.prepareStatement("SELECT _id, executionTime, destinationChannelID, messageContent FROM Messages WHERE _id = ? AND serverID = ?");
                prepStmt.setInt(1, Integer.parseInt(selection.substring(1)));
            } else {
                return null;
            }
            prepStmt.setString(2, serverID);
            return prepStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the Messages table.");
            return null;
        }
    }

    public int deleteTimedMessages(String selection, String serverID) {
        try {
            PreparedStatement prepStmtQuery;
            if (selection.startsWith(">")) {
                prepStmtQuery = timedMessagesConnection.prepareStatement("SELECT Count(*) FROM Messages WHERE _id > ? AND serverID = ?");
                prepStmtQuery.setInt(1, 0);
            } else if (selection.startsWith("=")) {
                prepStmtQuery = timedMessagesConnection.prepareStatement("SELECT Count(*) FROM Messages WHERE _id = ? AND serverID = ?");
                prepStmtQuery.setInt(1, Integer.parseInt(selection.substring(1)));
            } else {
                return -1;
            }
            prepStmtQuery.setString(2, serverID);
            ResultSet preQuery = prepStmtQuery.executeQuery();
            while (preQuery.next()) {
                if (preQuery.getInt(1) == 0) {
                    return -1;
                }
            }
            PreparedStatement prepStmtDelete;
            if (selection.startsWith(">")) {
                prepStmtDelete = timedMessagesConnection.prepareStatement("DELETE FROM Messages WHERE _id > ? AND serverID = ?");
                prepStmtDelete.setInt(1, 0);
            } else {
                prepStmtDelete = timedMessagesConnection.prepareStatement("DELETE FROM Messages WHERE _id = ? AND serverID = ?");
                prepStmtDelete.setInt(1, Integer.parseInt(selection.substring(1)));
            }
            prepStmtDelete.setString(2, serverID);
            if (prepStmtDelete.executeUpdate() == 0) {
                System.err.println("An error occured while deleting stuff from the Messages table.");
                return -1;
            }
            Cashew.timedMessagesManager.refresh();
            return 0;
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            System.err.println("An error occured while modifying the Messages table.");
            return 1;
        }
    }

    public int addTimedMessage(String messageContent, String executionTime, String repetitionInterval, String destinationChannelID, String serverID) {
        try {
            PreparedStatement prepStmt = timedMessagesConnection.prepareStatement("INSERT INTO Messages(messageContent, executionTime, repetitionInterval, destinationChannelID, serverID) VALUES(?, ?, ?, ?, ?);");
            prepStmt.setString(1, messageContent);
            prepStmt.setString(2, executionTime);
            prepStmt.setString(3, repetitionInterval);
            prepStmt.setString(4, destinationChannelID);
            prepStmt.setString(5, serverID);
            prepStmt.execute();
            ResultSet insertID = prepStmt.getGeneratedKeys();
            int returnedInsertID = 0;
            if (insertID.next()) {
                returnedInsertID = (int) insertID.getLong(1);
            }
            if (returnedInsertID != 0) {
                Cashew.timedMessagesManager.refresh();
                return returnedInsertID;
            } else return 0;
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            System.err.println("An error occured while modifying the Messages table.");
            return 0;
        }
    }

    public ArrayList<TimedMessage> getTimedMessages() {
        try {
            PreparedStatement prepStmt = timedMessagesConnection.prepareStatement("SELECT messageContent, executionTime, destinationChannelID FROM Messages");
            ResultSet timedMessages = prepStmt.executeQuery();
            ArrayList<TimedMessage> timedMessagesArrayList = new ArrayList<>();
            if (timedMessages != null) {
                while (timedMessages.next()) {
                    timedMessagesArrayList.add(new TimedMessage(timedMessages.getString(1), timedMessages.getString(2), 86400 * 1000, timedMessages.getString(3)));
                }
            }
            return timedMessagesArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the Messages table.");
            return new ArrayList<>();
        }
    }

    public int getSocialCredit(String userID, String serverID) {
        try {
            PreparedStatement prepStmt = socialCreditConnection.prepareStatement("SELECT credit FROM SocialCredit WHERE serverID = ? AND userID = ?");
            prepStmt.setString(1, serverID);
            prepStmt.setString(2, userID);
            ResultSet socialCredit = prepStmt.executeQuery();
            if (socialCredit != null) {
                if (socialCredit.next()) {
                    return socialCredit.getInt(1);
                }
                return 648294745;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the SocialCredit table.");
            return 0;
        }
        return 0;
    }

    public void addSocialCredit(String userID, String serverID, int credit) {
        try {
            int socialCredit = getSocialCredit(userID, serverID);
            if (socialCredit == 648294745) {
                newSocialCredit(userID, serverID, credit);
            } else {
                PreparedStatement prepStmt = socialCreditConnection.prepareStatement("UPDATE SocialCredit SET credit = ? WHERE serverID = ? AND userID = ?");
                prepStmt.setInt(1, credit + socialCredit);
                prepStmt.setString(2, serverID);
                prepStmt.setString(3, userID);
                prepStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the SocialCredit table.");
        }
    }

    public void newSocialCredit(String userID, String serverID, int credit) {
        try {
            PreparedStatement prepStmt = socialCreditConnection.prepareStatement("INSERT INTO SocialCredit(serverID, userID, credit) VALUES(?, ?, ?);");
            prepStmt.setString(1, serverID);
            prepStmt.setString(2, userID);
            prepStmt.setInt(3, credit);
            prepStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while inserting into the SocialCredit table.");
        }
    }

    public boolean setCountingStatus(boolean newState, String channelID) {
        try {
            PreparedStatement prepStmt = countingConnection.prepareStatement("SELECT Count(*) FROM Counting WHERE channelID = ?");
            prepStmt.setString(1, channelID);
            ResultSet result = prepStmt.executeQuery();
            boolean createNewRecord = false;
            while (result.next()) {
                if (result.getInt(1) == 0) {
                    createNewRecord = true;
                    break;
                }
            }
            if (createNewRecord) {
                prepStmt = countingConnection.prepareStatement("INSERT INTO Counting(channelID, activity, current) VALUES(? ,?, ?)");
                prepStmt.setString(1, channelID);
                prepStmt.setBoolean(2, newState);
                prepStmt.setInt(3, 0);
            } else {
                prepStmt = countingConnection.prepareStatement("UPDATE Counting SET activity = ? WHERE channelID = ?");
                prepStmt.setBoolean(1, newState);
                prepStmt.setString(2, channelID);
            }
            prepStmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying into the Counting table.");
            return false;
        }
    }

    public CountingInfo getCountingData(String channelID) {
        try {
            PreparedStatement prepStmt = countingConnection.prepareStatement("SELECT activity, userID, current, messageID FROM Counting WHERE channelID = ?");
            prepStmt.setString(1, channelID);
            ResultSet result = prepStmt.executeQuery();
            if(result.next()) {
                return new CountingInfo(result.getBoolean(1), result.getString(2), result.getInt(3), result.getString(4));
            }
            return new CountingInfo(false, " ", 0, " ");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying into the Counting table.");
            return new CountingInfo(false, " ", 0, " ");
        }
    }

    public void setCount(CountingInfo countingInfo, String channelID) {
        try {
            PreparedStatement prepStmt = countingConnection.prepareStatement("UPDATE Counting SET current = ?, userID = ?, messageID = ? WHERE channelID = ?");
            prepStmt.setInt(1, countingInfo.getValue());
            prepStmt.setString(2, countingInfo.getUserID());
            prepStmt.setString(3, countingInfo.getMessageID());
            prepStmt.setString(4, channelID);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while inserting into the Counting table.");
        }
    }

    public ArrayList<GiftInfo> getAvailableGifts() {
        try {
            PreparedStatement prepStmt = giftsConnection.prepareStatement("SELECT giftID, giftName, giftImageURL FROM Gifts");
            ResultSet results = prepStmt.executeQuery();
            ArrayList<GiftInfo> availableGifts = new ArrayList<>();
            if(results == null) {
                return availableGifts;
            }
            while(results.next()) {
                availableGifts.add(new GiftInfo(results.getInt(1), results.getString(2), results.getString(3)));
            }
            return availableGifts;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void insertNewGiftStats(GiftStats stats, int giftID, String userID, String serverID) {
        try {
            PreparedStatement prepStmt = giftsConnection.prepareStatement("INSERT INTO GiftHistory(giftID, userID, serverID, amountGifted, amountReceived, lastGifted) VALUES(?, ?, ?, ?, ?, ?)");
            prepStmt.setInt(1, giftID);
            prepStmt.setString(2, userID);
            prepStmt.setString(3, serverID);
            prepStmt.setInt(4, stats.getAmountGifted());
            prepStmt.setInt(5, stats.getAmountReceived());
            prepStmt.setLong(6, stats.getLastGifted());
            prepStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GiftStats getUserGiftStats(int giftID, String userID, String serverID) {
        try {
            PreparedStatement prepStmt = giftsConnection.prepareStatement("SELECT amountGifted, amountReceived, lastGifted FROM GiftHistory WHERE ((serverID = ? AND userID = ?) AND giftID = ?)");
            prepStmt.setString(1, serverID);
            prepStmt.setString(2, userID);
            prepStmt.setInt(3, giftID);
            ResultSet results = prepStmt.executeQuery();
            if(results == null) {
                return null;
            }
            if(results.next()) {
                return new GiftStats(results.getInt(1), results.getInt(2), results.getLong(3));
            } else {
                GiftStats newGiftStats = new GiftStats(0, 0, 0);
                insertNewGiftStats(newGiftStats, giftID, userID, serverID);
                return newGiftStats;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateUserGiftStats(GiftStats stats, int giftID, String userID, String serverID) {

    }
}