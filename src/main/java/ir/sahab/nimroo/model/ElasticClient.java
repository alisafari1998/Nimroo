package ir.sahab.nimroo.model;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticClient {
  RestHighLevelClient client;
  BulkRequest request;

  public ElasticClient() {
    client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    request = new BulkRequest();
  }

  public void postToElastic(
      String title, String text, String anchor, String index, String description, String keywords)
      throws IOException {
    XContentBuilder builder =
        jsonBuilder()
            .startObject()
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
}
