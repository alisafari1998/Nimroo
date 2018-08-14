package ir.sahab.nimroo.crawler.util;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;
import java.io.IOException;
import java.util.List;

/**
 * @author ArminF96
 */
public class Language {

  List<LanguageProfile> languageProfiles;
  static boolean flag = false;
  private static Language ourInstance = new Language();
  private LanguageDetector languageDetector;
  private TextObjectFactory textObjectFactory;
  private double acceptProbability;

  public static Language getInstance() {
    return ourInstance;
  }

  private Language() {
    acceptProbability = 0.75;
  }

  public void init() throws IOException {
    loadLanguages();
    buildDetector();
  }

  private void loadLanguages() throws IOException {
    languageProfiles = new LanguageProfileReader().readAllBuiltIn();
  }

  private void buildDetector() {
    languageDetector =
        LanguageDetectorBuilder.create(NgramExtractors.standard())
            .withProfiles(languageProfiles)
            .build();
    textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    flag = true;
  }

  public boolean detector(String text) {
    if (!flag) {
      System.err.println("error happen in language detector!");
      return false;
    }
//    TextObject textObject = textObjectFactory.forText(text);
//    Optional<LdLocale> lang = languageDetector.detect(textObject);
//    if (lang.isPresent()) {
//      return lang.get().toString().equals("en");
//    }
    double tmp = 1.0;
    text = text.substring(0,java.lang.Math.min(1000,text.length()));
    for (DetectedLanguage detectedLanguage : languageDetector.getProbabilities(text)) {
      if (tmp < acceptProbability) return false;
      if (detectedLanguage.getProbability() > acceptProbability) {
        return detectedLanguage.getLocale().toString().equals("en");
      }
      tmp -= detectedLanguage.getProbability();
    }
    return false;
  }
}
