package ir.sahab.nimroo.crawler.cache;

import org.junit.Assert;
import org.junit.Test;

public class DummyUrlCacheTest {

  @Test
  public void add() {
    DummyUrlCache cache = new DummyUrlCache();
    Assert.assertTrue(cache.add("https://stackoverflow.com/questions/32552307/hashset-vs-arraylist-contains-performance/32552348"));
    Assert.assertTrue(cache.add("https://stackoverflow.com/questions/10799417/performance-and-memory-allocation-comparison-between-list-and-set"));
    Assert.assertFalse(cache.add("https://stackoverflow.com/questions/32552307/hashset-vs-arraylist-contains-performance/32552348"));
  }

}