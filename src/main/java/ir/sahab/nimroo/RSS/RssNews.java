package ir.sahab.nimroo.RSS;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class RssNews {
  ArrayList<Site> listOfSites;
  PrintWriter writer;

  public RssNews() throws FileNotFoundException, UnsupportedEncodingException {
    listOfSites = new ArrayList<>();
    writer = new PrintWriter("newsLink.txt", "UTF-8");
  }

  public void addSite(String rssUrl, String siteName) {
    listOfSites.add(new Site(rssUrl, siteName));
  }

  public ArrayList<String> getNewsTitle()
      throws ParserConfigurationException, SAXException, IOException {
    ArrayList<String> topics = new ArrayList<>();
    for (Site site : listOfSites) {
      ArrayList<HashMap<String, String>> rssDataMap = getRssData(site.rssUrl);
      for (HashMap news : rssDataMap) {
        topics.add((String) news.get("title"));
        writer.println((String) news.get("link"));
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
