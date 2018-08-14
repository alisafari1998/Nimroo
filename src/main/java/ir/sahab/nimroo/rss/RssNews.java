package ir.sahab.nimroo.rss;

import ir.sahab.nimroo.model.Site;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class RssNews {
  private ArrayList<Site> listOfSites;
  private final Logger logger = Logger.getLogger(RssNews.class);
  private LinkPresistor linkPresistor;

  RssNews() throws FileNotFoundException, UnsupportedEncodingException {
    listOfSites = new ArrayList<>();
    linkPresistor = new LinkPresistor("newsLink.txt");
  }

  void addSite(String rssUrl, String siteName) {
    listOfSites.add(new Site(rssUrl, siteName));
  }

  ArrayList<String> getNewsTitle() {
    ArrayList<String> topics = new ArrayList<>();
    for (Site site : listOfSites) {
      ArrayList<HashMap<String, String>> rssDataMap;
      try {
        rssDataMap = getRssData(site.getRssUrl());
        for (HashMap news : rssDataMap) {
          topics.add((String) news.get("title"));
          linkPresistor.write((String) news.get("link"));
        }
      } catch (IOException | SAXException | ParserConfigurationException e) {
        logger.error(e);
      }
    }
    return topics;
  }

  private ArrayList<HashMap<String, String>> getRssData(String rssUrl)
      throws IOException, SAXException, ParserConfigurationException {
    ArrayList<HashMap<String, String>> rssDataMap = new ArrayList<>();

    Document domTree;
    domTree = getRssXml(rssUrl);
    for (int i = 0; i < domTree.getElementsByTagName("item").getLength(); i++) {
      rssDataMap.add(new HashMap<>());
      for (int j = 0;
          j < domTree.getElementsByTagName("item").item(i).getChildNodes().getLength();
          j++) {
        if (checkTag(domTree, i, j, "title")) {
          rssDataMap.get(i).put("title", contentOfNode(domTree, i, j));
        } else if (checkTag(domTree, i, j, "link")) {
          rssDataMap.get(i).put("link", contentOfNode(domTree, i, j));
        } else if (checkTag(domTree, i, j, "pubDate")) {
          rssDataMap.get(i).put("pubDate", contentOfNode(domTree, i, j));
        } else if (checkTag(domTree, i, j, "description")) {
          rssDataMap.get(i).put("description", contentOfNode(domTree, i, j));
        }
      }
    }
    return rssDataMap;
  }

  private Document getRssXml(String rssUrl)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
    URL url = new URL(rssUrl);
    URLConnection con = url.openConnection();
    con.setConnectTimeout(5000);
    return domBuilder.parse(con.getInputStream());
  }

  private boolean checkTag(Document domTree, int domNodeNumber, int itemNodeNumber, String tag) {
    return domTree
        .getElementsByTagName("item")
        .item(domNodeNumber)
        .getChildNodes()
        .item(itemNodeNumber)
        .toString()
        .contains(tag);
  }

  private String contentOfNode(Document domTree, int domNodeNumber, int itemNodeNumber) {
    return domTree
        .getElementsByTagName("item")
        .item(domNodeNumber)
        .getChildNodes()
        .item(itemNodeNumber)
        .getTextContent();
  }
}
