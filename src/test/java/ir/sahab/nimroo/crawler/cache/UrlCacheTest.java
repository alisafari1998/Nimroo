package ir.sahab.nimroo.crawler.cache;

import static org.junit.Assert.*;

import org.junit.Test;

public class UrlCacheTest {

  @Test
  public void getHostNameWithValidInputTest0() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    String result =
        dummyDomainCache.getHostName(
            "https://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8");
    assertEquals("google.com", result);
  }

  @Test
  public void getHostNameWithValidInputTest1() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    String result =
        dummyDomainCache.getHostName(
            "http://www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8");
    assertEquals("google.com", result);
  }

  @Test
  public void getHostNameWithValidInputTest2() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    String result =
        dummyDomainCache.getHostName(
            "www.google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8");
    assertEquals("google.com", result);
  }

  @Test
  public void getHostNameWithValidInputTest3() {
    DummyDomainCache dummyDomainCache = new DummyDomainCache(30 * 1000);
    String result =
        dummyDomainCache.getHostName(
            "google.com/search?client=ubuntu&channel=fs&q=how+to+create+test+for+class+in+intellij&ie=utf-8&oe=utf-8");
    assertEquals("google.com", result);
  }
}
