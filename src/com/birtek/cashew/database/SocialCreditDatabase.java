package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class SocialCreditDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialCreditDatabase.class);

    private static volatile SocialCreditDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * socialcredit table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private SocialCreditDatabase() {
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
    public static SocialCreditDatabase getInstance() {
        SocialCreditDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (SocialCreditDatabase.class) {
            if (instance == null) {
                instance = new SocialCreditDatabase();
            }
            return instance;
        }
    }

    /**
     * Returns the amount of social credit of the user on the server
     *
     * @param userID   user ID of the user to check the social credit of
     * @param serverID server ID of the server from which the request is coming - social credit is stored separately for
     *                 every server
     * @return social credit score of the user or 0 if an error occurred
     */
    public long getSocialCredit(String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return 0;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT credit FROM socialcredit WHERE serverid = ? AND userid = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.getSocialCredit()");
            return 0;
        }
    }

    /**
     * Adds social credit to the current user's credit on the server
     *
     * @param userID   user ID of the user to add the social credit to
     * @param serverID server ID of the server from which the request is coming - social credit is stored separately for
     *                 every server
     * @param credit   amount of credit to add/subtract - if the number is negative, credit is subtracted
     * @return true if the operation was successful, false otherwise
     */
    public boolean addSocialCredit(String userID, String serverID, long credit) {
        if (isInDatabase(userID, serverID)) {
            return updateSocialCredit(userID, serverID, credit);
        } else {
            return insertSocialCredit(userID, serverID, credit);
        }
    }

    /**
     * Checks whether the user has an entry in the social credit database for the specific server
     *
     * @param userID   ID of the user to check if they have a record in the database
     * @param serverID ID of the server from which the request came
     * @return true if the user has its record in the database for this server, false otherwise.
     * In case of the query failing, false will be returned
     */
    private boolean isInDatabase(String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM socialcredit WHERE userid = ? AND serverid = ?");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.isInDatabase()");
            return false;
        }
    }

    /**
     * Updates the social credit record of the user on the server
     *
     * @param userID   ID of the user to check if they have a record in the database
     * @param serverID ID of the server from which the request came
     * @param credit   amount to add or subtract from the current score
     * @return true if the update was successful, false otherwise
     */
    private boolean updateSocialCredit(String userID, String serverID, long credit) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            long currentCredit = getSocialCredit(userID, serverID);
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE socialcredit SET credit = ? WHERE userid = ? AND serverid = ?");
            preparedStatement.setLong(1, currentCredit + credit);
            preparedStatement.setString(2, userID);
            preparedStatement.setString(3, serverID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.updateSocialCredit()");
            return false;
        }
    }

    /**
     * Inserts a new social credit record into the database
     *
     * @param userID   ID of the user to create the record for
     * @param serverID ID of the server from which the request came
     * @param credit   amount to set the credit to
     * @return true if the insertion was successful, false otherwise
     */
    private boolean insertSocialCredit(String userID, String serverID, long credit) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO socialcredit(userid, serverid, credit) VALUES(?, ?, ?)");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, serverID);
            preparedStatement.setLong(3, credit);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.insertSocialCredit()");
            return false;
        }
    }

    /**
     * Gets the selected 10 record age of the social credit leaderboard of the selected scoreboard (top or bottom) from
     * the server
     *
     * @param top      if set to true, will get the top leaderboard, else it will sort the records from the lowest to
     *                 highest
     * @param page     number of the page of the leaderboard to get
     * @param serverID ID of the server from which the request for the leaderboard came - each server has a separate
     *                 social credit system
     * @return an ArrayList of {@link LeaderboardRecord LeaderboardRecords} containing the place, user ID and score for
     * each record on the page or null if an error occurred
     */
    public ArrayList<LeaderboardRecord> getSocialCreditLeaderboardPage(boolean top, int page, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            String selectedSorting = top ? "desc" : "asc";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select pos, userid, credit from (select ROW_NUMBER() over (order by credit " + selectedSorting + ") pos, userid, credit from socialcredit where serverid = ? order by credit " + selectedSorting + ") as subqr where pos between (?-1)*10+1 and (?-1)*10+10");
            preparedStatement.setString(1, serverID);
            preparedStatement.setInt(2, page);
            preparedStatement.setInt(3, page);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<LeaderboardRecord> leaderboardRecords = new ArrayList<>();
            while (results.next()) {
                leaderboardRecords.add(new LeaderboardRecord(results.getInt(1), results.getString(2), results.getLong(3)));
            }
            return leaderboardRecords;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.getSocialCreditLeaderboardPage()");
            return null;
        }
    }

    /**
     * Gets the {@link LeaderboardRecord LeaderboardRecords} of the specified user from the server from the selected
     * leaderboard
     *
     * @param top      if set to true, will get the record from the top leaderboard, else it will get one from the worst to
     *                 best leaderboard
     * @param serverID ID of the server from which the leaderboard request came - each server has a separate social
     *                 credit system
     * @param userID   ID of the user who requested the leaderboard
     * @return {@link LeaderboardRecord LeaderboardRecord} containing user's score on the selected leaderboard, with
     * place set to 0 if the record doesn't exist in the database, or null if an error occurred
     */
    public LeaderboardRecord getSocialCreditLeaderboardUserStats(boolean top, String serverID, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            String selectedSorting = top ? "desc" : "asc";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select pos, userid, credit from (select ROW_NUMBER() over (order by credit " + selectedSorting + ") pos, userid, credit from socialcredit where serverid = ? order by credit " + selectedSorting + ") as subqr where userid = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new LeaderboardRecord(results.getInt(1), userID, results.getLong(3));
            }
            return new LeaderboardRecord(0, userID, 0);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.getSocialCreditLeaderboardUserStats()");
            return null;
        }
    }

    /**
     * Gets the amount of pages of the social credit leaderboard on the server, which just corresponds to the amount of
     * pages that would be occupied by all records in the server
     *
     * @param serverID ID of the server from which the leaderboard request came - each server has a separate social
     *                 credit system
     * @return amount of pages of the social credit leaderboard in the server, 0 if the leaderboard doesn't exist yet,
     * or -1 if an error occurred
     */
    public int getSocialCreditLeaderboardPageCount(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) from socialcredit where serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) / 10 + (results.getInt(1) % 10 == 0 ? 0 : 1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at SocialCreditDatabase.getSocialCreditLeaderboardPageCount()");
            return -1;
        }
    }
}
