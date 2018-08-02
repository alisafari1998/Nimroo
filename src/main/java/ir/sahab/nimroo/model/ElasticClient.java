package ir.sahab.nimroo.model;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticClient {
  RestHighLevelClient client;
  BulkRequest request;

  public ElasticClient() {
    client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    request = new BulkRequest();
  }

  public void addToBulkOfElastic(
      String url,
      String title,
      String text,
      String anchor,
      String description,
      String keywords,
      String index)
      throws IOException {
    XContentBuilder builder =
        jsonBuilder()
            .startObject()
            .field("url", url)
            .field("title", title)
            .field("text", text)
            .field("anchor", anchor)
            .field("description", description)
            .field("keywords", keywords)
            .endObject();
    request.add(new IndexRequest(index, "_doc").source(builder));
  }

  public void addBulkToElastic() throws IOException {
    if (request.numberOfActions() > 0) {
      client.bulk(request);
      request = new BulkRequest();
    }
  }

  public void searchInElasticForWebPage(
      ArrayList<String> mustFind,
      ArrayList<String> mustNotFind,
      ArrayList<String> shouldFind,
      String index)
      throws IOException {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    for (String phrase : mustFind) {
      boolQuery.must(
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "anchor", "description", "keywords")
              .type(MultiMatchQueryBuilder.Type.PHRASE));
    }
    for (String phrase : mustNotFind) {
      boolQuery.mustNot(
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "anchor", "description", "keywords")
              .type(MultiMatchQueryBuilder.Type.PHRASE));
    }
    for (String phrase : shouldFind) {
      boolQuery.should(
          QueryBuilders.multiMatchQuery(
                  phrase, "text", "title", "anchor", "description", "keywords")
              .type(MultiMatchQueryBuilder.Type.PHRASE));
    }
    searchSourceBuilder.query(boolQuery);
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest);
    SearchHits hits = searchResponse.getHits();
    SearchHit[] searchHits = hits.getHits();
    for (SearchHit hit : searchHits) {
      System.out.println(hit.getScore());
      System.out.println(hit.getSourceAsString());
    }
  }
}
