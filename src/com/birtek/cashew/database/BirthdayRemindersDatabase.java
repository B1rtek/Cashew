package com.birtek.cashew.database;

import com.birtek.cashew.timings.BirthdayReminder;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class BirthdayRemindersDatabase extends TransferrableDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayRemindersDatabase.class);

    private static volatile BirthdayRemindersDatabase instance;

    /**
     * Initializes the connection to the Postgres database, specifically to the
     * birthdayreminders and defaultbirthdayreminderchannels tables
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private BirthdayRemindersDatabase() {
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists birthdayreminders ( _id bigserial constraint idx_16657_birthdayreminders_pkey primary key, message text, dateandtime text, channelid text, serverid text, userid text );");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the birthdayreminders table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists defaultbirthdayreminderchannels ( _id bigserial constraint idx_16664_defaultbirthdayreminderchannels_pkey primary key, serverid text, channelid text, override bigint );");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create the defaultbirthdayreminderchannels table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create or replace function create_constraint_if_not_exists (t_name text, c_name text, constraint_sql text) returns void AS $$ begin if not exists (select constraint_name from information_schema.constraint_column_usage where table_name = t_name  and constraint_name = c_name) then execute constraint_sql; end if; end; $$ language 'plpgsql';");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create/replace the plsql function create_constraint_if_not_exists!");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select create_constraint_if_not_exists('birthdayreminders', 'uc_reminder', 'alter table birthdayreminders add constraint uc_reminder unique (serverid,userid);')");
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error("Failed to create constraint birthdayreminders.uc_reminder!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private TransferResult checkDefaultsStateForTransfer(String serverID) {
        int serverDefaultsID = getDefaultsID(serverID);
        if(serverDefaultsID < 1) {
            return switch (serverDefaultsID) {
                case 0 -> TransferResult.NO_DEFAULTS_SPECIFIED;
                case -1 -> TransferResult.DATABASE_ERROR;
                default -> TransferResult.UNKNOWN_ERROR;
            };
        }
        return TransferResult.SUCCESS;
    }

    @Override
    public TransferResult importDataFromServer(String serverID, String destinationServerID, String userID) {
        TransferResult defaultsCheckResult = checkDefaultsStateForTransfer(destinationServerID);
        if(defaultsCheckResult != TransferResult.SUCCESS) return defaultsCheckResult;
        try {
            PreparedStatement importStatement;
            if(userID == null) {
                importStatement = databaseConnection.prepareStatement("insert into birthdayreminders(message, dateandtime, channelid, serverid, userid) select message, dateandtime, channelid, ?, userid from birthdayreminders where serverid = ?");
            } else {
                importStatement = databaseConnection.prepareStatement("insert into birthdayreminders(message, dateandtime, channelid, serverid, userid) select message, dateandtime, channelid, ?, userid from birthdayreminders where serverid = ? and userid = ?");
                importStatement.setString(3, userID);
            }
            importStatement.setString(1, destinationServerID);
            importStatement.setString(2, serverID);

            importStatement.execute();
        } catch (SQLException e) {
            if(e.getErrorCode() == 23505) {// unique constraint violation
                return TransferResult.CONFLICT;
            } else {
                e.printStackTrace();
                return TransferResult.DATABASE_ERROR;
            }
        }
        return TransferResult.SUCCESS;
    }

    private int getAmountToImport(String serverID) {
        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select count(*) from birthdayreminders where serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return results.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getAmountOfConflicts(String serverID, String destinationServerID) {
        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select count(*) from (select userid, count(*) from birthdayreminders where serverid = ? or serverid = ? group by userid having count(*) = 2) subqr");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, destinationServerID);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return results.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Pair<Integer, Integer> getInformationAboutDuplicates(String serverID, String destinationServerID) {
        int amountToImport = getAmountToImport(serverID);
        int amountOfConflicts = getAmountOfConflicts(serverID, destinationServerID);
        return Pair.of(amountToImport, amountOfConflicts);
    }

    @Override
    public TransferResult importDataFromUser(String userID, String targetUserID, String serverID) {
        TransferResult defaultsCheckResult = checkDefaultsStateForTransfer(serverID);
        if(defaultsCheckResult != TransferResult.SUCCESS) return defaultsCheckResult;
        try {
            PreparedStatement importStatement = databaseConnection.prepareStatement("insert into birthdayreminders(message, dateandtime, channelid, serverid, userid) select message, dateandtime, channelid, ?, ? from birthdayreminders where serverid = ? and userid = ?");
            importStatement.setString(1, serverID);
            importStatement.setString(2, targetUserID);
            importStatement.setString(3, serverID);
            importStatement.setString(4, userID);
            importStatement.execute();
        } catch (SQLException e) {
            if(e.getErrorCode() == 23505) {// unique constraint violation
                return TransferResult.CONFLICT;
            } else {
                e.printStackTrace();
                return TransferResult.DATABASE_ERROR;
            }
        }
        return TransferResult.SUCCESS;
    }

    @Override
    public TransferResult deleteDataFromUser(String userID, String serverID) {
        try {
            PreparedStatement deleteStatement = databaseConnection.prepareStatement("delete from birthdayreminders where userid = ? and serverid = ?");
            deleteStatement.setString(1, userID);
            deleteStatement.setString(2, serverID);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            return TransferResult.DATABASE_ERROR;
        }
        return TransferResult.SUCCESS;
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static BirthdayRemindersDatabase getInstance() {
        BirthdayRemindersDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (BirthdayRemindersDatabase.class) {
            if (instance == null) {
                instance = new BirthdayRemindersDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a list of all {@link BirthdayReminder BirthdayReminders} from the database
     *
     * @return an ArrayList of {@link BirthdayReminder BirthdayReminders} for all reminders in the database, or null if
     * an error occurred
     */
    public ArrayList<BirthdayReminder> getAllReminders() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM birthdayreminders");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminder> reminders = new ArrayList<>();
            while (results.next()) {
                reminders.add(new BirthdayReminder(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6)));
            }
            return reminders;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getAllReminders()");
            return null;
        }
    }

    /**
     * Gets a list of all {@link BirthdayReminderDefaults BirthdayReminderDefaults} from the database
     *
     * @return an ArrayList of {@link BirthdayReminderDefaults BirthdayReminderDefaults} that are present in the
     * database, not every server has to have one, or null if an error occurred
     */
    public ArrayList<BirthdayReminderDefaults> getAllDefaults() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT serverid, channelid, override FROM defaultbirthdayreminderchannels");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminderDefaults> defaults = new ArrayList<>();
            while (results.next()) {
                defaults.add(new BirthdayReminderDefaults(results.getString(1), results.getString(2), results.getBoolean(3)));
            }
            return defaults;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getAllDefaults()");
            return null;
        }
    }

    /**
     * Puts a new {@link BirthdayReminder BirthdayReminder} in the database or updates an existing one if the user who
     * requested a new one already had one - one user can only have one reminder per server.
     *
     * @param reminder {@link BirthdayReminder BirthdayReminder} object containing all information about the reminder
     * @return {@link BirthdayReminder BirthdayReminder} with corrected ID if setting was successful, or null if an
     * error occurred
     */
    public BirthdayReminder setBirthdayReminder(BirthdayReminder reminder) {
        int id = getBirthdayReminderID(reminder.getServerID(), reminder.getUserID());
        switch (id) {
            case -1:
                return null;
            case 0:  // new record
                return insertBirthdayReminder(reminder);
            default:  // record update
                reminder.setId(id);
                if (this.updateBirthdayReminder(reminder)) {
                    return reminder;
                }
                return null;
        }
    }

    /**
     * Retrieves the ID of the BirthdayReminder from the database based on the provided user and server IDs, can be used
     * to check whether a reminder already exists in the database
     *
     * @param serverID ID of the server from the BirthdayReminder
     * @param userID   ID of the user from the BirthdayReminder
     * @return ID of the reminder from the database (the _id column), 0 if it wasn't found, or -1 if an error occurred
     */
    private int getBirthdayReminderID(String serverID, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id FROM birthdayreminders WHERE serverid = ? AND userid = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            ResultSet results = preparedStatement.executeQuery();
            int id = 0;
            if (results.next()) {
                id = results.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getBirthdayReminderID()");
            return -1;
        }
    }

    /**
     * Inserts a BirthdayReminder into the database and returns that reminder with the ID given to id by the database
     *
     * @param reminder {@link BirthdayReminder BirthdayReminder} with all information apart from a correct ID in it
     * @return a {@link BirthdayReminder BirthdayReminder} with assigned ID if the insertion was successful, or null if
     * an error occurred
     */
    private BirthdayReminder insertBirthdayReminder(BirthdayReminder reminder) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO birthdayreminders(message, dateandtime, channelid, serverid, userid) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reminder.getMessage());
            preparedStatement.setString(2, reminder.getDateAndTime());
            preparedStatement.setString(3, reminder.getChannelID());
            preparedStatement.setString(4, reminder.getServerID());
            preparedStatement.setString(5, reminder.getUserID());
            if (preparedStatement.executeUpdate() != 1) return null;
            ResultSet id = preparedStatement.getGeneratedKeys();
            if (id.next()) {
                reminder.setId(id.getInt(1));
                return reminder;
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.insertBirthdayReminder()");
            return null;
        }
    }

    /**
     * Updates a BirthdayReminder that already is in the database
     *
     * @param reminder {@link BirthdayReminder BirthdayReminder} with updated information in it
     * @return true if the update was successful, false if an error occurred
     */
    private boolean updateBirthdayReminder(BirthdayReminder reminder) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE birthdayreminders SET message = ?, dateandtime = ?, channelid = ?, serverid = ?, userid = ? WHERE _id = ?");
            preparedStatement.setString(1, reminder.getMessage());
            preparedStatement.setString(2, reminder.getDateAndTime());
            preparedStatement.setString(3, reminder.getChannelID());
            preparedStatement.setString(4, reminder.getServerID());
            preparedStatement.setString(5, reminder.getUserID());
            preparedStatement.setInt(6, reminder.getId());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.updateBirthdayReminder()");
            return false;
        }
    }

    /**
     * Removes a {@link BirthdayReminder BirthdayReminder} from the database.
     *
     * @param serverID ID of the server from which the request came
     * @param userID   ID of the user who is deleting their {@link BirthdayReminder BirthdayReminder} - because every user
     *                 can only have one reminder per server, these two arguments identify a record
     * @return the ID of the deleted reminder, or -1 if an error occurred
     */
    public int deleteBirthdayReminder(String serverID, String userID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            int id = getBirthdayReminderID(serverID, userID);
            if (id <= 0) return -1;
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("DELETE FROM birthdayreminders WHERE serverid = ? AND userid = ?");
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, userID);
            if (preparedStatement.executeUpdate() != 1) return -1;
            return id;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.deleteBirthdayReminder()");
            return -1;
        }
    }

    /**
     * Puts new {@link BirthdayReminderDefaults BirthdayReminderDefaults} record in the database or updates an existing
     * one if the server from which the request came already has set up its defaults
     *
     * @param defaults {@link BirthdayReminderDefaults BirthdayReminderDefaults} object containing server settings for
     *                 {@link BirthdayReminder BirthdayReminders'} default channel on the server and whether it should
     *                 override users' settings
     * @return true if the update was successful, false otherwise
     */
    public boolean setBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        int id = getDefaultsID(defaults.serverID());
        return switch (id) {
            case -1 -> false;
            case 0 ->  // new record
                    insertBirthdayRemindersDefaults(defaults);
            default -> // update
                    updateBirthdayRemindersDefaults(defaults);
        };
    }

    /**
     * Gets an ID of the {@link BirthdayReminderDefaults BirthdayReminderDefaults} for the provided server
     *
     * @param serverID ID of the server which {@link BirthdayReminderDefaults BirthdayReminderDefaults}' ID will be
     *                 found
     * @return the _id column value for this server's recordin the database if it exists, 0 if it doesn't, and -1 if an
     * error occurred
     */
    private int getDefaultsID(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT _id FROM defaultbirthdayreminderchannels WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            int id = 0;
            if (results.next()) {
                id = results.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getDefaultsID()");
            return -1;
        }
    }

    /**
     * Inserts new {@link BirthdayReminderDefaults BirthdayReminderDefaults} into the database
     *
     * @param defaults {@link BirthdayReminderDefaults BirthdayReminderDefaults} object with a server's Birthday
     *                 Reminders system settings to save
     * @return true if the insertion was successful, false if an error occurred
     */
    private boolean insertBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO defaultbirthdayreminderchannels(serverid, channelid, override) VALUES(?, ?, ?)");
            preparedStatement.setString(1, defaults.serverID());
            preparedStatement.setString(2, defaults.channelID());
            preparedStatement.setInt(3, defaults.override() ? 1 : 0);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.insertBirthdayRemindersDefaults()");
            return false;
        }
    }

    /**
     * Updates {@link BirthdayReminderDefaults BirthdayReminderDefaults} in the database
     *
     * @param defaults {@link BirthdayReminderDefaults BirthdayReminderDefaults} object with a server's Birthday
     *                 Reminders system settings to update
     * @return true if the update was successful, false if an error occurred
     */
    private boolean updateBirthdayRemindersDefaults(BirthdayReminderDefaults defaults) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE defaultbirthdayreminderchannels SET channelid = ?, override = ? WHERE serverid = ?");
            preparedStatement.setString(1, defaults.channelID());
            preparedStatement.setInt(2, defaults.override() ? 1 : 0);
            preparedStatement.setString(3, defaults.serverID());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.updateBirthdayRemindersDefaults()");
            return false;
        }
    }

    /**
     * Gets a single {@link BirthdayReminder BirthdayReminder} from the database
     *
     * @param userID   ID of the user requesting the {@link BirthdayReminder BirthdayReminder}
     * @param serverID ID of the server from which the request came
     * @return a requested {@link BirthdayReminder BirthdayReminder} with ID set to 0 if it was found, a
     * {@link BirthdayReminder BirthdayReminder} with ID set to -1 if it wasn't found, or null if an error occurred
     */
    public BirthdayReminder getBirthdayReminder(String userID, String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT message, dateandtime, channelid FROM birthdayreminders WHERE userid = ? AND serverid = ?");
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new BirthdayReminder(0, results.getString(1), results.getString(2), results.getString(3), serverID, userID);
            }
            return new BirthdayReminder(-1, "", "", "", "", "");
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getBirthdayReminder()");
            return null;
        }
    }

    /**
     * Gets {@link BirthdayReminderDefaults BirthdayReminderDefaults} from the database for the specified server
     *
     * @param serverID ID of the server from which the request came
     * @return the requested {@link BirthdayReminderDefaults BirthdayReminderDefaults} object with data in it if it was
     * found, {@link BirthdayReminderDefaults BirthdayReminderDefaults} object with serverID set to an empty string if
     * it wasn't found, or null if an error occurred
     */
    public BirthdayReminderDefaults getBirthdayReminderDefault(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT channelid, override FROM defaultbirthdayreminderchannels WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return new BirthdayReminderDefaults(serverID, results.getString(1), results.getBoolean(2));
            }
            return new BirthdayReminderDefaults("", "", false);
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getBirthdayReminderDefaults()");
            return null;
        }
    }

    /**
     * Gets a list of all {@link BirthdayReminder BirthdayReminders} set on the specified server
     *
     * @param serverID ID of the server from which all {@link BirthdayReminder BirthdayReminders} should be obtained
     * @return an ArrayList with all {@link BirthdayReminder BirthdayReminders} from the server, or null if an error
     * occurred
     */
    public ArrayList<BirthdayReminder> getBirthdayRemindersFromServer(String serverID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT * FROM birthdayreminders WHERE serverid = ?");
            preparedStatement.setString(1, serverID);
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<BirthdayReminder> reminders = new ArrayList<>();
            while (results.next()) {
                reminders.add(new BirthdayReminder(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6)));
            }
            return reminders;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BirthdayRemindersDatabase.getBirthdayRemindersFromServer()");
            return null;
        }
    }
}
