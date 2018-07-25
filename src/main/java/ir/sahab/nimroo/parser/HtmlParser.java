package ir.sahab.nimroo.parser;

import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.Meta;
import ir.sahab.nimroo.model.PageData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HtmlParser {
    private PageData pageData = new PageData();
    private ArrayList<Link> links = new ArrayList<>();
    private ArrayList<Meta> metas = new ArrayList<>();

    /** parses a html string and returns PageData */
    public PageData parse(String urlString, String htmlString) {
        pageData.setUrl(urlString);

        Document document = Jsoup.parse(htmlString);
        Element bodyElement = document.select("body").first();

        pageData.setTitle(document.title());
        pageData.setText(bodyElement.text());

        Elements aElements = document.select("a");
        //aElements.addAll(document.select("link"));  // is correct ?

        for (Element aElement: aElements) {
            String href = aElement.attr("href");
            String anchor = aElement.text();
            Link link = new Link();
            link.setAnchor(anchor);
            href = getCompleteUrl(urlString, href);
            link.setLink(href);

            links.add(link);
        }

        pageData.setLinks(links);

        Elements metaElements = document.select("meta");
        for (Element metaElement: metaElements) {
            Meta meta = new Meta();
            meta.setCharset     (metaElement.attr("charset"));
            meta.setContent     (metaElement.attr("content"));
            meta.setHttpEquiv   (metaElement.attr("http-equiv"));
            meta.setName        (metaElement.attr("name"));
            meta.setScheme      (metaElement.attr("scheme"));

            metas.add(meta);
        }

        pageData.setMetas(metas);

        /*Source source = new Source(htmlString);
        System.out.println(source.getTextExtractor());
        try {
            source.getAllElements(HTMLElementName.TITLE).get(0).;
            source.getAllElements(HTMLElementName.BODY).get(0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }*/

        return pageData;
    }

    public String getCompleteUrl(String url, String relativeUrl) {
        URL mainUrl;
        String host;

        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }

        try {
            mainUrl = new URL(url);
            host = mainUrl.getHost();
            if (relativeUrl.contains(host)) {
                return relativeUrl;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        int lastIndex = url.lastIndexOf('/');
        if (lastIndex == -1 || (lastIndex <= 7 && url.startsWith("http"))) {
            if (relativeUrl.startsWith("/")) {
                return url + relativeUrl;
            }
            return url + "/" + relativeUrl;
        }
        url = url.substring(0, lastIndex);
        if (relativeUrl.startsWith("/")) {
            return url + relativeUrl;
        }
        return url + "/" + relativeUrl;
    }
}
