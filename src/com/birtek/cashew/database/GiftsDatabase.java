package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class GiftsDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftsDatabase.class);

    private static volatile GiftsDatabase instance;

    private Connection giftsConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/gifts.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private GiftsDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            giftsConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/gifts.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to gifts.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static GiftsDatabase getInstance() {
        GiftsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (GiftsDatabase.class) {
            if (instance == null) {
                instance = new GiftsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets all available gifts
     * @return ArrayList of all available gifts in GiftInfo form
     */
    public ArrayList<GiftInfo> getAvailableGifts() {
        try {
            PreparedStatement preparedStatement = giftsConnection.prepareStatement("SELECT giftID, giftName, giftImageURL, reactionLine1, reactionLine2, displayName, color FROM Gifts");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<GiftInfo> availableGifts = new ArrayList<>();
            while (results.next()) {
                availableGifts.add(new GiftInfo(results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getString(6), new Color(results.getInt(7))));
            }
            return availableGifts;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at GiftsDatabase.getAvailableGifts()");
            return null;
        }
    }
}
