package com.birtek.cashew.database;

import com.birtek.cashew.commands.Gifts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class GiftHistoryDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftHistoryDatabase.class);

    private static volatile GiftHistoryDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * gifthistory table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private GiftHistoryDatabase() {
        databaseURL = System.getenv("JDBC_DATABASE_URL");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            databaseConnection = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static GiftHistoryDatabase getInstance() {
        GiftHistoryDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (GiftHistoryDatabase.class) {
            if (instance == null) {
                instance = new GiftHistoryDatabase();
            }
            return instance;
        }
    }

    /**
     * Retrieves GiftStats of a user on a server from the database
     *
     * @param giftID   ID of the gift of which the stats are being obtained, 0 to get total stats
     * @param userID   ID of the user whose stats are being obtained
     * @param serverID ID of the server from which the request came
     * @return GiftStats object with information about amount gifted, received and last time the user gifted the gift,
     * or null if an error occurred
     */
    public GiftStats getGiftStats(int giftID, String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = databaseConnection.prepareStatement("SELECT amountgifted, amountreceived, lastgifted FROM gifthistory WHERE ((serverid = ? AND userid = ?) AND giftid = ?)");
                preparedStatement.setInt(3, giftID);
            } else {
                preparedStatement = databaseConnection.prepareStatement("SELECT SUM(amountgifted), SUM(amountreceived), MAX(lastgifted) FROM gifthistory WHERE (serverid = ? AND userid = ?)");
            }
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new GiftStats(results.getInt(1), results.getInt(2), results.getLong(3));
            } else {
                return new GiftStats(0, 0, 0);
            }
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.getGiftStats()");
            return null;
        }
    }

    /**
     * Updates gift stats in the database
     *
     * @param newStats GiftStats containing new stats
     * @param giftID   ID of the gift of which the stats are being updated
     * @param userID   ID of the user to whom these stats belong to
     * @param serverID ID of the server from which these stats came
     * @return true if the update was successful, false if an error occurred
     */
    public boolean updateGiftStats(GiftStats newStats, int giftID, String userID, String serverID) {
        if (isInDatabase(giftID, userID, serverID)) {
            return updateStats(newStats, giftID, userID, serverID);
        } else {
            return insertStats(newStats, giftID, userID, serverID);
        }
    }

    /**
     * Checks whether the user has an entry for the specified gift in a server
     *
     * @param giftID   ID of the gift to check for
     * @param userID   ID of the user to check
     * @param serverID ID of the server from which the request came
     * @return true if the record is in database, or false if it's not or an error occurred
     */
    private boolean isInDatabase(int giftID, String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM gifthistory WHERE giftid = ? AND userid = ? AND serverid = ?");
            preparedStatement.setInt(1, giftID);
            preparedStatement.setString(2, userID);
            preparedStatement.setString(3, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates the record in the database with the provided GiftStats for the specified user and gift in a server
     *
     * @param newStats GiftStats to which the record in the database will be updated
     * @param giftID   ID of the gift to update the record of
     * @param userID   ID of the user to update the record of
     * @param serverID ID of the server to update the record of
     * @return true if the update was successful, false otherwise
     */
    private boolean updateStats(GiftStats newStats, int giftID, String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE gifthistory SET amountgifted = ?, amountreceived = ?, lastgifted = ? WHERE giftid = ? AND userid = ? AND serverid = ?");
            preparedStatement.setInt(1, newStats.amountGifted());
            preparedStatement.setInt(2, newStats.amountReceived());
            preparedStatement.setLong(3, newStats.lastGifted());
            preparedStatement.setInt(4, giftID);
            preparedStatement.setString(5, userID);
            preparedStatement.setString(6, serverID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.updateStats()");
            return false;
        }
    }

    /**
     * Inserts a new record into the database with the provided GiftStats for the specified user and gift in a server
     *
     * @param newStats GiftStats that will be inserted into the database
     * @param giftID   ID of the gift to insert the record of
     * @param userID   ID of the user to insert the record of
     * @param serverID ID of the server to insert the record of
     * @return true if the insertion was successful, false otherwise
     */
    private boolean insertStats(GiftStats newStats, int giftID, String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO gifthistory(serverid, userid, giftid, amountgifted, amountreceived, lastgifted) VALUES(?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            preparedStatement.setInt(3, giftID);
            preparedStatement.setInt(4, newStats.amountGifted());
            preparedStatement.setInt(5, newStats.amountReceived());
            preparedStatement.setLong(6, newStats.lastGifted());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.insertStats()");
            return false;
        }
    }

    /**
     * Gets the selected 10 record page of a gift leaderboard of the provided type and of the selected gift
     *
     * @param type     {@link Gifts.GiftsLeaderboardType GiftsLeaderboardType} type of the leaderboard - should it show
     *                 amount of gifts received or gifted
     * @param page     number of the page requested
     * @param serverID ID of the server from which the leaderboard request came - each server has a separate leaderboard
     * @param giftID   ID of the gift of which the leaderboard is requested, 0 if it's a request for a total leaderboard
     * @return an ArrayList of {@link LeaderboardRecord LeaderboardRecords} containing the place, user ID and score for
     * each record on the page or null if an error occurred
     */

    public ArrayList<LeaderboardRecord> getGiftsLeaderboardPage(Gifts.GiftsLeaderboardType type, int page, String serverID, int giftID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            String selectedColumn = type == Gifts.GiftsLeaderboardType.MOST_GIFTED ? "amountgifted" : "amountreceived";
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = databaseConnection.prepareStatement("select pos, userid, " + selectedColumn + " from (select ROW_NUMBER() over (order by " + selectedColumn + " desc) pos, userid, " + selectedColumn + " from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0 order by " + selectedColumn + " desc) as subqr where pos between (?-1)*10+1 and (?-1)*10+10");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, giftID);
                preparedStatement.setInt(3, page);
                preparedStatement.setInt(4, page);
            } else {
                preparedStatement = databaseConnection.prepareStatement("select pos, userid, total from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where pos between (?-1)*10+1 and (?-1)*10+10 and total > 0");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, page);
                preparedStatement.setInt(3, page);
            }
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<LeaderboardRecord> leaderboardRecords = new ArrayList<>();
            while (results.next()) {
                leaderboardRecords.add(new LeaderboardRecord(results.getInt(1), results.getString(2), results.getInt(3)));
            }
            return leaderboardRecords;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.getGiftsLeaderboardPage()");
            return null;
        }
    }

    /**
     * Gets the {@link LeaderboardRecord LeaderboardRecords} or the specified user from the server from the selected
     * leaderboard
     *
     * @param type     {@link Gifts.GiftsLeaderboardType GiftsLeaderboardType} type of the leaderboard - should it be
     *                 the amount of gifts received or gifted leaderboard
     * @param serverID ID of the server from which the leaderboard request came - each server has a separate leaderboard
     * @param giftID   ID of the gift of which the LeaderboardRecord is requested, 0 if it's a request for a total
     *                 leaderboard record
     * @param userID   ID of the user whose record will be returned
     * @return {@link LeaderboardRecord LeaderboardRecord} containing the place, user ID and score for the user from the
     * specified leaderboard, with place set to 0 if the record doesn't exist in the database, or null if an error
     * occurred
     */
    public LeaderboardRecord getGiftsLeaderboardUserStats(Gifts.GiftsLeaderboardType type, String serverID, int giftID, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            String selectedColumn = type == Gifts.GiftsLeaderboardType.MOST_GIFTED ? "amountgifted" : "amountreceived";
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = databaseConnection.prepareStatement("select pos, " + selectedColumn + " from (select ROW_NUMBER() over (order by " + selectedColumn + " desc) pos, userid, " + selectedColumn + " from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0 order by " + selectedColumn + " desc) as subqr where userid = ?");
                preparedStatement.setString(1, serverID);
                preparedStatement.setInt(2, giftID);
                preparedStatement.setString(3, userID);
            } else {
                preparedStatement = databaseConnection.prepareStatement("select pos, total from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where userid = ? AND total > 0");
                preparedStatement.setString(1, serverID);
                preparedStatement.setString(2, userID);
            }
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new LeaderboardRecord(results.getInt(1), userID, results.getInt(2));
            }
            return new LeaderboardRecord(0, userID, 0);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.getGiftsLeaderboardUserStats()");
            return null;
        }
    }

    /**
     * Gets the amount of pages of the specified leaderboard by counting the amount of records in it and calculating the
     * amount of pages they occupy
     *
     * @param type     {@link Gifts.GiftsLeaderboardType GiftsLeaderboardType} type of the leaderboard - should it be
     *                 the amount of gifts received or gifted leaderboard
     * @param serverID ID of the server from which the request for the leaderboard came - each server has separate
     *                 leaderboards
     * @param giftID   ID of the gift of which the leaderboard size is requested, 0 if the request is for the total
     *                 leaderboard
     * @return amount of pages of the specified leaderboard, 0 if the leaderboard doesn't yet exist (there are no
     * records to include in it), or -1 if an error occurred
     */
    public int getGiftsLeaderboardPageCount(Gifts.GiftsLeaderboardType type, String serverID, int giftID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            String selectedColumn = type == Gifts.GiftsLeaderboardType.MOST_GIFTED ? "amountgifted" : "amountreceived";
            PreparedStatement preparedStatement;
            if (giftID != 0) {
                preparedStatement = databaseConnection.prepareStatement("select COUNT(*) from gifthistory where serverid = ? AND giftid = ? AND " + selectedColumn + " > 0");
                preparedStatement.setInt(2, giftID);
            } else {
                preparedStatement = databaseConnection.prepareStatement("select COUNT(*) from (select ROW_NUMBER() over (order by SUM(" + selectedColumn + ") desc) pos, userid, SUM(" + selectedColumn + ") as total from gifthistory where serverid = ? group by userid order by total desc) as subqr where total > 0");
            }
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) / 10 + (results.getInt(1) % 10 == 0 ? 0 : 1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftHistoryDatabase.getGiftsLeaderboardPageCount()");
            return -1;
        }
    }
}
