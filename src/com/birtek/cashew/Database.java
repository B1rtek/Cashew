package com.birtek.cashew;

import com.birtek.cashew.commands.GiftInfo;
import com.birtek.cashew.commands.GiftStats;
import com.birtek.cashew.commands.SkinInfo;
import com.birtek.cashew.commands.TwoStringsPair;
import com.birtek.cashew.messagereactions.CountingInfo;
import com.birtek.cashew.timings.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static volatile Database instance;

    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String CHANNEL_ACTIVITY_DB = "jdbc:sqlite:databases/userData/channelActivity.db";
    public static final String BO_BURNHAM_DB = "jdbc:sqlite:databases/data/boBurnhamQuotes.db";
    public static final String CASE_OPENING_DB = "jdbc:sqlite:databases/data/caseOpening.db";
    public static final String COLLECTION_OPENING_DB = "jdbc:sqlite:databases/data/collectionOpening.db";
    public static final String TIMED_MESSAGES_DB = "jdbc:sqlite:databases/userData/timedMessages.db";
    public static final String SOCIALCREDIT_DB = "jdbc:sqlite:databases/userData/socialCredit.db";
    public static final String COUNTING_DB = "jdbc:sqlite:databases/userData/counting.db";
    public static final String GIFTS_DB = "jdbc:sqlite:databases/data/gifts.db";
    public static final String GIFT_HISTORY_DB = "jdbc:sqlite:databases/userData/giftHistory.db";
    public static final String CASESIM_CASES_DB = "jdbc:sqlite:databases/data/casesimCases.db";
    public static final String CASESIM_COLLECTIONS_DB = "jdbc:sqlite:databases/data/casesimCollections.db";
    public static final String CASESIM_CAPSULES_DB = "jdbc:sqlite:databases/data/casesimCapsules.db";
    public static final String BIRTHDAY_REMINDSRS_DB = "jdbc:sqlite:databases/userData/birthdayReminders.db";
    public static final String REMINDERS_DB = "jdbc:sqlite:databases/userData/reminders.db";

    private Connection channelActivityConnection;
    private Statement channelActivityStatement;
    private Connection boBurnhamConnection;
    private Connection caseOpeningConnection;
    private Connection collectionOpeningConnection;
    private Connection timedMessagesConnection;
    private Connection socialCreditConnection;
    private Connection countingConnection;
    private Connection giftsConnection;
    private Connection giftHistoryConnection;
    private Connection casesimCasesConnection;
    private Connection casesimCollectionsConnection;
    private Connection casesimCapsulesConnection;
    private Connection birthdayRemindersConnection;
    private Connection remindersConnection;

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
            giftHistoryConnection = DriverManager.getConnection(GIFT_HISTORY_DB);
            casesimCasesConnection = DriverManager.getConnection(CASESIM_CASES_DB);
            casesimCollectionsConnection = DriverManager.getConnection(CASESIM_COLLECTIONS_DB);
            casesimCapsulesConnection = DriverManager.getConnection(CASESIM_CAPSULES_DB);
            birthdayRemindersConnection = DriverManager.getConnection(BIRTHDAY_REMINDSRS_DB);
            remindersConnection = DriverManager.getConnection(REMINDERS_DB);
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
                prepStmtQuery = timedMessagesConnection.prepareStatement("SELECT Count(*), _id FROM Messages WHERE _id > ? AND serverID = ?");
                prepStmtQuery.setInt(1, 0);
            } else if (selection.startsWith("=")) {
                prepStmtQuery = timedMessagesConnection.prepareStatement("SELECT Count(*), _id FROM Messages WHERE _id = ? AND serverID = ?");
                prepStmtQuery.setInt(1, Integer.parseInt(selection.substring(1)));
            } else {
                return -1;
            }
            prepStmtQuery.setString(2, serverID);
            ResultSet preQuery = prepStmtQuery.executeQuery();
            ArrayList<Integer> idsToDelete = new ArrayList<>();
            while (preQuery.next()) {
                if (preQuery.getInt(1) == 0) {
                    return -1;
                }
                idsToDelete.add(preQuery.getInt(2));
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
            for(int id: idsToDelete) {
                Cashew.scheduledMessagesManager.deleteScheduledMessage(id);
            }
            return 0;
        } catch (SQLException e) {
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
                Cashew.scheduledMessagesManager.addScheduledMessage(new ScheduledMessage(returnedInsertID, messageContent, executionTime, destinationChannelID));
                return returnedInsertID;
            } else return 0;
        } catch (SQLException e) {
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
            return null;
        }
    }

    public ArrayList<ScheduledMessage> getScheduledMessages() {
        try {
            PreparedStatement prepStmt = timedMessagesConnection.prepareStatement("SELECT _id, messageContent, executionTime, destinationChannelID FROM Messages");
            ResultSet scheduledMessages = prepStmt.executeQuery();
            ArrayList<ScheduledMessage> scheduledMessageArrayList = new ArrayList<>();
            if(scheduledMessages != null) {
                while(scheduledMessages.next()) {
                    scheduledMessageArrayList.add(new ScheduledMessage(scheduledMessages.getInt(1), scheduledMessages.getString(2), scheduledMessages.getString(3), scheduledMessages.getString(4)));
                }
            }
            return scheduledMessageArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the Messages table.");
            return null;
        }
    }

    public long getSocialCredit(String userID, String serverID) {
        try {
            PreparedStatement prepStmt = socialCreditConnection.prepareStatement("SELECT credit FROM SocialCredit WHERE serverID = ? AND userID = ?");
            prepStmt.setString(1, serverID);
            prepStmt.setString(2, userID);
            ResultSet socialCredit = prepStmt.executeQuery();
            if (socialCredit != null) {
                if (socialCredit.next()) {
                    return socialCredit.getLong(1);
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

    public void addSocialCredit(String userID, String serverID, long credit) {
        try {
            long socialCredit = getSocialCredit(userID, serverID);
            if (socialCredit == 648294745) {
                newSocialCredit(userID, serverID, credit);
            } else {
                PreparedStatement prepStmt = socialCreditConnection.prepareStatement("UPDATE SocialCredit SET credit = ? WHERE serverID = ? AND userID = ?");
                prepStmt.setLong(1, credit + socialCredit);
                prepStmt.setString(2, serverID);
                prepStmt.setString(3, userID);
                prepStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying the SocialCredit table.");
        }
    }

    public void newSocialCredit(String userID, String serverID, long credit) {
        try {
            PreparedStatement prepStmt = socialCreditConnection.prepareStatement("INSERT INTO SocialCredit(serverID, userID, credit) VALUES(?, ?, ?);");
            prepStmt.setString(1, serverID);
            prepStmt.setString(2, userID);
            prepStmt.setLong(3, credit);
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
                prepStmt = countingConnection.prepareStatement("INSERT INTO Counting(channelID, activity, current, typosLeft) VALUES(? ,?, ?, ?)");
                prepStmt.setString(1, channelID);
                prepStmt.setBoolean(2, newState);
                prepStmt.setInt(3, 0);
                prepStmt.setInt(4, 3);
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
            PreparedStatement prepStmt = countingConnection.prepareStatement("SELECT activity, userID, current, messageID, typosLeft FROM Counting WHERE channelID = ?");
            prepStmt.setString(1, channelID);
            ResultSet result = prepStmt.executeQuery();
            if (result.next()) {
                return new CountingInfo(result.getBoolean(1), result.getString(2), result.getInt(3), result.getString(4), result.getInt(5));
            }
            return new CountingInfo(false, " ", 0, " ", 3);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while querying into the Counting table.");
            return new CountingInfo(false, " ", 0, " ", 3);
        }
    }

    public void setCount(CountingInfo countingInfo, String channelID) {
        try {
            PreparedStatement prepStmt = countingConnection.prepareStatement("UPDATE Counting SET current = ?, userID = ?, messageID = ?, typosLeft = ? WHERE channelID = ?");
            prepStmt.setInt(1, countingInfo.value());
            prepStmt.setString(2, countingInfo.userID());
            prepStmt.setString(3, countingInfo.messageID());
            prepStmt.setInt(4, countingInfo.typosLeft());
            prepStmt.setString(5, channelID);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("An error occured while inserting into the Counting table.");
        }
    }

    public ArrayList<String> getAllActiveCountingChannels() {
        try {
            PreparedStatement preparedStatement = countingConnection.prepareStatement("SELECT channelID FROM Counting WHERE activity = 1");
            ResultSet results = preparedStatement.executeQuery();
            return createArrayListFromResultSet(results);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GiftInfo> getAvailableGifts() {
        try {
            PreparedStatement prepStmt = giftsConnection.prepareStatement("SELECT giftID, giftName, giftImageURL, reactionLine1, reactionLine2, displayName FROM Gifts");
            ResultSet results = prepStmt.executeQuery();
            ArrayList<GiftInfo> availableGifts = new ArrayList<>();
            if (results == null) {
                return availableGifts;
            }
            while (results.next()) {
                availableGifts.add(new GiftInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6)));
            }
            return availableGifts;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void insertNewGiftStats(GiftStats stats, int giftID, String userID, String serverID) {
        try {
            PreparedStatement prepStmt = giftHistoryConnection.prepareStatement("INSERT INTO GiftHistory(giftID, userID, serverID, amountGifted, amountReceived, lastGifted) VALUES(?, ?, ?, ?, ?, ?)");
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
            PreparedStatement prepStmt;
            if (giftID != 0) {
                prepStmt = giftHistoryConnection.prepareStatement("SELECT amountGifted, amountReceived, lastGifted FROM GiftHistory WHERE ((serverID = ? AND userID = ?) AND giftID = ?)");
                prepStmt.setString(1, serverID);
                prepStmt.setString(2, userID);
                prepStmt.setInt(3, giftID);
            } else {
                prepStmt = giftHistoryConnection.prepareStatement("SELECT SUM(amountGifted), SUM(amountReceived), AVG(lastGifted) FROM GiftHistory WHERE (serverID = ? AND userID = ?)");
                prepStmt.setString(1, serverID);
                prepStmt.setString(2, userID);
            }
            ResultSet results = prepStmt.executeQuery();
            if (results == null) {
                return null;
            }
            if (results.next()) {
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
        try {
            PreparedStatement prepStmt = giftHistoryConnection.prepareStatement("UPDATE GiftHistory SET amountGifted = ?, amountReceived = ?, lastGifted = ? WHERE ((serverID = ? AND userID = ?) AND giftID = ?)");
            prepStmt.setInt(1, stats.getAmountGifted());
            prepStmt.setInt(2, stats.getAmountReceived());
            prepStmt.setLong(3, stats.getLastGifted());
            prepStmt.setString(4, serverID);
            prepStmt.setString(5, userID);
            prepStmt.setInt(6, giftID);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> createArrayListFromResultSet(ResultSet results) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        if (results == null) {
            return list;
        }
        while (results.next()) {
            list.add(results.getString(1));
        }
        return list;
    }

    public ArrayList<String> getCasesimCases() {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT name FROM Cases");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getCasesimCollections() {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT name FROM Collections");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getCasesimCapsules() {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT name FROM Capsules");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<SkinInfo> getCaseSkins(String caseName) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT _id FROM Cases WHERE name = ?");
            preparedStatement.setString(1, caseName);
            ResultSet results = preparedStatement.executeQuery();
            int caseId = -1;
            if (results.next()) {
                caseId = results.getInt(1);
            }
            if (caseId == -1) {
                return null;
            }
            preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Skins WHERE caseId = ?");
            preparedStatement.setInt(1, caseId);
            results = preparedStatement.executeQuery();
            ArrayList<SkinInfo> skins = new ArrayList<>();
            while (results.next()) {
                skins.add(new SkinInfo(results.getInt(2), results.getString(3), results.getInt(4), results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18)));
            }
            if(skins.isEmpty()) {
                return null;
            }
            return skins;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<BirthdayReminder> getBirthdayReminders() {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT * FROM Reminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminder> reminders = new ArrayList<>();
            while(results.next()) {
                reminders.add(new BirthdayReminder(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6)));
            }
            return reminders;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<BirthdayReminderDefaults> getBirthdayReminderDefaults() {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT serverID, channelID, override FROM DefaultChannels");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminderDefaults> defaults = new ArrayList<>();
            while(results.next()) {
                defaults.add(new BirthdayReminderDefaults(results.getString(1), results.getString(2), results.getBoolean(3)));
            }
            return defaults;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getBirthdayReminderId(String serverID, String userID) throws SQLException {
        PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT _id FROM Reminders WHERE serverID = ? AND userID = ?");
        preparedStatement.setString(1, serverID);
        preparedStatement.setString(2, userID);
        ResultSet results = preparedStatement.executeQuery();
        int id = 0;
        if(results.next()) {
            id = results.getInt(1);
        }
        return id;
    }

    public boolean addBirthdayReminder(BirthdayReminder reminder) {
        try {
            int id = getBirthdayReminderId(reminder.getServerID(), reminder.getUserID());
            if(id == 0) { // new record
                reminder = insertBirthdayReminder(reminder);
                if(reminder != null) {
                    Cashew.birthdayRemindersManager.addBirthdayReminder(reminder);
                    return true;
                }
                return false;
            } else { // record update
                reminder.setId(id);
                if(this.updateBirthdayReminder(reminder)) {
                    Cashew.birthdayRemindersManager.updateBirthdayReminder(reminder);
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private BirthdayReminder insertBirthdayReminder(BirthdayReminder reminder) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("INSERT INTO Reminders(message, dateAndTime, channelID, serverID, userID) VALUES(?, ?, ?, ?, ?)");
            preparedStatement.setString(1, reminder.getMessage());
            preparedStatement.setString(2, reminder.getDateAndTime());
            preparedStatement.setString(3, reminder.getChannelID());
            preparedStatement.setString(4, reminder.getServerID());
            preparedStatement.setString(5, reminder.getUserID());
            preparedStatement.execute();
            ResultSet id = preparedStatement.getGeneratedKeys();
            if(id.next()) {
                reminder.setId(id.getInt(1));
                return reminder;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean updateBirthdayReminder(BirthdayReminder reminder) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("UPDATE Reminders SET message = ?, dateAndTime = ?, channelID = ?, serverID = ?, userID = ? WHERE _id = ?");
            preparedStatement.setString(1, reminder.getMessage());
            preparedStatement.setString(2, reminder.getDateAndTime());
            preparedStatement.setString(3, reminder.getChannelID());
            preparedStatement.setString(4, reminder.getServerID());
            preparedStatement.setString(5, reminder.getUserID());
            preparedStatement.setInt(6, reminder.getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBirthdayReminder(String serverID, String userID) {
        try {
            int id = getBirthdayReminderId(serverID, userID);
            if(id == 0) return false;
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("DELETE FROM Reminders WHERE serverID = ? AND userID = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            preparedStatement.execute();
            Cashew.birthdayRemindersManager.deleteBirthdayReminder(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT _id FROM DefaultChannels WHERE serverID = ?");
            preparedStatement.setString(1, defaults.serverID());
            ResultSet results = preparedStatement.executeQuery();
            int id = 0;
            if(results.next()) {
                id = results.getInt(1);
            }
            if(id == 0) { // new record
                if(insertBirthdayRemindersDefaults(defaults)) {
                    Cashew.birthdayRemindersManager.updateBirthdayRemindersDefaults(defaults);
                    return true;
                }
            } else {
                if(updateBirthdayRemindersDefaults(defaults)) {
                    Cashew.birthdayRemindersManager.updateBirthdayRemindersDefaults(defaults);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean insertBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("INSERT INTO DefaultChannels(serverID, channelID, override) VALUES(?, ?, ?)");
            preparedStatement.setString(1, defaults.serverID());
            preparedStatement.setString(2, defaults.channelID());
            preparedStatement.setBoolean(3, defaults.override());
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("UPDATE DefaultChannels SET channelID = ?, override = ? WHERE serverID = ?");
            preparedStatement.setString(1, defaults.channelID());
            preparedStatement.setBoolean(2, defaults.override());
            preparedStatement.setString(3, defaults.serverID());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public BirthdayReminder getBirthdayReminder(String userID, String serverID) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT message, dateAndTime, channelID FROM Reminders WHERE userID = ? AND serverID = ?");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return new BirthdayReminder(0, results.getString(1), results.getString(2), results.getString(3), serverID, userID);
            }
            return new BirthdayReminder(-1, "", "", "", "", "");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BirthdayReminderDefaults getBirthdayReminderDefault(String serverID) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT channelID, override FROM DefaultChannels WHERE serverID = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return new BirthdayReminderDefaults(serverID, results.getString(1), results.getBoolean(2));
            }
            return new BirthdayReminderDefaults("", "", false);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReminderRunnable addReminder(ReminderRunnable reminder) {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("INSERT INTO Reminders(content, timedate, userID, ping) VALUES(?, ?, ?, ?)");
            preparedStatement.setString(1, reminder.getContent());
            preparedStatement.setString(2, reminder.getDateTime());
            preparedStatement.setString(3, reminder.getUserID());
            preparedStatement.setBoolean(4, reminder.isPing());
            preparedStatement.execute();
            ResultSet id = preparedStatement.getGeneratedKeys();
            if(id.next()) {
                reminder.setId(id.getInt(1));
                Cashew.remindersManager.addReminder(reminder);
                return reminder;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<ReminderRunnable> getReminders(String userID) {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT _id, content, timedate, ping FROM Reminders where userID = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while(results.next()) {
                reminders.add(new ReminderRunnable(results.getInt(1), results.getBoolean(4), results.getString(2), results.getString(3), userID));
            }
            return reminders;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<ReminderRunnable> getAllReminders() {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT _id, content, timedate, ping, userID FROM Reminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while(results.next()) {
                reminders.add(new ReminderRunnable(results.getInt(1), results.getBoolean(4), results.getString(2), results.getString(3), results.getString(5)));
            }
            return reminders;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getRemindersCount(String userID) {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT COUNT(*) FROM Reminders WHERE userID = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return results.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteReminder(int id, String userID) {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT COUNT(*) FROM Reminders where _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                if(results.getInt(1) == 0) return 0;
            } else return -1;
            preparedStatement = remindersConnection.prepareStatement("DELETE FROM Reminders WHERE _id = ? AND userID = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, userID);
            int rowsDeleted = preparedStatement.executeUpdate();
            if(rowsDeleted != 0) {
                Cashew.remindersManager.deleteReminder(id);
                return 1;
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}