package com.birtek.cashew.database;

import com.birtek.cashew.timings.PollSummarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class PollsDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollsDatabase.class);

    private static volatile PollsDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * reminders table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private PollsDatabase() {
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
    public static PollsDatabase getInstance() {
        PollsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PollsDatabase.class) {
            if (instance == null) {
                instance = new PollsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets all polls from the database
     *
     * @return ArrayList of {@link PollSummarizer PollSummarizers} containing all information about polls, or null if an
     * error occurred
     */
    public ArrayList<PollSummarizer> getAllPolls() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM polls");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<PollSummarizer> polls = new ArrayList<>();
            while (results.next()) {
                polls.add(new PollSummarizer(results.getInt(1), results.getString(2), results.getString(3), results.getString(4)));
            }
            return polls;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at PollsDatabase.getAll()");
            return null;
        }
    }

    /**
     * Adds a poll to the database, and then to the {@link com.birtek.cashew.timings.PollManager PollManager}.
     *
     * @param poll a {@link PollSummarizer PollSummarizer} object containing all information about poll like ending
     *             time, ID of the message with the poll etc. apart from the poll ID
     * @return a {@link PollSummarizer PollSummarizer} with an ID assigned by the database, or null if an error occurred
     */
    public PollSummarizer addPoll(PollSummarizer poll) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO polls(channelid, messageid, endtime) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, poll.getChannelID());
            preparedStatement.setString(2, poll.getMessageID());
            preparedStatement.setString(3, poll.getEndTime());
            if (preparedStatement.executeUpdate() != 1) return null;
            ResultSet id = preparedStatement.getGeneratedKeys();
            if (id.next()) {
                poll.setId(id.getInt(1));
                return poll;
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at PollsDatabase.addPoll()");
            return null;
        }
    }

    /**
     * Removes the poll from the database after its conclusion - Polls can't be removed by a command or a button, but
     * removing the message with the poll is good enough as the poll won't be able to conclude and will be removed after
     * finishing the conclusion process anyway
     *
     * @param id the ID of the poll to remove
     * @return true if the removal was successful, false otherwise
     */
    public boolean deletePoll(int id) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("DELETE FROM polls WHERE _id = ?");
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at PollsDatabase.deletePoll()");
            return false;
        }
    }
}
