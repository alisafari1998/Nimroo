package ir.sahab.nimroo.model;

import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** @author ArminF96 */
public class DummyDomainCache extends UrlCache {

  private HashMap<String, Long> cache;
  private long expirationTime;
  private static final Logger logger = Logger.getLogger(DummyDomainCache.class);

  /** @param time in millisecond and Specifies that when the domain become deprecated. */
  public DummyDomainCache(long time) {
    this.expirationTime = time;
    cache = new HashMap<>();
    PropertyConfigurator.configure("log4j.properties");
  }

  @Override
  public boolean add(String url, long newTime) {
    if (memoryInUse() > 1100) {
      logger.warn(
          "DummyDomainCache HashMap use more than 1 gigabyte of main memory, scrap method recommended!");
    }
    String domainHash = getHash(getHostName(url));
    Long lastTime = cache.get(domainHash);
    if (lastTime == null) {
      cache.put(domainHash, newTime);
      return true;
    }
    if (newTime - lastTime > expirationTime) {
      cache.replace(domainHash, newTime);
      return true;
    }
    return false;
  }

  public long addByTime(String url, long newTime) {
    if (memoryInUse() > 1100) {
      logger.warn(
          "DummyDomainCache HashMap use more than 1 gigabyte of main memory, scrap method recommended!");
    }
    String domainHash = getHash(getHostName(url));
    Long lastTime = cache.get(domainHash);
    if (lastTime == null) {
      cache.put(domainHash, newTime);
      return 0;
    }
    if (newTime - lastTime > expirationTime) {
      cache.replace(domainHash, newTime);
      return 1;
    }
    return expirationTime - (newTime - lastTime);
  }

  @Override
  public boolean contains(String url) {
    return cache.containsKey(getHash(getHostName(url)));
  }

  @Override
  public void remove(String url) {
    cache.remove(getHash(getHostName(url)));
  }

  @Override
  public boolean isEmpty() {
    return cache.isEmpty();
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public void scrap() {
    Long time = new Date().getTime();
    for (HashMap.Entry<String, Long> entry : cache.entrySet()) {
      if (time - entry.getValue() > expirationTime) cache.remove(entry.getKey());
    }
  }

  @Override
  public int memoryInUse() {
    return cache.size() * (16 + 8) / (1 << 20);
  }
}
