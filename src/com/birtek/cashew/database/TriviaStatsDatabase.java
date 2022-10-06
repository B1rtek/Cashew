package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class TriviaStatsDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriviaStatsDatabase.class);

    private static volatile TriviaStatsDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * triviastats table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private TriviaStatsDatabase() {
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

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS triviastats(userid text, progress text, gamesplayed integer, gameswon integer)");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the triviastats table");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static TriviaStatsDatabase getInstance() {
        TriviaStatsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (TriviaStatsDatabase.class) {
            if (instance == null) {
                instance = new TriviaStatsDatabase();
            }
            return instance;
        }
    }

    /**
     * Checks whether a user has a record in the database
     *
     * @param userID ID of the user to check for
     * @return true if the user has a record in the triviastats database, false if it's not or if an error occurred
     */
    private boolean isInDatabase(String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM triviastats WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaStatsDatabasease.isInDatabase()");
            return false;
        }
    }

    /**
     * Creates a {@link TriviaStats TriviaStats} object from the given data
     * @param userID ID of the user to whom these stats belong
     * @param progress progress string describing which questions have been completed
     * @param gamesPlayed number od games played
     * @param gamesWon number of games won
     * @return a {@link TriviaStats TriviaStats} object describing the data
     */
    private TriviaStats createStatsFromData(String userID, String progress, int gamesPlayed, int gamesWon) {
        TriviaQuestionsDatabase database = TriviaQuestionsDatabase.getInstance();
        ArrayList<Integer> hardnessMap = database.getHardnessMap();
        int easy = 0, medium = 0, hard = 0;
        for (int i = 0; i < progress.length(); i++) {
            if (progress.charAt(i) == '1') {
                switch (hardnessMap.get(i)) {
                    case 1 -> easy++;
                    case 2 -> medium++;
                    case 3 -> hard++;
                }
            }
        }
        return new TriviaStats(userID, easy, medium, hard, gamesPlayed, gamesWon);
    }

    /**
     * Gets user's {@link TriviaStats TriviaStats}
     *
     * @param userID ID of the user to get stats of
     * @return {@link TriviaStats TriviaStats} of the user, or null if an error occurred
     */
    public TriviaStats getUserStats(String userID) {
        if (!isInDatabase(userID)) return new TriviaStats(userID, 0, 0, 0, 0, 0);
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM triviastats WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                String progress = results.getString(2);
                return createStatsFromData(userID, progress, results.getInt(3), results.getInt(4));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaStatsDatabase.getUserStats()");
            return null;
        }
    }

    /**
     * Updates user's {@link TriviaStats TriviaStats} according to the result of the game played
     *
     * @param userID   ID of the user whose stats will be updated
     * @param won      true if the games was won, false otherwise
     * @param question question from the game
     * @return 0 if the update was successful, 1 if completing this question resulted in completing all questions of
     * the difficulty, or -1 if an error occurred
     */
    public int updateUserStats(String userID, boolean won, TriviaQuestion question) {
        if (!isInDatabase(userID)) {
            String progress = "";
            progress = saveProgress(progress, won, question);
            if (insertStatsRecord(userID, won, progress)) return 0;
            else return -1;
        } else {
            TriviaStats oldUserStats = getUserStats(userID);
            if (oldUserStats == null) return -1;
            try {
                if (databaseConnection.isClosed()) {
                    if (!reestablishConnection()) return -1;
                }
                PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM triviastats WHERE userid = ?");
                preparedStatement.setString(1, userID);
                String progress;
                int gamesPlayed, gamesWon;
                ResultSet results = preparedStatement.executeQuery();
                if (results.next()) {
                    progress = saveProgress(results.getString(2), won, question);
                    gamesPlayed = oldUserStats.gamesPlayed() + 1;
                    gamesWon = oldUserStats.gamesWon() + (won ? 1 : 0);
                } else return -1;
                if (!updateStatsRecord(userID, progress, gamesPlayed, gamesWon)) return -1;
                if (!won) return 0;
                TriviaStats newUserStats = createStatsFromData(userID, progress, gamesPlayed, gamesWon);
                TriviaQuestionsDatabase database = TriviaQuestionsDatabase.getInstance();
                HashMap<Integer, Integer> distribution = database.getQuestionsCountByType();
                int oldCompletedQuestionsOfType = switch (question.difficulty()) {
                    case 1 -> oldUserStats.easy();
                    case 2 -> oldUserStats.medium();
                    case 3 -> oldUserStats.hard();
                    default -> 0;
                };
                int newCompletedQuestionsOfType = switch (question.difficulty()) {
                    case 1 -> newUserStats.easy();
                    case 2 -> newUserStats.medium();
                    case 3 -> newUserStats.hard();
                    default -> 0;
                };
                if (distribution.get(question.difficulty()) == newCompletedQuestionsOfType && newCompletedQuestionsOfType == oldCompletedQuestionsOfType + 1) return 1;
                else return 0;
            } catch (SQLException e) {
                LOGGER.warn(e + " thrown at TriviaStatsDatabase.updateUserStats()");
                return -1;
            }
        }
    }

    /**
     * Extends the progress String (1 and 0s describing which questions have already been answered correctly by a player)
     * to minimum length required by the newly answered question and saves the new progress
     *
     * @param progress progress String, as mentioned above
     * @param won      true if the recent game was won
     * @param question number of the recent question
     * @return progress String extended to accommodate the recent question
     */
    private String saveProgress(String progress, boolean won, TriviaQuestion question) {
        if (!won) return progress;
        int toExtend = question.id() - progress.length();
        if (toExtend > 0) {
            progress = progress + "0".repeat(toExtend);
        }
        StringBuilder pb = new StringBuilder(progress);
        pb.setCharAt(question.id() - 1, '1');
        return pb.toString();
    }

    /**
     * Inserts a new {@link TriviaStats TriviaStats} record into the database
     *
     * @param userID   ID of the user whose record will be created
     * @param won      true if the player won their first game
     * @param progress string telling which questions have been correctly answered by them
     * @return true if the insertion was successful, false otherwise
     */
    private boolean insertStatsRecord(String userID, boolean won, String progress) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO triviastats(userid, progress, gamesplayed, gameswon) VALUES(?, ?, ?, ?)");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, progress);
            preparedStatement.setInt(3, won ? 1 : 0);
            preparedStatement.setInt(4, 1);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaStatsDatabase.insertStatsRecord()");
            return false;
        }
    }

    /**
     * Updates user's record in the TriviaStats database
     *
     * @param userID      ID of the user whose record will be updated
     * @param progress    updated progress String from user's stats
     * @param gamesPlayed amount of games played by the user
     * @param gamesWon    amount of games won by the user
     * @return true if the update was successful, false otherwise
     */
    private boolean updateStatsRecord(String userID, String progress, int gamesPlayed, int gamesWon) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE triviastats SET progress = ?, gamesplayed = ?, gameswon = ? WHERE userid = ?");
            preparedStatement.setString(1, progress);
            preparedStatement.setInt(2, gamesPlayed);
            preparedStatement.setInt(3, gamesWon);
            preparedStatement.setString(4, userID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaStatsDatabase.updateStatsRecord()");
            return false;
        }
    }
}
