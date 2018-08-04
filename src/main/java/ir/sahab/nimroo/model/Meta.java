package ir.sahab.nimroo.model;

public class Meta {
    private String name = null;
    private String content = null;
    private String charset = null;
    private String httpEquiv = null;
    private String scheme = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHttpEquiv() {
        return httpEquiv;
    }

    public void setHttpEquiv(String httpEquiv) {
        this.httpEquiv = httpEquiv;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String toString() {
        return name + "\n" + content + "\n" + charset + "\n" + httpEquiv + "\n" + scheme;
    }
}
