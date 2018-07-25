package ir.sahab.nimroo.model;

import static org.junit.Assert.*;

import java.security.acl.LastOwnerException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class DummyDomainCacheTest {

  @Test
  public void addWithValidInputTest0() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    Assert.assertTrue(
        dummyDomainCache.add(
            "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime()));
  }

  @Test
  public void addWithValidInputTest1() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    Assert.assertTrue(
        dummyDomainCache.add(
            "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime()));
    Assert.assertFalse(
        dummyDomainCache.add(
            "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime()));
    assertEquals(1, dummyDomainCache.size());
  }

  @Test
  public void addWithValidInputTest2() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    Assert.assertTrue(
        dummyDomainCache.add(
            "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime() - 40 * 1000));

    Assert.assertFalse(
        dummyDomainCache.add(
            "https://www.google.com/search?client=ubuntu&channst+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime() - 12 * 1000));

    Assert.assertTrue(
        dummyDomainCache.add(
            "https://www.google.com/sto+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime() - 8 * 1000));

    Assert.assertFalse(
        dummyDomainCache.add(
            "https://google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
            new Date().getTime()));

    Assert.assertEquals(1, dummyDomainCache.size());
  }

  @Test
  public void scrapWithValidInputTest0() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    dummyDomainCache.add(
        "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
        new Date().getTime());
    dummyDomainCache.scrap();
    assertEquals(1, dummyDomainCache.size());
  }

  @Test
  public void scrapWithValidInputTest1() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    dummyDomainCache.add(
        "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8",
        new Date().getTime() - 31 * 1000);
    dummyDomainCache.scrap();
    assertEquals(0, dummyDomainCache.size());
  }

  @Test
  public void performanceTest0by1000000Link() {
    Long startTime = System.currentTimeMillis();
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    for (int i = 0; i < 100000; i++) {
      String string = randomAlphaNumeric(20);
      for (int j = 0; j < 10; j++) {
        string = string.concat("/");
        string = string.concat(randomAlphaNumeric(50));
        Random rand = new Random();
        int randTime = rand.nextInt(100) - 50;
        dummyDomainCache.add(string, new Date().getTime() - randTime);
      }
    }
    long finishTime = new Date().getTime();
    if (finishTime - startTime > 3500) Assert.fail();
  }

  private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private static String randomAlphaNumeric(int count) {

    StringBuilder builder = new StringBuilder();

    while (count-- != 0) {

      int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());

      builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    }

    return builder.toString();
  }
}
