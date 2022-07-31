package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class ReactionsDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionsDatabase.class);

    private static volatile ReactionsDatabase instance;

    private Connection reactionsConnection;

    /**
     * Initializes the connection to the database located at
     * databases/data/reactions.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private ReactionsDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            reactionsConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/reactions.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to reactions.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static ReactionsDatabase getInstance() {
        ReactionsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ReactionsDatabase.class) {
            if (instance == null) {
                instance = new ReactionsDatabase();
            }
            return instance;
        }
    }

    /**
     * Retrieves all reactions from the database
     *
     * @return ArrayList of {@link Reaction Reactions} for every record in the database or null if an error occurred
     */
    public ArrayList<Reaction> getAllReactions() {
        try {
            PreparedStatement preparedStatement = reactionsConnection.prepareStatement("SELECT _id,  name, description, pattern, actionID, actionContent FROM reactions");
            ResultSet results = preparedStatement.executeQuery();
            ArrayList<Reaction> reactions = new ArrayList<>();
            while (results.next()) {
                reactions.add(new Reaction(results.getInt(1), results.getString(2), results.getString(3), createPatternsArrayList(results.getString(4)), results.getInt(5), results.getString(6)));
            }
            return reactions;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at ReactionsDatabase.getAllReactions()");
            return null;
        }
    }

    /**
     * Splist the string by the commas and returns the split contents as an ArrayList
     *
     * @param patterns String patterns separated by commas
     * @return an ArrayList of Strings generated from the comma separated input String
     */
    private ArrayList<String> createPatternsArrayList(String patterns) {
        ArrayList<String> patternsArrayList = new ArrayList<>();
        Collections.addAll(patternsArrayList, patterns.split(","));
        return patternsArrayList;
    }
}
