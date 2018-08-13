package ir.sahab.nimroo.serialization;

import static org.junit.Assert.*;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.parser.HtmlParser;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class LinkArraySerializerTest {

  @Test
  public void serializeAndDeserializeTeste() {
    String htmlStirng =
        "\n"
            + "<!doctype html>\n"
            + "<html>\n"
            + "<head>\n"
            + "    <title>Example Domain</title>\n"
            + "\n"
            + "    <meta charset=\"utf-8\" />\n"
            + "    <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
            + "    <style type=\"text/css\">\n"
            + "    body {\n"
            + "        background-color: #f0f0f2;\n"
            + "        margin: 0;\n"
            + "        padding: 0;\n"
            + "        font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n"
            + "        \n"
            + "    }\n"
            + "    div {\n"
            + "        width: 600px;\n"
            + "        margin: 5em auto;\n"
            + "        padding: 50px;\n"
            + "        background-color: #fff;\n"
            + "        border-radius: 1em;\n"
            + "    }\n"
            + "    a:link, a:visited {\n"
            + "        color: #38488f;\n"
            + "        text-decoration: none;\n"
            + "    }\n"
            + "    @media (max-width: 700px) {\n"
            + "        body {\n"
            + "            background-color: #fff;\n"
            + "        }\n"
            + "        div {\n"
            + "            width: auto;\n"
            + "            margin: 0 auto;\n"
            + "            border-radius: 0;\n"
            + "            padding: 1em;\n"
            + "        }\n"
            + "    }\n"
            + "    </style>    \n"
            + "</head>\n"
            + "\n"
            + "<body>\n"
            + "<div>\n"
            + "    <h1>Example Domain</h1>\n"
            + "    <p>This domain is established to be used for illustrative examples in documents. You may use this\n"
            + "    domain in examples without prior coordination or asking for permission.</p>\n"
            + "    <p><a href=\"http://www.test1.org/domains/example\">More information...0</a></p>\n"
            + "    <p><a href=\"http://www.test2.org/domasns/example\">More information...1</a></p>\n"
            + "    <p><a href=\"http://www.test3.org/domasns/example\">More information...2</a></p>\n"
            + "    <p><a href=\"http://www.test4.org/domasns/example\">More information...3</a></p>\n"
            + "</div>\n"
            + "</body>\n"
            + "</html>\n";

    PageData before = new HtmlParser().parse("url", htmlStirng);
    byte[] bytes = LinkArraySerializer.getInstance().serialize(before.getLinks());
    ArrayList<Link> links=null;
    try {
      links = LinkArraySerializer.getInstance().deserialize(bytes);
    } catch (com.github.os72.protobuf351.InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    Assert.assertNotNull(links);
    Assert.assertEquals(links.get(0).getLink(), "http://www.test1.org/domains/example");
    Assert.assertEquals(links.get(0).getAnchor(), "More information...0");
    Assert.assertEquals(links.get(1).getLink(), "http://www.test4.org/domasns/example");
    Assert.assertEquals(links.get(1).getAnchor(), "More information...3");
    Assert.assertEquals(links.get(2).getLink(), "http://www.test3.org/domasns/example");
    Assert.assertEquals(links.get(2).getAnchor(), "More information...2");
    Assert.assertEquals(links.get(3).getLink(), "http://www.test2.org/domasns/example");
    Assert.assertEquals(links.get(3).getAnchor(), "More information...1");
  }
}