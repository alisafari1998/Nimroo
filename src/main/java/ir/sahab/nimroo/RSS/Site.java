package ir.sahab.nimroo.RSS;

public class Site {
  String rssUrl;
  private String siteName;

  Site(String rssUrl, String siteName) {
    this.rssUrl = rssUrl;
    this.siteName = siteName;
  }
}
