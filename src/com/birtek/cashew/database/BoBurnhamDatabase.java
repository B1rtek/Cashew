package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Database class managing quotes for the /boburnham command
 */
public class BoBurnhamDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoBurnhamDatabase.class);

    private static volatile BoBurnhamDatabase instance;

    private Connection boBurnhamConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/boBurnhamQuotes.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private BoBurnhamDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            boBurnhamConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/boBurnhamQuotes.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to boBurnhamQuotes.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static BoBurnhamDatabase getInstance() {
        BoBurnhamDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (BoBurnhamDatabase.class) {
            if (instance == null) {
                instance = new BoBurnhamDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a random Bo Burnham's quote from the database
     * @param nsfw if set to true, will fetch a quote marked as nsfw
     * @return a BoBurnhamQuote containing all information about the selected quote
     */
    public BoBurnhamQuote getQuote(boolean nsfw) {
        try {
            PreparedStatement preparedStatement = boBurnhamConnection.prepareStatement("SELECT * FROM Quotes WHERE nsfw = ? ORDER BY RANDOM() LIMIT 1");
            preparedStatement.setInt(1, nsfw?1:0);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return new BoBurnhamQuote(results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6));
            }
            return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BoBurnhamDatabase.getQuote()");
            return null;
        }
    }
}
