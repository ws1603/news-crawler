package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/D:/crawler/news-crawer/news", USER_NAME, PASSWORD);
        try (PreparedStatement statement = connection.prepareStatement("insert into links_to_be_processed values('https://sina.cn')")) {
            statement.execute();
        }

        String link;
        //从数据库中加载下一个链接，如果能玩加载到则进行循环
        while ((link = getNextLinkThenDelete(connection)) != null) {

            if (isLinkProcessed(connection, link)) {
                continue;
            }

            if (isInterestedLink(link)) {

                Document doc = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);

                storeIntoDatabaseIfItIsNewsPage(doc, connection, link);

                updateDatabase(connection, link, "insert into links_already_processed (link) values(?)");
            }
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if(href.startsWith("//")){
                href = "https" + href;
            }
            if(!(href.toLowerCase().startsWith("javascript") || href.startsWith("#"))){
                updateDatabase(connection, href, "insert into links_to_be_processed (link) values(?)");
            }
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void updateDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static String getNextLink(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from links_to_be_processed limit 1"); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection);
        if (link != null) {
            updateDatabase(connection, link, "delete from links_to_be_processed where link = ?");
//            if (link.contains("?")) {           //去除重复页面
//                link = link.substring(0, link.lastIndexOf("?"));
//            }
        }
        return link;
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc, Connection connection, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p").text();
                try (PreparedStatement statement = connection.prepareStatement("select * from news where title = ?")) {
                    statement.setString(1,title);
                    ResultSet resultSet = statement.executeQuery();
                    if(resultSet.next()){
                        continue;
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("insert into news (title,url,content,created_at,modified_at) values(?,?,?,now(),now())")) {
                    statement.setString(1, title);
                    statement.setString(2, link);
                    statement.setString(3, content);
                    statement.executeUpdate();
                    System.out.println(title);
                    System.out.println(content);
                }
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        //这是我们感兴趣的，我们只处理新浪站内的链接
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if(link.contains("\\/")){
            link = link.replace("\\/","/");
        }
        System.out.println(link);
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestedLink(String link) {
        return (isIndexPage(link) || isNewsLink(link) && isNotLoginPage(link));
    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

    private static boolean isNewsLink(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport");
    }
}
