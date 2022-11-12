package com.birtek.cashew.badwayfarersubmissions;

import java.sql.*;
import java.util.ArrayList;

public class PostsDatabase {
    private static volatile PostsDatabase instance;
    private final String databaseURL = System.getenv("JDBC_DATABASE_URL");
    private Connection databaseConnection;

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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("create table if not exists submissions(_id serial primary key, file_id text, caption text, author text, verified boolean)");
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
            while(resultSet.next()) {
                posts.add(new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)));
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, author from submissions where verified = true and _id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Failed to get a post by id!");
            e.printStackTrace();
            return null;
        }
    }

    public boolean addSubmission(NewSubmission newSubmission, String author) {
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement("select _id, file_id, caption, author from submissions where verified = false order by _id limit 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Post(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
            }
            return new Post(0,"", "", "");
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
            if(preparedStatement.executeUpdate() != 1) return false;
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
            if(resultSet.next()) {
                exists = resultSet.getInt(1) == 1;
            }
            if(exists) {
                preparedStatement = databaseConnection.prepareStatement("update submissionstats set postscount = postscount + 1 where author = (select author from submissions where _id = ?)");
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            } else {
                preparedStatement = databaseConnection.prepareStatement("insert into submissionstats values((select author from submissions where _id = ?), 1)");
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            }
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

}
