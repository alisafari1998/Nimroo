package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.controller.Controller;
import ir.sahab.nimroo.model.Language;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class App {

  public static void main(String[] args) throws InterruptedException, IOException {
    Config.load();
    Controller controller = new Controller();
    PropertyConfigurator.configure("log4j.properties");
    Language.getInstance().init();
    ElasticsearchUI elasticsearchUI = new ElasticsearchUI();
    Thread searchThread = new Thread(elasticsearchUI);
    searchThread.start();
    controller.start();
  }
}
