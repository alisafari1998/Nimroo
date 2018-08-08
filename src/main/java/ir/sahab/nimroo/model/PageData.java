package ir.sahab.nimroo.model;

import java.util.ArrayList;

public class PageData { // todo h1, h2, ...
  private String url;
  private String title;
  private String text;
  private ArrayList<Link> links;
  private ArrayList<Meta> metas;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public ArrayList<Link> getLinks() {
    return links;
  }

  public void setLinks(ArrayList<Link> links) {
    this.links = links;
  }

  public ArrayList<Meta> getMetas() {
    return metas;
  }

  public void setMetas(ArrayList<Meta> metas) {
    this.metas = metas;
  }

  @Override
  public String toString() {
    return url + "\n" + title + "\n" + text + "\n" + links + "\n" + metas;
  }
}
