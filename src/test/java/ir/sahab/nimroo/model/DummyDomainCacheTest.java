package ir.sahab.nimroo.model;

import static org.junit.Assert.*;

import java.util.Date;
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
  public void memoryInUse() {}
}
