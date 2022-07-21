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