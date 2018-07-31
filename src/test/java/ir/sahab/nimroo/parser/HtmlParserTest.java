package ir.sahab.nimroo.parser;

import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.Meta;
import ir.sahab.nimroo.model.PageData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class HtmlParserTest {
    @Test
    public void parserTest() {
        String htmlStirng = "\n" +
                "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Example Domain</title>\n" +
                "\n" +
                "    <meta charset=\"utf-8\" />\n" +
                "    <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "    <style type=\"text/css\">\n" +
                "    body {\n" +
                "        background-color: #f0f0f2;\n" +
                "        margin: 0;\n" +
                "        padding: 0;\n" +
                "        font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n" +
                "        \n" +
                "    }\n" +
                "    div {\n" +
                "        width: 600px;\n" +
                "        margin: 5em auto;\n" +
                "        padding: 50px;\n" +
                "        background-color: #fff;\n" +
                "        border-radius: 1em;\n" +
                "    }\n" +
                "    a:link, a:visited {\n" +
                "        color: #38488f;\n" +
                "        text-decoration: none;\n" +
                "    }\n" +
                "    @media (max-width: 700px) {\n" +
                "        body {\n" +
                "            background-color: #fff;\n" +
                "        }\n" +
                "        div {\n" +
                "            width: auto;\n" +
                "            margin: 0 auto;\n" +
                "            border-radius: 0;\n" +
                "            padding: 1em;\n" +
                "        }\n" +
                "    }\n" +
                "    </style>    \n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<div>\n" +
                "    <h1>Example Domain</h1>\n" +
                "    <p>This domain is established to be used for illustrative examples in documents. You may use this\n" +
                "    domain in examples without prior coordination or asking for permission.</p>\n" +
                "    <p><a href=\"http://www.iana.org/domains/example\">More information...</a></p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";

        PageData pageData = new HtmlParser().parse("url", htmlStirng);
        ArrayList<Meta> metas = pageData.getMetas();
        ArrayList<Link> links = pageData.getLinks();

        Assert.assertEquals(pageData.getUrl(), "url");

        Assert.assertEquals(pageData.getText(), "Example Domain This domain is established to be used for " +
                "illustrative examples in documents. You may use this domain in examples without prior coordination " +
                "or asking for permission. More information...");

        Assert.assertEquals(metas.size(), 3);
        Assert.assertEquals(metas.get(0).getCharset(), "utf-8");
        Assert.assertEquals(metas.get(2).getName(), "viewport");
        Assert.assertEquals(metas.get(2).getContent(), "width=device-width, initial-scale=1");

        Assert.assertEquals(pageData.getTitle(), "Example Domain");

        Assert.assertEquals(links.get(0).getAnchor(), "More information...");
        Assert.assertEquals(links.get(0).getLink(), "http://www.iana.org/domains/example");
    }

    @Test
    public void getCompleteUrlTest() {
        String url = "https://stackoverflow.com/questions/3365271/standard-url-normalization-java";
        String relativeUrl = "/qwerty.html";
        String relativeUrl2 = "qwerty.html";
        String relativeUrl3 = "https://stackoverflow.com/test123";
        String relativeUrl4 = "stackoverflow.com/test123/qwerty123";

        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl), "https://stackoverflow.com/questions/3365271/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl2), "https://stackoverflow.com/questions/3365271/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl3), "https://stackoverflow.com/test123");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl4), "stackoverflow.com/test123/qwerty123");

        String url1 = "google.com";
        String relativeUrl11 = "tweeter.com";
        String relativeUrl22 = "support.tweeter.com";

        Assert.assertEquals(htmlParser.getCompleteUrl(url1, relativeUrl11), "tweeter.com");
        Assert.assertEquals(htmlParser.getCompleteUrl(url1, relativeUrl22), "support.tweeter.com");
    }

    @Test
    public void testCompletedUrlWithHostUrl() {
        String url = "https://stackoverflow.com";
        String relativeUrl = "/qwerty.html";
        String relativeUrl2 = "qwerty.html";
        String relativeUrl3 = "https://stackoverflow.com/test123";
        String relativeUrl4 = "stackoverflow.com/test123";
        String relativeUrl5 = "overflow.com/test123";

        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl), "https://stackoverflow.com/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl2), "https://stackoverflow.com/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl3), "https://stackoverflow.com/test123");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl4), "stackoverflow.com/test123");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl5), "overflow.com/test123");
    }

    @Test
    public void testGetCompleteUrlWithWWW() {
        String url = "https://www.stackoverflow.com/questions/3365271/standard-url-normalization-java";
        String relativeUrl = "/qwerty.html";
        String relativeUrl2 = "qwerty.html";
        String relativeUrl3 = "https://stackoverflow.com/test123";
        String relativeUrl4 = "www.stackoverflow.com/test123/qwerty123";

        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl), "https://www.stackoverflow.com/questions/3365271/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl2), "https://www.stackoverflow.com/questions/3365271/qwerty.html");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl3), "https://stackoverflow.com/test123");
        Assert.assertEquals(htmlParser.getCompleteUrl(url, relativeUrl4), "www.stackoverflow.com/test123/qwerty123");
    }

    @Test
    public void isValidTest() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertTrue(htmlParser.isValid("http://www.stackoverflow.com"));
        Assert.assertTrue(htmlParser.isValid("https://www.stackoverflow.com"));
        Assert.assertTrue(htmlParser.isValid("https://www.stackoverflow.com/abc.html"));
        Assert.assertTrue(htmlParser.isValid("https://www.stackoverflow.com/abc.php"));
        Assert.assertTrue(htmlParser.isValid("https://www.stackoverflow.com/abc.php?t=5;q=12"));

        Assert.assertTrue(htmlParser.isValid("www.stackoverflow.com"));
        Assert.assertTrue(htmlParser.isValid("www.stackoverflow.com"));
        Assert.assertTrue(htmlParser.isValid("www.stackoverflow.com/abc.html"));
        Assert.assertTrue(htmlParser.isValid("www.stackoverflow.com/abc.php"));
        Assert.assertTrue(htmlParser.isValid("www.stackoverflow.com/abc.php?t=5;q=12"));

        Assert.assertFalse(htmlParser.isValid("https://www.stackoverflow.com/abc.pdf"));
        Assert.assertFalse(htmlParser.isValid("www.stackoverflow.com/abc.pdf"));
    }

    @Test
    public void getCompleteUrl2NoSlashOrDot() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl2("https://data.stackexchange.com", "meta.boardgames/queries"),
                "https://data.stackexchange.com/meta.boardgames/queries");
    }

    @Test
    public void getCompleteUrl2JustSlash() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl2("http://memory-alpha.wikia.com/wiki/Photonic_flea",
                "/wiki/Category:Spacecraft"),
                "http://memory-alpha.wikia.com/wiki/Category:Spacecraft");
    }

    @Test
    public void getCompleteUrl2DotSlash() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl2("https://plus.google.com/104378092678028911346",
                "./104378092678028911346/posts/3ff7wsCmKPB"),
                "https://plus.google.com/104378092678028911346/posts/3ff7wsCmKPB");
    }

    @Test
    public void getCompleteUrl2DoubleSlashes() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl2("https://plus.google.com/104378092678028911346",
                "//googleads.g.doubleclick.net"), "https://googleads.g.doubleclick.net");
    }

    @Test
    public void getCompleteUrl2DoubleDot() {
        HtmlParser htmlParser = new HtmlParser();
        Assert.assertEquals(htmlParser.getCompleteUrl2("https://plus.google.com/salam/",
                "../baghali"), "https://plus.google.com/baghali");
    }
}
