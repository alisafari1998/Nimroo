package ir.sahab.nimroo.model;

public class Site {
    String rssUrl;
    String siteName;

    public Site(String rssUrl, String siteName){
        this.rssUrl = rssUrl;
        this.siteName = siteName;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

}
