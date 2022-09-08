package com.birtek.cashew.database;

import com.birtek.cashew.timings.ScheduledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class ScheduledMessagesDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessagesDatabase.class);

    private static volatile ScheduledMessagesDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * scheduledmessages table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private ScheduledMessagesDatabase() {
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists scheduledmessages ( _id bigserial constraint idx_16761_scheduledmessages_pkey primary key, messagecontent text, executiontime text, repetitioninterval text, destinationchannelid text, serverid text );");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the scheduledmessages table");
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
     * for a single one is more common, or null if an error occurred
     */
    public ArrayList<ScheduledMessage> getScheduledMessages(int id, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement;
            if (id != 0) {
                preparedStatement = databaseConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ? AND _id = ?");
                preparedStatement.setInt(2, id);
            } else {
                preparedStatement = databaseConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ?");
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
     * Gets a page of the {@link ScheduledMessage ScheduledMessages} from the specified server
     *
     * @param serverID   ID of the server from which the messages were requested
     * @param pageNumber number of the page requested, starting from 1
     * @return an ArrayList of {@link ScheduledMessage ScheduledMessages} representing the page, or null if an error
     * occurred
     */
    public ArrayList<ScheduledMessage> getScheduledMessagesPage(String serverID, int pageNumber) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM (SELECT ROW_NUMBER() OVER(ORDER BY _id) index, _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ?) AS subqr where index between (?-1)*10+1 and (?-1)*10+10");
            preparedStatement.setString(1, serverID);
            preparedStatement.setInt(2, pageNumber);
            preparedStatement.setInt(3, pageNumber);
            ResultSet results = preparedStatement.executeQuery();
            return resultSetToArrayList(results);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.getScheduledMessagesPage()");
            return null;
        }
    }

    /**
     * Gets the number of pages that the whole scheduled messages list consists of, each page consists of 10 messages
     *
     * @param serverID ID of the server which page count will be obtained
     * @return the number of pages of the list, or -1 if an error occurred
     */
    public int getScheduledMessagesPageCount(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM scheduledmessages WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) / 10 + (results.getInt(1) % 10 == 0 ? 0 : 1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.getScheduledMessagesPageCount()");
            return -1;
        }
    }

    /**
     * Gets the scheduled message with the provided index from the server's scheduled messages list
     *
     * @param serverID ID of the server from which list the message will be taken
     * @param index    index of the message, starting from zero
     * @return {@link ScheduledMessage ScheduledMessage} with the provided index, or null if it doesn't exist or if an
     * error occurred
     */
    public ScheduledMessage getScheduledMessageByIndex(String serverID, int index) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM (SELECT ROW_NUMBER() OVER(ORDER BY _id) index, _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages WHERE serverid = ?) AS subqr where index = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setInt(2, index + 1);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new ScheduledMessage(results.getInt(1), results.getString(2), results.getString(3), results.getString(4));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ScheduledMessagesDatabase.getScheduledMessagesPage()");
            return null;
        }
    }

    /**
     * Adds a ScheduledMessage to the database by inserting it into the scheduledmessages table
     *
     * @param scheduledMessage ScheduledMessage to add
     * @param serverID         serverID of the server where the message was scheduled
     * @return the provided {@link ScheduledMessage ScheduledMessage} but with an ID assigned by the database, or null
     */
    public ScheduledMessage addScheduledMessage(ScheduledMessage scheduledMessage, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO scheduledmessages(messagecontent, executiontime, repetitioninterval, destinationchannelid, serverid) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, scheduledMessage.getMessageContent());
            preparedStatement.setString(2, scheduledMessage.getExecutionTime());
            preparedStatement.setString(3, "86400");
            preparedStatement.setString(4, scheduledMessage.getDestinationChannelID());
            preparedStatement.setString(5, serverID);
            if (preparedStatement.executeUpdate() != 1) return null;
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
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement;
            if (id != 0) { // check if the message exists if a single one is getting deleted
                preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM scheduledmessages WHERE _id = ? AND serverid = ?");
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
                preparedStatement = databaseConnection.prepareStatement("DELETE FROM scheduledmessages WHERE _id = ?");
                preparedStatement.setInt(1, id);
            } else {
                preparedStatement = databaseConnection.prepareStatement("DELETE FROM scheduledmessages WHERE serverid = ?");
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
     * @return ArrayList containing all ScheduledMessages from the database, or null if an error occurred
     */
    public ArrayList<ScheduledMessage> getAllScheduledMessages() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, messagecontent, executiontime, destinationchannelid FROM scheduledmessages");
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
