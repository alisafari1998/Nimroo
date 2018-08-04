package ir.sahab.nimroo.view;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.controller.StoreManager;

public class Indexer {
  public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {

      StoreManager storeManager = new StoreManager();
      storeManager.start();

  }
}
