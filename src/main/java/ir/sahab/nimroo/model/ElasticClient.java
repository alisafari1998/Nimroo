package ir.sahab.nimroo.model;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
public class ElasticClient {
  RestHighLevelClient client;

  public ElasticClient() {
    client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
  }

  public void postToElastic(String title, String text, String anchor, String index, String description, String keywords) throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder.field("title", title);
      builder.field("text", text);
      builder.field("anchor", anchor);
      builder.field("description" , description);
      builder.field("keywords" , keywords);
    }
    builder.endObject();
    IndexRequest indexRequest = new IndexRequest(index, "_doc");
    indexRequest.source(builder);
    client.index(indexRequest);
  }
}
