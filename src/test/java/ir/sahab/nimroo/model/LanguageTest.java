package ir.sahab.nimroo.model;

import static org.junit.Assert.*;

import ir.sahab.nimroo.parser.HtmlParser;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class LanguageTest {

  @Test
  public void detectorWithValidInput0() {
    try {
      Language.getInstance().init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Assert.assertTrue(Language.getInstance().detector("hello world !"));
  }

  @Test
  public void detectorWithValidInput1() {
    try {
      Language.getInstance().init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Assert.assertFalse(Language.getInstance().detector("Deviner une énigme"));
  }

  @Test
  public void detectorWithValidInput2() {

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
    try {
      Language.getInstance().init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Assert.assertTrue(Language.getInstance().detector(pageData.getText().substring(0,java.lang.Math.min(pageData.getText().length(), 1000))));
  }
}
