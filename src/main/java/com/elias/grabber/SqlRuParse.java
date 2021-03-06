package com.elias.grabber;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.elias.grabber.model.Post;
import com.elias.grabber.store.PsqlStore;
import com.elias.grabber.store.Store;
import com.elias.grabber.utils.DateTimeParser;
import com.elias.grabber.utils.SqlRuDateTimeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRuParse implements Parse {

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());

    private static final String SITE_URL = "https://www.sql.ru/forum/job-offers/";

    private static final Properties CFG = new Properties();

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        DateTimeParser dateParser = new SqlRuDateTimeParser();
        Parse parse = new SqlRuParse(dateParser);
        List<Post> posts = parse.list(SITE_URL);
        Store store = new PsqlStore(CFG);
        posts.forEach(store::save);
        store.getAll().forEach(System.out::println);
    }

    @Override
    public List<Post> list(String link) {
        var posts = new ArrayList<Post>();
        for (var url : getPages(link)) {
            var doc = getDocument(url);
            var row = doc.select(".postslisttopic");
            var vacancies = row.subList(3, row.size());
            for (Element td : vacancies) {
                Element parent = td.parent();
                if (parent != null) {
                    var job = parent.children().get(1).child(0);
                    var href = job.attr("href");
                    if (checkJavaVacancy(job.text())) {
                        posts.add(detail(href));
                    }
                }
            }
        }
        return posts;
    }

    @Override
    public Post detail(String link) {
        var doc = getDocument(link);
        var title = getVacancyTitle(doc);
        var description = getVacancyDescription(doc);
        var createdDate = getVacancyDate(doc);
        return new Post(title, link, description, createdDate);
    }

    private Document getDocument(String link) {
        Document result = null;
        try {
            result = Jsoup.connect(link).get();
        } catch (Exception e) {
            LOG.error("Get DOM error", e);
        }
        return result;
    }

    private List<String> getPages(String url) {
        var pages = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            pages.add(url + i);
        }
        return pages;
    }

    private String getVacancyTitle(Document doc) {
        return Objects.requireNonNull(doc.select(".messageHeader").first()).ownText();
    }

    private String getVacancyDescription(Document document) {
        return document.select(".msgBody").get(1).text();
    }

    private LocalDateTime getVacancyDate(Document doc) {
        String result = Objects.requireNonNull(doc.select(".msgFooter").first()).text();
        return dateTimeParser.parse(result.substring(0, result.indexOf(" [")));
    }

    private boolean checkJavaVacancy(String vacancyName) {
        return vacancyName.contains("Java") && !vacancyName.contains("Javascript");
    }

}