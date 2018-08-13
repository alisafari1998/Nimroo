package ir.sahab.nimroo.RSS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class SitesView {
  private RssNews rssNews;
  private ArrayList<String> stopWords;

  public SitesView() throws FileNotFoundException, UnsupportedEncodingException {
    rssNews = new RssNews();
    File file = new File("NewsResource/rss links");
    Scanner sc = new Scanner(file);
    while (sc.hasNextLine()) {
      rssNews.addSite(sc.nextLine(), "no name");
    }
    stopWords = new ArrayList<>();
    file = new File("NewsResource/stopWords");
    sc = new Scanner(file);
    while (sc.hasNextLine()) {
      stopWords.add(sc.nextLine());
    }
  }

  public ArrayList<String> getKeywordsOfLatestNews() {
    ArrayList<String> topics = rssNews.getNewsTitle();
    KeywordExtractor keywordExtractor = new KeywordExtractor(stopWords, 20);
    ArrayList<String> regex = new ArrayList<>();
    regex.add(":");
    regex.add("'s");
    regex.add("’s");
    regex.add("‘");
    regex.add("'");
    regex.add("’");
    regex.add(",");
    regex.add("&");
    regex.add("_");
    regex.add("–");
    regex.add("0");
    regex.add("1");
    regex.add("2");
    regex.add("3");
    regex.add("4");
    regex.add("5");
    regex.add("6");
    regex.add("7");
    regex.add("8");
    regex.add("9");
    for (String topic : topics) {
      String temp = topic.toLowerCase();
      for (int i = 0; i < regex.size(); i++) {
        temp = temp.replaceAll(regex.get(0), "");
      }
      keywordExtractor.addForExtractingKeywords(temp);
    }
    return keywordExtractor.getKeywords();
  }
}
