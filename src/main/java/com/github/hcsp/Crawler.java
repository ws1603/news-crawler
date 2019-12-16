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

public class Crawler {

    private CrawlerDao dao = new MybatisCrawlerDao();

    private void run() throws SQLException, IOException {
        String link;
        //从数据库中加载下一个链接，如果能玩加载到则进行循环
        while ((link = dao.getNextLinkThenDelete()) != null) {

            if (dao.isLinkProcessed(link)) {
                continue;
            }

            if (isInterestedLink(link)) {

                Document doc = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDatabase(doc);

                storeIntoDatabaseIfItIsNewsPage(doc, link);

                dao.insertProcessedLink(link);
//                dao.updateDatabase(link, "insert into links_already_processed (link) values(?)");
            }
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https" + href;
            }
            if (!(href.toLowerCase().startsWith("javascript") || href.startsWith("#"))) {
                dao.insertLinkToBeProcessed(href);
//                dao.updateDatabase(href, "insert into links_to_be_processed (link) values(?)");
            }
        }
    }

    private void storeIntoDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p").text();
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        //这是我们感兴趣的，我们只处理新浪站内的链接
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (link.contains("\\/")) {
            link = link.replace("\\/", "/");
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
