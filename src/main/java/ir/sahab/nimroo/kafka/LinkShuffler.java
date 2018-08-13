package ir.sahab.nimroo.kafka;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.controller.Controller;
import org.apache.log4j.Logger;

public class LinkShuffler implements Runnable {
  private final Object LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING;
  private Logger logger = Logger.getLogger(LinkShuffler.class);
  private Controller controller;
  private KafkaLinkProducer kafkaLinkProducer = new KafkaLinkProducer();
  private String tempLinkArray[] = new String[100000];

  public LinkShuffler(Controller controller) {
    LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING = new Object();
    this.controller = controller;
  }

  public Object getLOCK_FOR_WAIT_AND_NOTIFY_PRODUCING() {
    return LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING;
  }

  @Override
  public void run() {
    while (true) {
      synchronized (LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING) {
        try {
          LOCK_FOR_WAIT_AND_NOTIFY_PRODUCING.wait();
        } catch (InterruptedException e) {
          logger.error("InterruptedException happen!", e);
        }
      }
      for (int i = 0; i < 100000; i++) {
        tempLinkArray[i] = controller.getFromLinkQueue();
      }
      for (int i = 0; i < 1000; i++) {
        logger.info("number of produced links:" + i * 100);
        for (int j = i; j < 100000; j += 1000) {
          kafkaLinkProducer.send(Config.kafkaLinkTopicName, tempLinkArray[j], tempLinkArray[j]);
        }
      }
    }
  }
}
