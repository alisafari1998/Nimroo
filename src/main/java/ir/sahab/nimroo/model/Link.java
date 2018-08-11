package ir.sahab.nimroo.model;

public class Link {
    private String link;
    private String anchor;

    public void setLink(String link) {this.link = link;}
    public String getLink() {return link;}

    public void setAnchor(String anchor) {this.anchor = anchor;}
    public String getAnchor() {return anchor;}

    @Override
    public String toString() {
        return link + "\n" + anchor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Link))
            return false;
        return ((Link) o).link.equals(link);
    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }
}
