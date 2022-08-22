package com.birtek.cashew.database;

import com.birtek.cashew.timings.ReminderRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class RemindersDatabase extends Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemindersDatabase.class);

    private static volatile RemindersDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * reminders table
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private RemindersDatabase() {
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
    public static RemindersDatabase getInstance() {
        RemindersDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (RemindersDatabase.class) {
            if (instance == null) {
                instance = new RemindersDatabase();
            }
            return instance;
        }
    }

    /**
     * Adds a reminder to the database and gives it the ID assigned by the database
     *
     * @param reminder {@link ReminderRunnable ReminderRunnable} object containing all information about the reminder
     *                 apart from a correct ID
     * @return a {@link ReminderRunnable ReminderRunnable} with an ID assigned by the database, or null if an error
     * occurred
     */
    public ReminderRunnable addReminder(ReminderRunnable reminder) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO reminders(content, timedate, userid, ping) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reminder.getContent());
            preparedStatement.setString(2, reminder.getDateTime());
            preparedStatement.setString(3, reminder.getUserID());
            preparedStatement.setInt(4, reminder.isPing() ? 1 : 0);
            if (preparedStatement.executeUpdate() != 1) return null;
            ResultSet id = preparedStatement.getGeneratedKeys();
            if (id.next()) {
                reminder.setId(id.getInt(1));
                return reminder;
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.addReminder()");
            return null;
        }
    }

    /**
     * Gets a list of all reminders set by the user
     *
     * @param userID ID of the user whose reminders will be retrieved
     * @return an ArrayList of {@link ReminderRunnable ReminderRunnables} for every reminder set by the user, or null if
     * an error occurred
     */
    public ArrayList<ReminderRunnable> getUserReminders(String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, content, timedate, ping FROM reminders where userid = ? order by _id");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while (results.next()) {
                reminders.add(new ReminderRunnable(results.getInt(1), results.getBoolean(4), results.getString(2), results.getString(3), userID));
            }
            return reminders;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.getUserReminders()");
            return null;
        }
    }

    /**
     * Gets a reminder from user's list of reminders by the index
     *
     * @param userID ID of the user whose reminder will be retrieved
     * @param index  index of the reminder, starting from 0, which will be chosen. Reminders are sorted by the date of
     *               their creation, aka _id assigned by the database
     * @return a {@link ReminderRunnable ReminderRunnable} representing the requested reminder, or null if an error
     * occurred or if the provided index doesn't point to any reminder
     */
    public ReminderRunnable getUsersReminderByIndex(String userID, int index) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, content, timedate, ping FROM (SELECT ROW_NUMBER() OVER (ORDER BY _id) index, _id, content, timedate, ping FROM reminders where userid = ? order by _id) as subqr where index = ?");
            preparedStatement.setString(1, userID);
            preparedStatement.setInt(2, index + 1);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new ReminderRunnable(results.getInt(1), results.getBoolean(4), results.getString(2), results.getString(3), userID);
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.getUsersReminderByIndex()");
            return null;
        }
    }

    /**
     * Gets a list of all reminders set by all users
     *
     * @return an ArrayList of all {@link ReminderRunnable ReminderRunnables} in the database, or null if an error
     * occurred
     */
    public ArrayList<ReminderRunnable> getAllReminders() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id, content, timedate, ping, userid FROM reminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ReminderRunnable> reminders = new ArrayList<>();
            while (results.next()) {
                reminders.add(new ReminderRunnable(results.getInt(1), results.getBoolean(4), results.getString(2), results.getString(3), results.getString(5)));
            }
            return reminders;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.getAllReminders()");
            return null;
        }
    }

    /**
     * Gets the amount of reminders set by the user
     *
     * @param userID ID of the user whose amount of reminders will be checked
     * @return amount of the reminders or -1 if an error occurred
     */
    public int getRemindersCount(String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM reminders WHERE userid = ?");
            preparedStatement.setString(1, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.getRemindersCount()");
            return -1;
        }
    }

    /**
     * Removes a reminder from the database after checking if the reminder belongs to the person who's trying to remove
     * it
     *
     * @param id     ID of the reminder to remove
     * @param userID ID of the user requesting removal
     * @return 1 if the deletion was successful, 0 if the user tried to remove a reminder that isn't theirs or that
     * doesn't exist, or -1 if an error occurred
     */
    public int deleteReminder(int id, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            if (!isBelongingTo(id, userID)) return 0;
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("DELETE FROM reminders WHERE _id = ? AND userid = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, userID);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted == 1 ? 1 : 0;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.deleteReminder()");
            return -1;
        }
    }

    /**
     * Checks whether the reminder with the provided ID was set by the specified user
     *
     * @param id     id of the reminder to check the ownership of
     * @param userID ID of the user that is supposed to be the owner of the reminder
     * @return true if the reminder with the provided ID was set by that user, false otherwise or if an error occurs
     */
    private boolean isBelongingTo(int id, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM reminders where _id = ? AND userid = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, userID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt(1) == 1;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.isBelongingTo()");
            return false;
        }
    }

    /**
     * Deletes all user's reminders from the database
     *
     * @param userID ID of the user whose reminders should be removed
     * @return a list of all user's reminders' IDs, or null if an error occurred or the user had no reminders
     */
    public ArrayList<Integer> deleteUsersReminders(String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            ArrayList<ReminderRunnable> reminders = getUserReminders(userID);
            if (reminders == null || reminders.isEmpty()) return null;
            ArrayList<Integer> remindersIDs = new ArrayList<>();
            for (ReminderRunnable reminder : reminders) {
                remindersIDs.add(reminder.getId());
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("DELETE FROM reminders WHERE userid = ?");
            preparedStatement.setString(1, userID);
            if (preparedStatement.executeUpdate() == 0) return null;
            else return remindersIDs;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at RemindersDatabase.deleteUsersReminders()");
            return null;
        }
    }
}
