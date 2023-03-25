package com.birtek.cashew.badwayfarersubmissions;

import com.birtek.cashew.database.LeaderboardRecord;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.sql.*;
import java.util.ArrayList;

public class PostsDatabase {
    private static volatile PostsDatabase instance;
    private Connection databaseConnection;
    private final String databaseURL = System.getenv("JDBC_DATABASE_URL");

    private PostsDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Missing Postgres JDBC driver");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            databaseConnection = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            System.err.println("Couldn't connect to the Postgres database - database could be offline or the url might be wrong or being currently refreshed");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists submissions(_id serial primary key, file_id text, caption text, author text)");
            preparedStatement.execute();
        } catch (SQLException e) {
            System.err.println("Failed to create the submissions table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists submissionstats(author text primary key, postscount integer)");
            preparedStatement.execute();
        } catch (SQLException e) {
            System.err.println("Failed to create the submissionstats table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select column_name from information_schema.columns where table_name='submissionstats' and column_name='chat_id'");
            ResultSet results = preparedStatement.executeQuery();
            if(!results.next()) {
                preparedStatement = databaseConnection.prepareStatement("alter table submissionstats add column chat_id text, add column show_tag boolean");
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            System.err.println("Failed to add the chat_id column");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static PostsDatabase getInstance() {
        PostsDatabase result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PostsDatabase.class) {
            if (instance == null) {
                instance = new PostsDatabase();
            }
            return instance;
        }
    }

    private boolean reestablishConnection() {
        try {
            databaseConnection = DriverManager.getConnection(databaseURL);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public ArrayList<Post> getAllPosts() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, author from submissions where verified = true order by _id");
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Post> posts = new ArrayList<>();
            while (resultSet.next()) {
                posts.add(new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), ""));
            }
            return posts;
        } catch (SQLException e) {
            System.err.println("Failed to get a list of all verified posts!");
            e.printStackTrace();
            return null;
        }
    }

    public Post getPostByID(int id) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, s1.author, s2.chat_id from submissions s1 join submissionstats s2 on s1.author = s2.author where _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Failed to get a post by id!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean addSubmission(NewSubmission newSubmission, String author, Long chatId) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("insert into submissions(file_id, caption, author, verified) values(?, ?, ?, ?)");
            preparedStatement.setString(1, newSubmission.getFileID());
            preparedStatement.setString(2, newSubmission.getDescription());
            preparedStatement.setString(3, author);
            preparedStatement.setBoolean(4, false);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Failed to add a new submission to the database!");
            e.printStackTrace();
            return false;
        }
    }

    public Post getUnverifiedPost() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, s1.author, s2.chat_id from submissions s1 join submissionstats s2 on s1.author = s2.author where verified = false order by _id limit 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
            }
            return new Post(0, "", "", "", "");
        } catch (SQLException e) {
            System.err.println("Failed to get a new unverified submission!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyPost(int id) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("update submissions set verified = true where _id = ?");
            preparedStatement.setInt(1, id);
            if (preparedStatement.executeUpdate() != 1) return false;
            updateSubmissionsCount(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to verify a post!");
            e.printStackTrace();
            return false;
        }
    }

    private void updateSubmissionsCount(int id) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select count(*) from submissionstats where author = (select author from submissions where _id = ?)");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) == 1;
            }
            if (exists) {
                preparedStatement = databaseConnection.prepareStatement("update submissionstats set postscount = postscount + 1 where author = (select author from submissions where _id = ?)");
            } else {
                preparedStatement = databaseConnection.prepareStatement("insert into submissionstats values((select author from submissions where _id = ?), 1)");
            }
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update posts count!");
            e.printStackTrace();
        }
    }

    public boolean removePost(int id) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("delete from submissions where _id = ?");
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Failed to remove a post!");
            e.printStackTrace();
            return false;
        }
    }

    public int getVerifiedPostsCount() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return -1;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select count(*) from submissions where verified = true");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Failed to get a list of all verified posts!");
            e.printStackTrace();
            return -1;
        }
    }

    public Post getOldestVerifiedPost() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, s1.author, s2.chat_id from submissions s1 join submissionstats s2 on s1.author = s2.author where verified = true order by _id limit 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
            }
            return new Post(0, "", "", "", "");
        } catch (SQLException e) {
            System.err.println("Failed to get the oldest unverified submission!");
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<LeaderboardRecord> getTopSubmitters() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select author, postscount from submissionstats order by postscount desc limit 10");
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<LeaderboardRecord> leaderboard = new ArrayList<>();
            while (resultSet.next()) {
                leaderboard.add(new LeaderboardRecord(0, resultSet.getString(1), resultSet.getInt(2)));
            }
            return leaderboard;
        } catch (SQLException e) {
            System.err.println("Failed to get the list of top submitters!");
            e.printStackTrace();
            return null;
        }
    }

    public Pair<Integer, Integer> getQueueStats() {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select count(*), verified from submissions group by verified order by verified");
            ResultSet resultSet = preparedStatement.executeQuery();
            int verified = 0, unverified = 0;
            while (resultSet.next()) {
                if (resultSet.getBoolean(2)) {
                    verified = resultSet.getInt(1);
                } else {
                    unverified = resultSet.getInt(1);
                }
            }
            return Pair.of(verified, unverified);
        } catch (SQLException e) {
            System.err.println("Failed to get the queue stats!");
            e.printStackTrace();
            return null;
        }
    }

    private boolean userHasChatID(String username) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select chat_id from submissionstats where author = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String chatID = resultSet.getString(1);
                return chatID != null && !chatID.isEmpty();
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to check whether the user is already in the database!");
            e.printStackTrace();
            return false;
        }
    }

    public Submitter getSubmitterStats(String author) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return null;
            }
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select * from submissionstats where author = ?");
            preparedStatement.setString(1, author);
            ResultSet results = preparedStatement.executeQuery();
            if(results.next()) {
                return new Submitter(results.getString(1), results.getInt(2), results.getString(3), results.getBoolean(4));
            }
            return new Submitter(author, 0, "", false);
        } catch (SQLException e) {
            System.err.println("Failed to get submission stats for " + author + "!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveChatID(String author, String chatID) {
        try {
            if (databaseConnection.isClosed()) {
                if (!reestablishConnection()) return false;
            }
            if(!userHasChatID(author)) {
                Submitter submitterStats = getSubmitterStats(author);
                if(submitterStats != null) {
                    PreparedStatement preparedStatement;
                    if(submitterStats.submissionsCount() == 0) {
                        preparedStatement = databaseConnection.prepareStatement("insert into submissionstats values(?, ?, ?, ?)");
                        preparedStatement.setString(1, author);
                        preparedStatement.setInt(2, 0);
                        preparedStatement.setString(3, chatID);
                        preparedStatement.setBoolean(4, false);
                    } else {
                        preparedStatement = databaseConnection.prepareStatement("update submissionstats set chat_id = ? where author = ?");
                        preparedStatement.setString(1, chatID);
                        preparedStatement.setString(2, author);
                    } return preparedStatement.executeUpdate() == 1;
                }
            }
            return true;
        }  catch (SQLException e) {
            System.err.println("Failed to add the chat ID!");
            e.printStackTrace();
            return false;
        }
    }
}
