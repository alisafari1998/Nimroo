package ir.sahab.nimroo.model;

import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DummyUrlCache extends UrlCache {

  private HashSet<String> cache;
  private static final Logger logger = Logger.getLogger(DummyDomainCache.class);

  public DummyUrlCache(){
    cache = new HashSet<>();
    PropertyConfigurator.configure("log4j.properties");
  }

  @Override
  public boolean add(String url, long time) {
    if (memoryInUse() > 1100) {
      logger.warn(
          "DummyUrlCache HashSet use more than 1 gigabyte of main memory, scrap method recommended!");
    }
    String urlHash = getHash(url);
    if(cache.contains(urlHash))
      return false;
    cache.add(urlHash);
    return true;
  }

  @Override
  public boolean contains(String url) {
    return cache.contains(getHash(url));
  }

  @Override
  public void remove(String url) {
    cache.remove(getHash(url));
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
    cache.clear();
  }

  @Override
  public double memoryInUse() {
    return cache.size() * 16.0 / (1 << 20);
  }
}
