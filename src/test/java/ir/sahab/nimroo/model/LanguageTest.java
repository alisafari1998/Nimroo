package ir.sahab.nimroo.model;

import static org.junit.Assert.*;

import java.io.IOException;
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
}
