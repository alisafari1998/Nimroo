package ir.sahab.nimroo.model;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.crawler.Crawler;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticClient {
  private RestHighLevelClient client;
  private BulkRequest request;
  private ArrayList<String> obsceneWords;
  private Logger logger = Logger.getLogger(ElasticClient.class);
  private boolean safeSearch;

  public ElasticClient() {
    client =
        new RestHighLevelClient(
            RestClient.builder(new HttpHost("94.23.216.137", 9200, "http"))
                .setRequestConfigCallback(
                    requestConfigBuilder ->
                        requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(600000))
                .setMaxRetryTimeoutMillis(600000));
    request = new BulkRequest();
    safeSearch = false;
  }

  public void disableSource() throws IOException {
    GetIndexRequest getIndexRequest = new GetIndexRequest().indices(Config.elasticsearchIndexName);
    if (client.indices().exists(getIndexRequest)) {
      return;
    }
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(Config.elasticsearchIndexName);
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder.startObject("_doc");
      {
        builder.startObject("_source");
        {
          builder.field("enabled", "false");
        }
        builder.endObject();
        builder.startObject("properties");
        {
          builder.startObject("url");
          {
            builder.field("type", "text");
            builder.field("store", "true");
          }
          builder.endObject();
        }
        builder.endObject();
      }
      builder.endObject();
    }
    builder.endObject();
    createIndexRequest.mapping("_doc", builder);
    client.indices().create(createIndexRequest);
  }

  public synchronized void addToBulkOfElastic(PageData pageData, String anchors, String index)
      throws IOException {
    String url = pageData.getUrl();
    String title = pageData.getTitle();
    String text = pageData.getText();
    String description = null;
    String keywords = null;
    if (pageData.getMetas() != null) {
      for (Meta temp : pageData.getMetas()) {
        if (temp.getName().equals("description")) {
          description = temp.getContent();
        } else if (temp.getName().equals("keywords")) {
          keywords = temp.getContent();
        }
      }
    }
    XContentBuilder builder =
        jsonBuilder()
            .startObject()
            .field("url", url)
            .field("title", title)
            .field("text", text)
            .field("description", description)
            .field("keywords", keywords)
            .field("anchors", anchors)
            .endObject();
    request.add(new IndexRequest(index, "_doc").source(builder));
  }

  public synchronized void addBulkToElastic() throws IOException {
    if (request.numberOfActions() > 0) {
      client.bulk(request);
      request = new BulkRequest();
    }
  }

  public void readObsceneWordsForSearch() {
    obsceneWords = new ArrayList<>();
    File file = new File("obscene words");
    Scanner sc = null;
    try {
      sc = new Scanner(file);
    } catch (FileNotFoundException e) {
      logger.error("obscene words file not found", e);
    }
    while (sc.hasNextLine()) {
      obsceneWords.add(sc.nextLine());
    }
  }

  public boolean getSafeSearch() {
    return safeSearch;
  }

  public void setSafeSearch(boolean safety) {
    safeSearch = safety;
  }

  public ArrayList<String> simpleSearchInElasticForWebPage(String searchText, String index)
      throws IOException {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (safeSearch) {
      for (String phrase : obsceneWords) {
        boolQuery.mustNot(
            QueryBuilders.multiMatchQuery(
                    phrase, "text", "title", "description", "keywords", "anchors")
                .type(MultiMatchQueryBuilder.Type.PHRASE));
      }
    }
    MultiMatchQueryBuilder multiMatchQueryBuilder =
        QueryBuilders.multiMatchQuery(
            searchText, "text", "title", "description", "keywords", "anchors");
    multiMatchQueryBuilder.field("text", 5);
    multiMatchQueryBuilder.field("title", 2);
    multiMatchQueryBuilder.field("description", 1);
    multiMatchQueryBuilder.field("keywords", 1);
    multiMatchQueryBuilder.field("anchors", 2);
    boolQuery.must(multiMatchQueryBuilder);
    searchSourceBuilder.query(boolQuery);
    searchSourceBuilder.storedField("url");
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest);
    SearchHits hits = searchResponse.getHits();
    SearchHit[] searchHits = hits.getHits();
    ArrayList<String> answer = new ArrayList<>();
    for (SearchHit hit : searchHits) {
      answer.add(hit.field("url").getValue().toString());
    }
    return answer;
  }

  public ArrayList<String> advancedSearchInElasticForWebPage(
      ArrayList<String> mustFind,
      ArrayList<String> mustNotFind,
      ArrayList<String> shouldFind,
      String index)
      throws IOException {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (safeSearch) {
      for (String phrase : obsceneWords) {
        boolQuery.mustNot(
            QueryBuilders.multiMatchQuery(
                    phrase, "text", "title", "description", "keywords", "anchors")
                .type(MultiMatchQueryBuilder.Type.PHRASE));
      }
    }
    for (String phrase : mustFind) {
      MultiMatchQueryBuilder multiMatchQueryBuilder =
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "description", "keywords", "anchors")
              .type(MultiMatchQueryBuilder.Type.PHRASE);
      multiMatchQueryBuilder.field("text", 5);
      multiMatchQueryBuilder.field("title", 2);
      multiMatchQueryBuilder.field("description", 1);
      multiMatchQueryBuilder.field("keywords", 1);
      multiMatchQueryBuilder.field("anchors", 2);
      boolQuery.must(multiMatchQueryBuilder);
    }
    for (String phrase : mustNotFind) {
      boolQuery.mustNot(
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "description", "keywords", "anchors")
              .type(MultiMatchQueryBuilder.Type.PHRASE));
    }
    for (String phrase : shouldFind) {
      MultiMatchQueryBuilder multiMatchQueryBuilder =
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "description", "keywords", "anchors")
              .type(MultiMatchQueryBuilder.Type.PHRASE);
      multiMatchQueryBuilder.field("text", 5);
      multiMatchQueryBuilder.field("title", 2);
      multiMatchQueryBuilder.field("description", 1);
      multiMatchQueryBuilder.field("keywords", 1);
      multiMatchQueryBuilder.field("anchors", 2);
      boolQuery.should(multiMatchQueryBuilder);
    }
    searchSourceBuilder.query(boolQuery);
    searchSourceBuilder.storedField("url");
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest);
    SearchHits hits = searchResponse.getHits();
    SearchHit[] searchHits = hits.getHits();
    ArrayList<String> answer = new ArrayList<>();
    for (SearchHit hit : searchHits) {
      answer.add(hit.field("url").getValue().toString());
    }
    return answer;
  }

  public void closeClient() throws IOException {
    client.close();
  }
}
