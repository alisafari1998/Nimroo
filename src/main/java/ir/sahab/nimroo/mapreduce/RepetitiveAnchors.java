package ir.sahab.nimroo.mapreduce;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import java.io.IOException;
import java.util.HashSet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class RepetitiveAnchors {

  private static RepetitiveAnchors ourInstance = new RepetitiveAnchors();

  public static RepetitiveAnchors getInstance() {
    return ourInstance;
  }

  private static HashSet<String> uselessAnchors;

  private RepetitiveAnchors() {
    uselessAnchors = new HashSet<>();
    uselessAnchors.add("link");
    uselessAnchors.add("this");
    uselessAnchors.add("site");
    uselessAnchors.add("click");
    uselessAnchors.add("ref");
    uselessAnchors.add("here");
    uselessAnchors.add(".");

    uselessAnchors.add("january");
    uselessAnchors.add("february");
    uselessAnchors.add("march");
    uselessAnchors.add("april");
    uselessAnchors.add("may");
    uselessAnchors.add("june");
    uselessAnchors.add("july");
    uselessAnchors.add("august");
    uselessAnchors.add("september");
    uselessAnchors.add("october");
    uselessAnchors.add("november");
    uselessAnchors.add("december");

    uselessAnchors.add("monday");
    uselessAnchors.add("tuesday");
    uselessAnchors.add("wednesday");
    uselessAnchors.add("thursday");
    uselessAnchors.add("friday");
    uselessAnchors.add("saturday");
    uselessAnchors.add("sunday");

    uselessAnchors.add("\\xe2\\x96\\xba");
    uselessAnchors.add("older posts");
    uselessAnchors.add("privacy policy");
    uselessAnchors.add("links to this post");
    uselessAnchors.add("no comments:");
    uselessAnchors.add("comment");
    uselessAnchors.add("comments");
    uselessAnchors.add("skip to sidebar");
    uselessAnchors.add("skip to main");
    uselessAnchors.add("(3)");
    uselessAnchors.add("(2)");
    uselessAnchors.add("(1)");
    uselessAnchors.add("(4)");
    uselessAnchors.add("(5)");
    uselessAnchors.add("(6)");
    uselessAnchors.add("(7)");
    uselessAnchors.add("(8)");
    uselessAnchors.add("(9)");
    uselessAnchors.add("(0)");

    uselessAnchors.add("reply");
    uselessAnchors.add("posts");
    uselessAnchors.add("post");
    uselessAnchors.add("click here");
    uselessAnchors.add("?");
    uselessAnchors.add("??");
    uselessAnchors.add("???");
    uselessAnchors.add("????");
    uselessAnchors.add(
        "\\xe8\\xbe\\xa3\\xe5\\xa6\\xb9\\xe7\\xbe\\x8e\\xe5\\xa5\\xb3\\xe3\\x8a\\xa3 \\xe8\\xbe\\xa3\\xe5\\xa6\\xb9\\xe7\\xbe\\x8e\\xe5\\xa5\\xb3\\xe3\\x8a\\xa3 \\xe8\\xbe\\xa3\\xe5\\xa6\\xb9\\xe7\\xbe\\x8e\\xe5\\xa5\\xb3\\xe3\\x8a\\xa3");
    uselessAnchors.add("leave a comment");
    uselessAnchors.add("about");
    uselessAnchors.add("contact");
    uselessAnchors.add("contact us");
    uselessAnchors.add("home");
    uselessAnchors.add("edit");
    uselessAnchors.add("skip to content");
    uselessAnchors.add("blog");
    uselessAnchors.add("login");
    uselessAnchors.add("log in");
    uselessAnchors.add("log out");
    uselessAnchors.add("logout");
    uselessAnchors.add("about us");
    uselessAnchors.add("google");
    uselessAnchors.add("read more");
    uselessAnchors.add("events");
    uselessAnchors.add("visit");
    uselessAnchors.add("share");
    uselessAnchors.add("report");
    uselessAnchors.add("help");
    uselessAnchors.add("modify");
    uselessAnchors.add("email");
    uselessAnchors.add("terms");
    uselessAnchors.add("download");
    uselessAnchors.add("menu");
    uselessAnchors.add("faq");
    uselessAnchors.add("faqs");
  }

  static class AnchorMapper extends TableMapper<Text, Text> {

    private int numRecords = 0;

    @Override
    public void map(ImmutableBytesWritable row, Result values, Context context) {
      PageData pageData = null;
      try {
        pageData = PageDataSerializer.getInstance().deserialize(values.value());
      } catch (com.github.os72.protobuf351.InvalidProtocolBufferException e) {
        return;
      }
      for (Link link : pageData.getLinks())
        if (!uselessAnchors.contains(link.getAnchor().toLowerCase())) {
          try {
            context.write(new Text(DigestUtils.md5Hex(link.getLink())), new Text(link.getAnchor()));
          } catch (IOException | InterruptedException ignored) {
          }
        }
      numRecords++;
      if ((numRecords % 10000) == 0) {
        context.setStatus("mapper processed " + numRecords + " records so far");
      }
    }
  }

  public static class AnchorReducer extends TableReducer<Text, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) {
      StringBuilder anchor = new StringBuilder();
      for (Text val : values) {
        anchor.append(val);
      }
      Put put = new Put(key.getBytes());
      put.addColumn(
          Bytes.toBytes("pageData"), Bytes.toBytes("anchors"), Bytes.toBytes(anchor.toString()));
      try {
        context.write(key, put);
      } catch (IOException | InterruptedException ignored) {
      }
    }
  }

  public static class AnchorCombiner extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> values, Context context) {
      StringBuilder tmp = new StringBuilder();
      for (Text val : values) {
        tmp.append(val);
        tmp.append(" ");
      }
      try {
        context.write(key, new Text(tmp.toString()));
      } catch (IOException | InterruptedException ignored) {
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration config = HBaseConfiguration.create();
    config.addResource(new Path("/home/hadoop/hbase-1.2.6.1/conf/hbase-site.xml"));
    config.addResource(new Path("/home/hadoop/hadoop-2.9.1/etc/hadoop/core-site.xml"));
    Job job = Job.getInstance(config, "Repetitive Anchors");
    job.setJarByClass(RepetitiveAnchors.class);
    job.setCombinerClass(RepetitiveAnchors.AnchorCombiner.class);

    Scan scan = new Scan();
    scan.setCaching(500);
    scan.setCacheBlocks(false);
    scan.addColumn(Bytes.toBytes("pageData"), Bytes.toBytes("pageData"));
    scan.addColumn(Bytes.toBytes("pageData"), Bytes.toBytes("myPageData"));

    TableMapReduceUtil.initTableMapperJob(
        "nimroo", scan, RepetitiveAnchors.AnchorMapper.class, Text.class, Text.class, job);
    TableMapReduceUtil.initTableReducerJob("nimroo", RepetitiveAnchors.AnchorReducer.class, job);
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
