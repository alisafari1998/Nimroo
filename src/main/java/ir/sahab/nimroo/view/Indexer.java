package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.controller.StoreManager;

import java.io.IOException;

public class Indexer {
  public static void main(String[] args) throws IOException, InterruptedException {
      Config.load();
      StoreManager storeManager = new StoreManager();
      storeManager.start();
  }
}
