package ir.sahab.nimroo.view;

import ir.sahab.nimroo.model.Language;
import java.io.IOException;

/** Hello world! */
public class App {
  public static void main(String[] args) {
    System.out.println("Hello World!");
    try {
      Language.getInstance().init();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
