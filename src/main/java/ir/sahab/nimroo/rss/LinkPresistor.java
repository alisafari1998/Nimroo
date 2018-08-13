package ir.sahab.nimroo.rss;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class LinkPresistor {
  private PrintWriter writer;

  LinkPresistor(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
    writer = new PrintWriter(fileName, "UTF-8");
  }

  void write(String text) {
    writer.append(text).append("\n");
    writer.flush();
  }

  void closePersistor() {
    writer.close();
  }
}
