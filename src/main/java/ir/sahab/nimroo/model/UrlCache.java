package ir.sahab.nimroo.model;

import java.util.Date;

public interface UrlCache {

  boolean add(String url, Date date);

  boolean contains(String url);

  boolean remove(String url);

  boolean isEmpty();

  int size();

  void scrap();

  String getHostName(String url);
}
