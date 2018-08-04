package ir.sahab.nimroo.model;

import ir.sahab.nimroo.Config;
import org.apache.http.HttpHost;
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

import java.io.IOException;
import java.util.ArrayList;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticClient {
  private RestHighLevelClient client;
  private BulkRequest request;

  public ElasticClient() {
    client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    request = new BulkRequest();
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

  public void addToBulkOfElastic(PageData pageData, String index) throws IOException {
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
            .endObject();
    request.add(new IndexRequest(index, "_doc").source(builder));
  }

  public void addBulkToElastic() throws IOException {
    if (request.numberOfActions() > 0) {
      client.bulk(request);
      request = new BulkRequest();
    }
  }

  public ArrayList<String> searchInElasticForWebPage(
      ArrayList<String> mustFind,
      ArrayList<String> mustNotFind,
      ArrayList<String> shouldFind,
      String index)
      throws IOException {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    for (String phrase : mustFind) {
      MultiMatchQueryBuilder multiMatchQueryBuilder =
          QueryBuilders.multiMatchQuery(phrase, "text", "title", "description", "keywords");
      multiMatchQueryBuilder.field("text", 1);
      multiMatchQueryBuilder.field("title", 2);
      multiMatchQueryBuilder.field("description", 3);
      multiMatchQueryBuilder.field("keywords", 3);
      boolQuery.must(multiMatchQueryBuilder);
    }
    for (String phrase : mustNotFind) {
      boolQuery.mustNot(
          QueryBuilders.multiMatchQuery(phrase, "text", "title", "description", "keywords")
              .type(MultiMatchQueryBuilder.Type.PHRASE));
    }
    for (String phrase : shouldFind) {
      MultiMatchQueryBuilder multiMatchQueryBuilder =
          QueryBuilders.multiMatchQuery(phrase, "text", "title", "description", "keywords");
      multiMatchQueryBuilder.field("text", 1);
      multiMatchQueryBuilder.field("title", 2);
      multiMatchQueryBuilder.field("description", 3);
      multiMatchQueryBuilder.field("keywords", 3);
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
}
