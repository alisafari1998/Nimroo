package ir.sahab.nimroo.model;

import ir.sahab.nimroo.hbase.HBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchUIConnector {
  private ElasticClient elasticClient;

  public SearchUIConnector() {
    elasticClient = new ElasticClient();
    elasticClient.readObsceneWordsForSearch();
  }

  public HashMap<String, Double> simpleSearch(
      String searchText, String index, boolean safety, boolean pageRank) throws IOException {
    elasticClient.setSafeSearch(safety);
    HashMap<String, Double> ans =
        elasticClient.simpleSearchInElasticForWebPage(searchText, index, pageRank);
    return makeHashMap(ans, pageRank);
  }

  public HashMap<String, Double> advancedSearch(
      ArrayList<String> mustFind,
      ArrayList<String> mustNotFind,
      ArrayList<String> shouldFind,
      String index,
      boolean safety,
      boolean pageRank)
      throws IOException {
    elasticClient.setSafeSearch(safety);
    HashMap<String, Double> ans =
        elasticClient.advancedSearchInElasticForWebPage(
            mustFind, mustNotFind, shouldFind, index, pageRank);
    return makeHashMap(ans, pageRank);
  }

  private HashMap<String, Double> makeHashMap(HashMap<String, Double> links, boolean pageRank) {
    if (!pageRank) {
      HashMap<String, Double> answer = new HashMap<>();
      for (HashMap.Entry<String, Double> temp : links.entrySet()) {
        answer.put(temp.getKey(), null);
      }
      return answer;
    } else {
      HashMap<String, Double> mapForScoring = new HashMap<>();
      HashMap<String, Double> answer = new HashMap<>();
      for (HashMap.Entry<String, Double> temp : links.entrySet()) {
        double pageRangTemp = HBase.getInstance().getPageRank(temp.getKey());
        if (pageRangTemp == 1d){
          pageRangTemp = 0d;
        }
        mapForScoring.put(temp.getKey(), pageRangTemp * temp.getValue());
      }
      for (int i = 0; i < 10; i++) {
        double maxFinalScore = 0;
        String linkOfMaxScore = null;
        for (HashMap.Entry<String, Double> temp : mapForScoring.entrySet()) {
          if (maxFinalScore < temp.getValue()) {
            maxFinalScore = temp.getValue();
            linkOfMaxScore = temp.getKey();
          }
        }
        double pageRangTemp = HBase.getInstance().getPageRank(linkOfMaxScore);
        if (pageRangTemp == 1d){
          pageRangTemp = 0d;
        }
        answer.put(linkOfMaxScore, pageRangTemp);
        mapForScoring.remove(linkOfMaxScore);
      }
      return answer;
    }
  }
}
