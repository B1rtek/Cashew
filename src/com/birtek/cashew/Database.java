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

    public ArrayList<String> getCasesimCapsules() {
        try {
            PreparedStatement preparedStatement = casesimCapsulesConnection.prepareStatement("SELECT name FROM Capsules");
            return createArrayListFromResultSet(preparedStatement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
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