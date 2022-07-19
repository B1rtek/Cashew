package com.birtek.cashew;

import com.birtek.cashew.commands.*;
import com.birtek.cashew.timings.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The old Database class, contains only old pieces of code that are no longer used
 */
public final class Database {

    private static volatile Database instance;

    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    public static final String POSTGRES_DRIVER = "org.sqlite.JDBC";
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
    public static final String POLLS_DB = "jdbc:sqlite:databases/userData/polls.db";
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
    private Connection pollsConnection;

    private Database() {

        // SQLite
        try {
            Class.forName(Database.POSTGRES_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Missing SQLite JDBC driver");
            e.printStackTrace();
        }

        try {
            caseOpeningConnection = DriverManager.getConnection(CASE_OPENING_DB);
            collectionOpeningConnection = DriverManager.getConnection(COLLECTION_OPENING_DB);
            giftsConnection = DriverManager.getConnection(GIFTS_DB);
            casesimCasesConnection = DriverManager.getConnection(CASESIM_CASES_DB);
            casesimCollectionsConnection = DriverManager.getConnection(CASESIM_COLLECTIONS_DB);
            casesimCapsulesConnection = DriverManager.getConnection(CASESIM_CAPSULES_DB);
        } catch (SQLException e) {
            System.err.println("There was a problem while establishing a connection with the SQLite3 databases");
            e.printStackTrace();
        }

        // PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Missing PostgreSQL JDBC driver");
            e.printStackTrace();
        }

        // Postgres
        try {
            String postgresDBUrl = System.getenv("JDBC_DATABASE_URL");
            timedMessagesConnection = DriverManager.getConnection(postgresDBUrl);
            socialCreditConnection = DriverManager.getConnection(postgresDBUrl);
            countingConnection = DriverManager.getConnection(postgresDBUrl);
            giftHistoryConnection = DriverManager.getConnection(postgresDBUrl);
            birthdayRemindersConnection = DriverManager.getConnection(postgresDBUrl);
            remindersConnection = DriverManager.getConnection(postgresDBUrl);
            pollsConnection = DriverManager.getConnection(postgresDBUrl);
        } catch (SQLException e) {
            System.err.println("There was a problem while establishing a connection with the Postgres databases");
            e.printStackTrace();
        }
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

    public ArrayList<TimedMessage> getTimedMessages() {
        try {
            PreparedStatement prepStmt = timedMessagesConnection.prepareStatement("SELECT messagecontent, executiontime, destinationchannelid FROM scheduledmessages");
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

    private void insertNewGiftStats(GiftStats stats, int giftID, String userID, String serverID) {
        try {
            PreparedStatement prepStmt = giftHistoryConnection.prepareStatement("INSERT INTO gifthistory(giftid, userid, serverid, amountgifted, amountreceived, lastgifted) VALUES(?, ?, ?, ?, ?, ?)");
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
                prepStmt = giftHistoryConnection.prepareStatement("SELECT amountgifted, amountreceived, lastgifted FROM gifthistory WHERE ((serverid = ? AND userid = ?) AND giftid = ?)");
                prepStmt.setString(1, serverID);
                prepStmt.setString(2, userID);
                prepStmt.setInt(3, giftID);
            } else {
                prepStmt = giftHistoryConnection.prepareStatement("SELECT SUM(amountgifted), SUM(amountreceived), AVG(lastgifted) FROM gifthistory WHERE (serverid = ? AND userid = ?)");
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
            PreparedStatement prepStmt = giftHistoryConnection.prepareStatement("UPDATE gifthistory SET amountgifted = ?, amountreceived = ?, lastgifted = ? WHERE ((serverid = ? AND userid = ?) AND giftid = ?)");
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

    public ArrayList<LeaderboardRecord> getGiftsLeaderboardPage(Gifts.GiftsLeaderboardType type, int page, String serverID, int giftID) {
        String selectedColumn;
        if (type == Gifts.GiftsLeaderboardType.MOST_GIFTED) {
            selectedColumn = "amountgifted";
        } else {
            selectedColumn = "amountreceived";
        }
        try {
            ArrayList<LeaderboardRecord> leaderboardRecords = new ArrayList<>();
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = giftHistoryConnection.prepareStatement("select pos, userid, " + selectedColumn + " from (select ROW_NUMBER() over (order by " + selectedColumn + " desc) pos, userid, " + selectedColumn + " from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0 order by " + selectedColumn + " desc) as subqr where pos between (?-1)*10+1 and (?-1)*10+10");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, giftID);
                preparedStatement.setInt(3, page);
                preparedStatement.setInt(4, page);
            } else {
                preparedStatement = giftHistoryConnection.prepareStatement("select pos, userid, total from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where pos between (?-1)*10+1 and (?-1)*10+10 and total > 0");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, page);
                preparedStatement.setInt(3, page);
            }
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                leaderboardRecords.add(new LeaderboardRecord(results.getInt(1), results.getString(2), results.getInt(3)));
            }
            return leaderboardRecords;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LeaderboardRecord getGiftsLeaderboardStats(Gifts.GiftsLeaderboardType type, String serverID, int giftID, String userID) {
        String selectedColumn;
        if (type == Gifts.GiftsLeaderboardType.MOST_GIFTED) {
            selectedColumn = "amountgifted";
        } else {
            selectedColumn = "amountreceived";
        }
        try {
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = giftHistoryConnection.prepareStatement("select pos, " + selectedColumn + " from (select ROW_NUMBER() over (order by " + selectedColumn + " desc) pos, userid, " + selectedColumn + " from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0 order by " + selectedColumn + " desc) as subqr where userid = ?");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, giftID);
                preparedStatement.setString(3, userID);
            } else {
                preparedStatement = giftHistoryConnection.prepareStatement("select pos, total from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where userid = ? AND total > 0");
                preparedStatement.setString(1, serverID);
                preparedStatement.setString(2, userID);
            }
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new LeaderboardRecord(results.getInt(1), userID, results.getInt(2));
            }
            return new LeaderboardRecord(0, userID, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getGiftsLeaderboardPageCount(Gifts.GiftsLeaderboardType type, String serverID, int giftID) {
        String selectedColumn;
        if (type == Gifts.GiftsLeaderboardType.MOST_GIFTED) {
            selectedColumn = "amountgifted";
        } else {
            selectedColumn = "amountreceived";
        }
        try {
            ArrayList<LeaderboardRecord> leaderboardRecords = new ArrayList<>();
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = giftHistoryConnection.prepareStatement("select COUNT(*) from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, giftID);
            } else {
                preparedStatement = giftHistoryConnection.prepareStatement("select COUNT(*) from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where total > 0");
                preparedStatement.setString(1, serverID);
            }
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1)/10+(results.getInt(1)%10==0?0:1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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

    public CaseInfo getCaseInfo(String caseName) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Cases WHERE name = ?");
            preparedStatement.setString(1, caseName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getInt(5));
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CaseInfo getCollectionInfo(String collectionName) {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT * FROM Collections WHERE name = ?");
            preparedStatement.setString(1, collectionName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), 0);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CaseInfo getCapsuleInfo(String capsuleName) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Capsules WHERE name = ?");
            preparedStatement.setString(1, capsuleName);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new CaseInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), 0);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<SkinInfo> getSkinsFromResultSet(ResultSet results) throws SQLException {
        ArrayList<SkinInfo> skins = new ArrayList<>();
        while (results.next()) {
            skins.add(new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], results.getFloat(5), results.getFloat(6), results.getString(7), results.getString(8), results.getString(9), results.getString(10), results.getString(11), results.getString(12), results.getString(13), results.getString(14), results.getString(15), results.getString(16), results.getString(17), results.getString(18)));
        }
        if (skins.isEmpty()) {
            return null;
        }
        return skins;
    }

    public ArrayList<SkinInfo> getCaseSkins(CaseInfo caseInfo) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * FROM Skins WHERE caseId = ?");
            preparedStatement.setInt(1, caseInfo.caseId());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SkinInfo> getCaseKnives(CaseInfo caseInfo) {
        try {
            PreparedStatement preparedStatement = casesimCasesConnection.prepareStatement("SELECT * From Knives where knifeGroup = ?");
            preparedStatement.setInt(1, caseInfo.knifeGroup());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SkinInfo> getCollectionSkins(CaseInfo collectionInfo) {
        try {
            PreparedStatement preparedStatement = casesimCollectionsConnection.prepareStatement("SELECT * FROM Skins WHERE collectionId = ?");
            preparedStatement.setInt(1, collectionInfo.caseId());
            return getSkinsFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SkinInfo> getCapsuleItems(CaseInfo capsuleInfo) {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT * FROM Stickers WHERE capsuleId = ?");
            preparedStatement.setInt(1, capsuleInfo.caseId());
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<SkinInfo> items = new ArrayList<>();
            while (results.next()) {
                items.add(new SkinInfo(results.getInt(1), results.getInt(2), results.getString(3), CaseSim.SkinRarity.values()[results.getInt(4)], 0.0f, 0.0f, "", "", "", results.getString(5), "", "", results.getString(6), "", "", "", "", results.getString(7)));
            }
            if (items.isEmpty()) {
                return null;
            }
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<BirthdayReminder> getBirthdayReminders() {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT * FROM birthdayreminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminder> reminders = new ArrayList<>();
            while (results.next()) {
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT serverid, channelid, override FROM defaultbirthdayreminderchannels");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminderDefaults> defaults = new ArrayList<>();
            while (results.next()) {
                defaults.add(new BirthdayReminderDefaults(results.getString(1), results.getString(2), results.getBoolean(3)));
            }
            return defaults;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getBirthdayReminderId(String serverID, String userID) throws SQLException {
        PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT _id FROM birthdayreminders WHERE serverid = ? AND userid = ?");
        preparedStatement.setString(1, serverID);
        preparedStatement.setString(2, userID);
        ResultSet results = preparedStatement.executeQuery();
        int id = 0;
        if (results.next()) {
            id = results.getInt(1);
        }
        return id;
    }

    public boolean addBirthdayReminder(BirthdayReminder reminder) {
        try {
            int id = getBirthdayReminderId(reminder.getServerID(), reminder.getUserID());
            if (id == 0) { // new record
                reminder = insertBirthdayReminder(reminder);
                if (reminder != null) {
                    Cashew.birthdayRemindersManager.addBirthdayReminder(reminder);
                    return true;
                }
                return false;
            } else { // record update
                reminder.setId(id);
                if (this.updateBirthdayReminder(reminder)) {
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("INSERT INTO birthdayreminders(message, dateandtime, channelid, serverid, userid) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reminder.getMessage());
            preparedStatement.setString(2, reminder.getDateAndTime());
            preparedStatement.setString(3, reminder.getChannelID());
            preparedStatement.setString(4, reminder.getServerID());
            preparedStatement.setString(5, reminder.getUserID());
            preparedStatement.execute();
            ResultSet id = preparedStatement.getGeneratedKeys();
            if (id.next()) {
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("UPDATE birthdayreminders SET message = ?, dateandtime = ?, channelid = ?, serverid = ?, userid = ? WHERE _id = ?");
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
            if (id == 0) return false;
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("DELETE FROM birthdayreminders WHERE serverid = ? AND userid = ?");
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT _id FROM defaultbirthdayreminderchannels WHERE serverid = ?");
            preparedStatement.setString(1, defaults.serverID());
            ResultSet results = preparedStatement.executeQuery();
            int id = 0;
            if (results.next()) {
                id = results.getInt(1);
            }
            if (id == 0) { // new record
                if (insertBirthdayRemindersDefaults(defaults)) {
                    Cashew.birthdayRemindersManager.updateBirthdayRemindersDefaults(defaults);
                    return true;
                }
            } else {
                if (updateBirthdayRemindersDefaults(defaults)) {
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("INSERT INTO defaultbirthdayreminderchannels(serverid, channelid, override) VALUES(?, ?, ?)");
            preparedStatement.setString(1, defaults.serverID());
            preparedStatement.setString(2, defaults.channelID());
            preparedStatement.setInt(3, defaults.override()?1:0);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("UPDATE defaultbirthdayreminderchannels SET channelid = ?, override = ? WHERE serverid = ?");
            preparedStatement.setString(1, defaults.channelID());
            preparedStatement.setInt(2, defaults.override()?1:0);
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT message, dateandtime, channelid FROM birthdayreminders WHERE userid = ? AND serverid = ?");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
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
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT channelid, override FROM defaultbirthdayreminderchannels WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new BirthdayReminderDefaults(serverID, results.getString(1), results.getBoolean(2));
            }
            return new BirthdayReminderDefaults("", "", false);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<BirthdayReminder> getBirthdayRemindersFromServer(String serverID) {
        try {
            PreparedStatement preparedStatement = birthdayRemindersConnection.prepareStatement("SELECT * FROM birthdayreminders WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminder> reminders = new ArrayList<>();
            while (results.next()) {
                reminders.add(new BirthdayReminder(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6)));
            }
            return reminders;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReminderRunnable addReminder(ReminderRunnable reminder) {
        try {
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("INSERT INTO reminders(content, timedate, userid, ping) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reminder.getContent());
            preparedStatement.setString(2, reminder.getDateTime());
            preparedStatement.setString(3, reminder.getUserID());
            preparedStatement.setInt(4, reminder.isPing()?1:0);
            preparedStatement.execute();
            ResultSet id = preparedStatement.getGeneratedKeys();
            if (id.next()) {
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
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT _id, content, timedate, ping FROM reminders where userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while (results.next()) {
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
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT _id, content, timedate, ping, userid FROM reminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while (results.next()) {
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
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT COUNT(*) FROM reminders WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
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
            PreparedStatement preparedStatement = remindersConnection.prepareStatement("SELECT COUNT(*) FROM reminders where _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                if (results.getInt(1) == 0) return 0;
            } else return -1;
            preparedStatement = remindersConnection.prepareStatement("DELETE FROM reminders WHERE _id = ? AND userid = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, userID);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted != 0) {
                Cashew.remindersManager.deleteReminder(id);
                return 1;
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArrayList<PollSummarizer> getAllPolls() {
        try {
            PreparedStatement preparedStatement = pollsConnection.prepareStatement("SELECT * FROM polls");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<PollSummarizer> polls = new ArrayList<>();
            while(results.next()) {
                polls.add(new PollSummarizer(results.getInt(1), results.getString(2), results.getString(3), results.getString(4)));
            }
            return polls;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PollSummarizer addPoll(PollSummarizer poll) {
        try {
            PreparedStatement preparedStatement = pollsConnection.prepareStatement("INSERT INTO polls(channelid, messageid, endtime) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, poll.getChannelID());
            preparedStatement.setString(2, poll.getMessageID());
            preparedStatement.setString(3, poll.getEndTime());
            preparedStatement.execute();
            ResultSet id = preparedStatement.getGeneratedKeys();
            if(id.next()) {
                poll.setId(id.getInt(1));
                Cashew.pollManager.addPoll(poll);
                return poll;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deletePoll(int id) {
        try {
            PreparedStatement preparedStatement = pollsConnection.prepareStatement("DELETE FROM polls WHERE _id = ?");
            preparedStatement.setInt(1, id);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}