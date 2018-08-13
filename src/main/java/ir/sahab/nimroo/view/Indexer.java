package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;

import ir.sahab.nimroo.hbase.HBase;
import java.io.IOException;

public class Indexer {
  public static void main(String[] args) throws IOException, InterruptedException {
      Config.load();
//      StoreManager storeManager = new StoreManager();
//      storeManager.start();
    HBase.getInstance().storeFromKafka();
  }
}
