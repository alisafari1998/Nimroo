package ir.sahab.nimroo.rss;

import java.util.*;

public class KeywordExtractor {
  private HashMap<String, Integer> map;
  private ArrayList<String> stopWords;
  private int numberOfKeywordsToExtract;

  KeywordExtractor(ArrayList<String> stopWords, int numberOfKeywordsToExtract) {
    map = new HashMap<>();
    this.stopWords = stopWords;
    this.numberOfKeywordsToExtract = numberOfKeywordsToExtract;
  }

  public void newHashMap() {
    map = new HashMap<>();
  }

  void addForExtractingKeywords(String text) {
    for (String word : text.split("\\W")) {
      boolean isStopWord = false;
      for (String sw : stopWords) {
        if (sw.equals(word)) {
          isStopWord = true;
          break;
        }
      }
      if (word.isEmpty() || isStopWord) {
        continue;
      }
      if (map.containsKey(word)) {
        map.put(word, map.get(word) + 1);
      } else {
        map.put(word, 1);
      }
    }
  }

  ArrayList<String> getKeywords() {
    map = this.sortByValues(map);
    ArrayList<String> topKeywords = new ArrayList<>();
    int i = 0;
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      topKeywords.add(entry.getKey());
      i++;
      if (i == numberOfKeywordsToExtract) {
        break;
      }
    }
    return topKeywords;
  }

  private HashMap<String, Integer> sortByValues(HashMap<String, Integer> map) {
    List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
    Collections.sort(
        list,
        (Comparator)
            (o1, o2) ->
                ((Comparable) ((Map.Entry) (o1)).getValue())
                    .compareTo(((Map.Entry) (o2)).getValue()));
    HashMap<String, Integer> sortedHashMap = new LinkedHashMap<>();
    for (Iterator<Map.Entry<String, Integer>> it =
            ((LinkedList<Map.Entry<String, Integer>>) list).descendingIterator();
        it.hasNext(); ) {
      Map.Entry<String, Integer> entry = it.next();
      sortedHashMap.put(entry.getKey(), entry.getValue());
    }
    return sortedHashMap;
  }
}
