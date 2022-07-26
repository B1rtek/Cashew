package com.birtek.cashew.database;

import com.birtek.cashew.timings.ScheduledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class ScheduledMessagesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessagesDatabase.class);

    private static volatile ScheduledMessagesDatabase instance;

    private Connection scheduledMessagesConnection;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * scheduledmessages table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private ScheduledMessagesDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            scheduledMessagesConnection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static ScheduledMessagesDatabase getInstance() {
        ScheduledMessagesDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ScheduledMessagesDatabase.class) {
            if (instance == null) {
                instance = new ScheduledMessagesDatabase();
            }
            return instance;
        }
    }

    /**
     * Returns a list of ScheduledMessages based on the selection
     *
     * @param id       ID of the message to return - if the ID is equal to 0, all messages from the server will be returned
     * @param serverID ID of the server
     * @return ArrayList of ScheduledMessages matching the given criteria, since querying for all messages rather than
     * for a single one is more common
     */
    public ArrayList<ScheduledMessage> getScheduledMessages(int id, String serverID) {
        try {
            PreparedStatement preparedStatement;
            if (id != 0) {
                preparedStatement = scheduledMessagesConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ? AND _id = ?");
                preparedStatement.setInt(2, id);
            } else {
                preparedStatement = scheduledMessagesConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ?");
            }
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            return resultSetToArrayList(results);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.getScheduledMessages()");
            return null;
        }
    }

    /**
     * Adds a ScheduledMessage to the database by inserting it into the scheduledmessages table
     *
     * @param scheduledMessage ScheduledMessage to add
     * @param serverID         serverID of the server where the message was scheduled
     * @return the provided {@link ScheduledMessage ScheduledMessage} but with an ID assigned by the database, or null
     *
     */
    public ScheduledMessage addScheduledMessage(ScheduledMessage scheduledMessage, String serverID) {
        try {
            PreparedStatement preparedStatement = scheduledMessagesConnection.prepareStatement("INSERT INTO scheduledmessages(messagecontent, executiontime, repetitioninterval, destinationchannelid, serverid) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, scheduledMessage.getMessageContent());
            preparedStatement.setString(2, scheduledMessage.getExecutionTime());
            preparedStatement.setString(3, "86400");
            preparedStatement.setString(4, scheduledMessage.getDestinationChannelID());
            preparedStatement.setString(5, serverID);
            if(preparedStatement.executeUpdate() != 1) return null;
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int scheduledMessageID = generatedKeys.getInt(1);
                if (scheduledMessageID >= 0) {
                    scheduledMessage.setId(scheduledMessageID);
                    return scheduledMessage;
                }
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.addScheduledMessages()");
            return null;
        }
    }

    /**
     * Removes a ScheduledMessage from the database based on the ID provided and the serverID provided
     *
     * @param id       ID of the message to remove, 0 to remove all
     * @param serverID ID of the server from which the request came
     * @return true if the deletion was successful, false otherwise. Deletion will fail if the ID of the message and
     * the serverID of that message in the database don't match
     */
    public boolean removeScheduledMessage(int id, String serverID) {
        try {
            PreparedStatement preparedStatement;
            if (id != 0) { // check if the message exists if a single one is getting deleted
                preparedStatement = scheduledMessagesConnection.prepareStatement("SELECT COUNT(*) FROM scheduledmessages WHERE _id = ? AND serverid = ?");
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, serverID);
                ResultSet results = preparedStatement.executeQuery();
                if (results.next()) {
                    if (results.getInt(1) != 1) return false;
                } else {
                    return false;
                }
            }
            // remove the messages
            if (id != 0) {
                preparedStatement = scheduledMessagesConnection.prepareStatement("DELETE FROM scheduledmessages WHERE _id = ?");
                preparedStatement.setInt(1, id);
            } else {
                preparedStatement = scheduledMessagesConnection.prepareStatement("DELETE FROM scheduledmessages WHERE serverid = ?");
                preparedStatement.setString(1, serverID);
            }
            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.removeScheduledMessages()");
            return false;
        }
    }

    /**
     * Method used by the {@link com.birtek.cashew.timings.ScheduledMessagesManager ScheduledMessagesManager} to get and
     * schedule all messages. Separate from the {@link #getScheduledMessages(int, String)}  getScheduledMessages()} method to avoid
     * leaks if something fails
     *
     * @return ArrayList containing all ScheduledMessages from the database
     */
    public ArrayList<ScheduledMessage> getAllScheduledMessages() {
        try {
            PreparedStatement preparedStatement = scheduledMessagesConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages");
            ResultSet results = preparedStatement.executeQuery();
            return resultSetToArrayList(results);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.getScheduledMessages()");
            return null;
        }
    }

    /**
     * Creates an ArrayList of ScheduledMessages out of a ResultSet containing data for them
     *
     * @param results ResultSet containing all data needed to create a ScheduledMessage object, with columns in the same
     *                order as in the database
     * @return ArrayList of ScheduledMessages extracted from the results
     * @throws SQLException if the ResultSet was not properly formatted (bad columns order)
     */
    private ArrayList<ScheduledMessage> resultSetToArrayList(ResultSet results) throws SQLException {
        ArrayList<ScheduledMessage> scheduledMessages = new ArrayList<>();
        while (results.next()) {
            scheduledMessages.add(new ScheduledMessage(results.getInt(1), results.getString(2), results.getString(3), results.getString(4)));
        }
        return scheduledMessages;
    }
}
