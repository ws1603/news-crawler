package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/D:/crawler/news-crawer/news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLink() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from links_to_be_processed limit 1"); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink();
        if (link != null) {
            updateDatabase(link, "delete from links_to_be_processed where link = ?");
        }
        return link;
    }

    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement1 = connection.prepareStatement("select * from news where title = ?")) {
            statement1.setString(1, title);
            resultSet = statement1.executeQuery();
            if (!resultSet.next()) {
                try (PreparedStatement statement2 = connection.prepareStatement("insert into news (title,url,content,created_at,modified_at) values(?,?,?,now(),now())")) {
                    statement2.setString(1, title);
                    statement2.setString(2, url);
                    statement2.setString(3, content);
                    statement2.executeUpdate();
                    System.out.println(title);
                    System.out.println(content);
                }
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from links_already_processed where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
}
