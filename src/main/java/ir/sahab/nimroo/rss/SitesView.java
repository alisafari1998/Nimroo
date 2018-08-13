package ir.sahab.nimroo.rss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class SitesView {
  RssNews rssNews;
  ArrayList<String> stopWords;

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
    for (String topic : topics) {
      String temp = topic.toLowerCase();
      String regex = ":";
      temp = temp.replaceAll(regex, "");
      regex = "'s";
      temp = temp.replaceAll(regex, "");
      regex = "’s";
      temp = temp.replaceAll(regex, "");
      regex = "‘";
      temp = temp.replaceAll(regex, "");
      regex = "'";
      temp = temp.replaceAll(regex, "");
      regex = "’";
      temp = temp.replaceAll(regex, "");
      regex = ",";
      temp = temp.replaceAll(regex, "");
      regex = "&";
      temp = temp.replaceAll(regex, "");
      regex = "_";
      temp = temp.replaceAll(regex, "");
      regex = "–";
      temp = temp.replaceAll(regex, "");
      regex = "0";
      temp = temp.replaceAll(regex, "");
      regex = "1";
      temp = temp.replaceAll(regex, "");
      regex = "2";
      temp = temp.replaceAll(regex, "");
      regex = "3";
      temp = temp.replaceAll(regex, "");
      regex = "4";
      temp = temp.replaceAll(regex, "");
      regex = "5";
      temp = temp.replaceAll(regex, "");
      regex = "6";
      temp = temp.replaceAll(regex, "");
      regex = "7";
      temp = temp.replaceAll(regex, "");
      regex = "8";
      temp = temp.replaceAll(regex, "");
      regex = "9";
      temp = temp.replaceAll(regex, "");
      keywordExtractor.addForExtractingKeywords(temp);
    }
    return keywordExtractor.getKeywords();
  }
}
