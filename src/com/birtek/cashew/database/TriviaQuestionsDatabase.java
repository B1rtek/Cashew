package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class TriviaQuestionsDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriviaQuestionsDatabase.class);

    private static volatile TriviaQuestionsDatabase instance;

    private Connection triviaQuestionsConnection;

    private final ArrayList<Integer> hardnessMap = new ArrayList<>();

    private final HashMap<Integer, Integer> questionsDistribution = new HashMap<>();

    /**
     * Initializes the connection to the database located at
     * databases/data/trivia.db
     * If the connection fails to be established or the JDBC driver isn't found,
     * the bot exits with status 1 as it can't properly function without the database
     */
    private TriviaQuestionsDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Missing SQLite JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            triviaQuestionsConnection = DriverManager.getConnection("jdbc:sqlite:databases/data/trivia.db");
        } catch (SQLException e) {
            LOGGER.error("Couldn't connect to trivia.db - possibly missing database");
            e.printStackTrace();
            System.exit(1);
        }

        createHardnessMap();
        createDistributionMap();
    }

    /**
     * Creates a list telling the difficulty of each question
     */
    private void createHardnessMap() {
        try {
            PreparedStatement preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT difficulty FROM Questions ORDER BY _id");
            ResultSet results = preparedStatement.executeQuery();
            while(results.next()) {
                hardnessMap.add(results.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaQuestionsDatabase.createHardnessMap()");
        }
    }

    public ArrayList<Integer> getHardnessMap() {
        return hardnessMap;
    }

    /**
     * Used to get access to the singleton instance of the database
     */
    public static TriviaQuestionsDatabase getInstance() {
        TriviaQuestionsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (TriviaQuestionsDatabase.class) {
            if (instance == null) {
                instance = new TriviaQuestionsDatabase();
            }
            return instance;
        }
    }

    /**
     * Gets a random question from the database
     *
     * @param difficulty difficulty of the question to get, if set to 0 then the difficulty will be random
     * @return a {@link TriviaQuestion TriviaQuestion} with all details about the randomly selected question, or null if
     * an error occurred
     */
    public TriviaQuestion getRandomQuestion(int difficulty) {
        try {
            PreparedStatement preparedStatement;
            if (difficulty == 0) {
                preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT * FROM Questions ORDER BY RANDOM() LIMIT 1");
            } else {
                preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT * FROM Questions WHERE difficulty = ? ORDER BY RANDOM() LIMIT 1");
                preparedStatement.setInt(1, difficulty);
            }
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                String[] answers = results.getString(4).toLowerCase(Locale.ROOT).split(",");
                ArrayList<String> correctAnswers = new ArrayList<>(Arrays.asList(answers));
                return new TriviaQuestion(results.getInt(1), results.getString(2), correctAnswers, results.getInt(3), results.getString(5), results.getInt(6));
            } else return null;
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaQuestionsDatabase.getRandomQuestion()");
            return null;
        }
    }

    /**
     * Creates the map describing how many questions there are of each type
     */
    private void createDistributionMap() {
        try {
            PreparedStatement preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT COUNT(*), difficulty FROM Questions GROUP BY difficulty ORDER BY difficulty");
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                questionsDistribution.put(results.getInt(2), results.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.warn(e + " thrown at TriviaQuestionsDatabase.getQuestionsCountByType()");
        }
    }

    /**
     * Gets a HashMap describing how many questions there are of each type
     *
     * @return a HashMap which maps the difficulty to the number of questions of that difficulty
     */
    public HashMap<Integer, Integer> getQuestionsCountByType() {
        return questionsDistribution;
    }
}