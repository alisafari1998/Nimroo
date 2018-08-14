package ir.sahab.nimroo.crawler.cache;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author ArminF96
 */
public abstract class UrlCache {

  public abstract boolean add(String url, long time);

  public abstract boolean contains(String url);

  public abstract void remove(String url);

  public abstract boolean isEmpty();

  public abstract int size();

  public abstract void scrap();

  public String getHostName(String url) {
    if (url.startsWith("https://")) url = url.substring(8);
    if (url.startsWith("http://")) url = url.substring(7);
    if (url.startsWith("www.")) url = url.substring(4);
    int lastIndex = url.indexOf('/');
    if (lastIndex == -1) lastIndex = url.length();
    return url.substring(0, lastIndex);
  }

  public String getHash(String str) {
    return DigestUtils.md5Hex(str);
  }

  public abstract double memoryInUse();
}
