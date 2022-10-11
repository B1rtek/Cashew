package com.birtek.cashew.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

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
     * Gets randomly an ID of a question not answered yet by the user, if the user answered all questions the ID
     * is set to 0
     * @param userID ID of the user who requested a question
     * @param difficulty difficulty of the requested question
     * @return random ID of a question not answered yet by the user or 0 if all were already answered
     */
    private int getRandomNotAnsweredQuestionID(String userID, int difficulty) {
        TriviaStatsDatabase database = TriviaStatsDatabase.getInstance();
        TriviaStats userStats = database.getUserStats(userID);
        ArrayList<Integer> goodIDs = new ArrayList<>();
        for(int i=0; i<hardnessMap.size(); i++) {
            if(userStats.progress().length() < i+1 || userStats.progress().charAt(i) == '0') {
                if(difficulty == hardnessMap.get(i) || difficulty == 0) {
                    goodIDs.add(i+1);
                }
            }
        }
        if(goodIDs.isEmpty()) {
            return 0;
        } else {
            Random random = new Random();
            return goodIDs.get(random.nextInt(goodIDs.size()));
        }
    }

    /**
     * Gets a random question from the database, with a 50% chance of it being not yet answered by the user
     *
     * @param userID ID of the user who requested a question
     * @param difficulty difficulty of the question to get, if set to 0 then the difficulty will be random
     * @return a {@link TriviaQuestion TriviaQuestion} with all details about the randomly selected question, or null if
     * an error occurred
     */
    public TriviaQuestion getRandomQuestion(String userID, int difficulty) {
        Random random = new Random();
        boolean notAnswered = random.nextBoolean();
        int notAnsweredQuestionID = 0;
        if(notAnswered) {
            notAnsweredQuestionID = getRandomNotAnsweredQuestionID(userID, difficulty);
        }
        try {
            PreparedStatement preparedStatement;
            if(notAnswered && notAnsweredQuestionID != 0) {
                preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT * FROM Questions WHERE _id = ?");
                preparedStatement.setInt(1, notAnsweredQuestionID);
            } else {
                if (difficulty == 0) {
                    preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT * FROM Questions ORDER BY RANDOM() LIMIT 1");
                } else {
                    preparedStatement = triviaQuestionsConnection.prepareStatement("SELECT * FROM Questions WHERE difficulty = ? ORDER BY RANDOM() LIMIT 1");
                    preparedStatement.setInt(1, difficulty);
                }
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
