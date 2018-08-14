package ir.sahab.nimroo.crawler;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.crawler.util.Language;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class CrawlerLauncher {

  public static void main(String[] args) throws InterruptedException, IOException {
    Config.load();
    Crawler crawler = new Crawler();
    PropertyConfigurator.configure("log4j.properties");
    Language.getInstance().init();
    crawler.start();
  }
}
