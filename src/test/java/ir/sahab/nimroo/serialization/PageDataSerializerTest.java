package ir.sahab.nimroo.serialization;

import static org.junit.Assert.*;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.parser.HtmlParser;
import ir.sahab.nimroo.serialization.PageDataProto.Link;
import java.io.ByteArrayInputStream;
import org.junit.Assert;
import org.junit.Test;

public class PageDataSerializerTest {

  @Test
  public void serializeAndDeserializeTest() {
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
            + "    <p><a href=\"http://www.iana.org/domains/example\">More information...</a></p>\n"
            + "</div>\n"
            + "</body>\n"
            + "</html>\n";

    PageData before = new HtmlParser().parse("url", htmlStirng);
    byte[] byteArray = PageDataSerializer.getInstance().serialize(before);
    PageData after = null;
    try {
      after = PageDataSerializer.getInstance().deserialize(byteArray);
    } catch (com.github.os72.protobuf351.InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    Assert.assertEquals(before.getUrl(), after.getUrl());
    Assert.assertEquals(before.getText(), after.getText());
    Assert.assertEquals(before.getTitle(), after.getTitle());
    for (ir.sahab.nimroo.model.Link link : before.getLinks()) {
      boolean flag = false;
      for (ir.sahab.nimroo.model.Link link2 : after.getLinks()) {
        flag |=
            (link.getLink().equals(link2.getLink()) & link.getAnchor().equals(link2.getAnchor()));
      }
      Assert.assertTrue(flag);
    }

    for (ir.sahab.nimroo.model.Meta meta : before.getMetas()) {
      boolean flag = false;
      for (ir.sahab.nimroo.model.Meta meta2 : after.getMetas()) {
        flag |=
            (meta.getCharset().equals(meta2.getCharset())
                & meta.getContent().equals(meta2.getContent())
                & meta.getName().equals(meta2.getName())
                & meta.getHttpEquiv().equals(meta2.getHttpEquiv())
                & meta.getScheme().equals(meta2.getScheme()));
      }
      Assert.assertTrue(flag);
    }
  }
}
