package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.controller.DummyController;
import ir.sahab.nimroo.model.Language;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.logging.Logger;

/** Hello world! */
public class App {

  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Hello World!");
    Config.load();
    DummyController dc = new DummyController();
    PropertyConfigurator.configure("log4j.properties");
    Language.getInstance().init();
    dc.start();
  }
}
