package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BestNekoGifsDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BestNekoGifsDatabase.class);
    private static volatile BestNekoGifsDatabase instance;
    private Connection bestNekoGifsConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/bestneko.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private BestNekoGifsDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            bestNekoGifsConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/bestneko.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to bestneko.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static BestNekoGifsDatabase getInstance() {
        BestNekoGifsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (BestNekoGifsDatabase.class) {
            if (instance == null) {
                instance = new BestNekoGifsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets an ArrayList containing all nekos' names in their ID's order
     *
     * @return ArrayList containing Strings with names of all nekos or null if an error occurred
     */
    public ArrayList<String> getNekos() {
        try {
            PreparedStatement preparedStatement = bestNekoGifsConnection.prepareStatement("SELECT neko FROM Nekos");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<String> nekos = new ArrayList<>();
            while (results.next()) {
                nekos.add(results.getString(1));
            }
            return nekos;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoGifsDatabase.getNekos()");
            return null;
        }
    }

    /**
     * Gets an ArrayList of ArrayLists containing gifs for every neko, with gifs of every neko on separate lists
     * corresponding to nekos' order in the list returned from getNekos()
     *
     * @return ArrayList containing all neko gifs or null if an error occurred
     */
    public ArrayList<ArrayList<String>> getNekoGifs() {
        try {
            PreparedStatement preparedStatement = bestNekoGifsConnection.prepareStatement("SELECT * FROM BestnekoGifs ORDER BY nekoID");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<ArrayList<String>> nekoGifs = new ArrayList<>();
            ArrayList<String> currentNekoGifs = new ArrayList<>();
            int id = 1;
            while (results.next()) {
                if (results.getInt(1) != id) {
                    id = results.getInt(1);
                    nekoGifs.add(new ArrayList<>(currentNekoGifs));
                    currentNekoGifs.clear();
                }
                currentNekoGifs.add(results.getString(2));
            }
            nekoGifs.add(currentNekoGifs);
            return nekoGifs;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoGifsDatabase.getNekoGifs()");
            return null;
        }
    }

    /**
     * Gets a HashMap with all nekos' theme colors
     * @return a HashMap of nekos' names mapped to {@link Color Colors}, or null if an error occurred
     */
    public HashMap<String, Color> getNekoColors() {
        try {
            PreparedStatement preparedStatement = bestNekoGifsConnection.prepareStatement("SELECT neko, color FROM Nekos");
            ResultSet results = preparedStatement.executeQuery();
            HashMap<String, Color> nekoColors = new HashMap<>();
            while(results.next()) {
                nekoColors.put(results.getString(1), new Color(results.getInt(2)));
            }
            return nekoColors;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at BestNekoGifsDatabase.getNekoColors()");
            return null;
        }
    }
}
