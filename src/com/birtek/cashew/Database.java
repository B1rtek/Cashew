package com.birtek.cashew;

import com.birtek.cashew.timings.TimedMessage;
import kotlin.Pair;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    protected static volatile Database instance;

    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String CHANNEL_ACTIVITY_DB = "jdbc:sqlite:channelActivity.db";
    public static final String BO_BURNHAM_DB = "jdbc:sqlite:boBurnhamQuotes.db";
    public static final String CASE_OPENING_DB = "jdbc:sqlite:caseOpening.db";
    public static final String COLLECTION_OPENING_DB = "jdbc:sqlite:collectionOpening.db";
    public static final String TIMED_MESSAGES_DB = "jdbc:sqlite:timedMessages.db";
    public static final String SOCIALCREDIT_DB = "jdbc:sqlite:socialCredit.db";

    private Connection channelActivityConnection;
    private Statement channelActivityStatement;
    private Connection boBurnhamConnection;
    private Statement boBurnhamStatement;
    private Connection caseOpeningConnection;
    private Statement caseOpeningStatement;
    private Connection collectionOpeningConnection;
    private Statement collectionOpeningStatement;
    private Connection timedMessagesConnection;
    private Statement timedMessagesStatement;
    private Connection socialCreditConnection;
    private Statement socialCreditStatement;

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
            boBurnhamStatement = boBurnhamConnection.createStatement();
            caseOpeningConnection = DriverManager.getConnection(CASE_OPENING_DB);
            caseOpeningStatement = caseOpeningConnection.createStatement();
            collectionOpeningConnection = DriverManager.getConnection(COLLECTION_OPENING_DB);
            collectionOpeningStatement = collectionOpeningConnection.createStatement();
            timedMessagesConnection = DriverManager.getConnection(TIMED_MESSAGES_DB);
            timedMessagesStatement = timedMessagesConnection.createStatement();
            socialCreditConnection = DriverManager.getConnection(SOCIALCREDIT_DB);
            socialCreditStatement = socialCreditConnection.createStatement();
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
            if(instance == null) {
                instance = new Database();
            }
            return instance;
        }
    }

    private boolean createTables() {
        String createTestTable = "CREATE TABLE IF NOT EXISTS ChannelActivity(_id INTEGER PRIMARY KEY AUTOINCREMENT, channelID TEXT, activity INTEGER)";
        try {
            channelActivityStatement.execute(createTestTable);

        } catch (SQLException e) {
            System.err.println("An error occurred while creating the tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean channelActivityInsert(String channelID, int activitySetting) {
        try {
            PreparedStatement prepStmt = channelActivityConnection.prepareStatement("INSERT INTO ChannelActivity(channelID, activity) VALUES(?, ?);");
            prepStmt.setString(1, channelID);
            prepStmt.setInt(2, activitySetting);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("An error occured while inserting the values");
            e.printStackTrace();
            return false;
        }
        return true;
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
            if(prepStmt.executeUpdate()!=1) {
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
        try {
            int size = 0;
            ResultSet checkIfRecordExists = instance.channelActivitySelect(channelID);
            if(checkIfRecordExists!=null) {
                while(checkIfRecordExists.next()) {
                    size++;
                }
            }
            if(size == 0) { //create new record
                instance.channelActivityInsert(channelID, activitySetting);
            } else { //apply new setting
                boolean result = instance.channelActivityUpdate(channelID, activitySetting);
                if(!result) {
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
            if(results!=null) {
                while(results.next()) {
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
            while(results.next()) {
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

    public List<Pair<String,String>> getCollectionItems(int collectionID) {
        try {
            List<Pair<String,String>> skins = new ArrayList<>();
            PreparedStatement prepStmt = collectionOpeningConnection.prepareStatement("SELECT DISTINCT itemName, rarity FROM Skins WHERE collectionID = ?");
            prepStmt.setInt(1, collectionID);
            ResultSet results = prepStmt.executeQuery();
            if(results!=null) {
                while(results.next()) {
                    skins.add(new Pair<>(results.getString("itemName"),results.getString("rarity")));
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
            if(results!=null) {
                while(results.next()) {
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
            if(selection.startsWith(">")) {
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
            if(selection.startsWith(">")) {
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
            while(preQuery.next()) {
                if(preQuery.getInt(1)==0) {
                    return -1;
                }
            }
            PreparedStatement prepStmtDelete;
            if(selection.startsWith(">")) {
                prepStmtDelete = timedMessagesConnection.prepareStatement("DELETE FROM Messages WHERE _id > ? AND serverID = ?");
                prepStmtDelete.setInt(1, 0);
            } else {
                prepStmtDelete = timedMessagesConnection.prepareStatement("DELETE FROM Messages WHERE _id = ? AND serverID = ?");
                prepStmtDelete.setInt(1, Integer.parseInt(selection.substring(1)));
            }
            prepStmtDelete.setString(2, serverID);
            if(prepStmtDelete.executeUpdate()==0) {
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
            int returnedInsertID=0;
            if(insertID.next()) {
                returnedInsertID = (int) insertID.getLong(1);
            }
            if(returnedInsertID!=0) {
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
            if(timedMessages!=null) {
                while(timedMessages.next()) {
                    timedMessagesArrayList.add(new TimedMessage(timedMessages.getString(1), timedMessages.getString(2), 86400*1000, timedMessages.getString(3)));
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
            if(socialCredit!=null) {
                if(socialCredit.next()) {
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
            if(socialCredit==648294745) {
                newSocialCredit(userID, serverID, credit);
            } else {
                PreparedStatement prepStmt = socialCreditConnection.prepareStatement("UPDATE SocialCredit SET credit = ? WHERE serverID = ? AND userID = ?");
                prepStmt.setInt(1, credit+socialCredit);
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
}