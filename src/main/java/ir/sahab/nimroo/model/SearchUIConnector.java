package ir.sahab.nimroo.model;

import ir.sahab.nimroo.hbase.HBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchUIConnector {
  private ElasticClient elasticClient;

  public SearchUIConnector()
  {
    elasticClient = new ElasticClient();
    elasticClient.readObsceneWordsForSearch();
  }

  public HashMap<String, Double> simpleSearch(
      String searchText, String index, boolean safety, boolean pageRank) throws IOException {
    elasticClient.setSafeSearch(safety);
    ArrayList<String> ans = elasticClient.simpleSearchInElasticForWebPage(searchText, index);
    return makeHashMap(ans,pageRank);
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
    ArrayList<String> ans = elasticClient.advancedSearchInElasticForWebPage(mustFind,mustNotFind,shouldFind, index);
    return makeHashMap(ans,pageRank);
  }

  private HashMap<String, Double> makeHashMap(ArrayList<String> links,boolean pageRank){
      if (!pageRank) {
          HashMap<String, Double> map = new HashMap<>();
          for (String temp : links) {
              map.put(temp, null);
          }
          return map;
      } else {
          HashMap<String, Double> map = new HashMap<>();
          for (String temp : links) {
              map.put(temp, HBase.getInstance().getPageRank(temp));
          }
          return map;
      }
  }
}
