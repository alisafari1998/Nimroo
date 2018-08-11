package ir.sahab.nimroo.mapreduce;

import ir.sahab.nimroo.hbase.HBase;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.codec.digest.DigestUtils;
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

public class RepetitiveAnchors {

  private static RepetitiveAnchors ourInstance = new RepetitiveAnchors();
  public static RepetitiveAnchors getInstance() {
    return ourInstance;
  }
  private static ArrayList<String> uselessAnchors;

  private RepetitiveAnchors() {
    uselessAnchors = new ArrayList<>();
    uselessAnchors.add("link");
    uselessAnchors.add("this");
    uselessAnchors.add("site");
    uselessAnchors.add("click");
    uselessAnchors.add("ref");
    uselessAnchors.add("here");
  }

  static class Mapper extends TableMapper<Text, Text> {

    private int numRecords = 0;

    @Override
    public void map(ImmutableBytesWritable row, Result values, Context context) throws IOException {
      PageData pageData = PageDataSerializer.getInstance().deserialize(values.value());
      for(Link link : pageData.getLinks())if(!uselessAnchors.contains(link.getAnchor())){
        try {
          context.write(new Text(DigestUtils.md5Hex(link.getLink())), new Text(link.getAnchor()));
        } catch (InterruptedException e) {
          throw new IOException(e);
        }
      }
      numRecords++;
      if ((numRecords % 10000) == 0) {
        context.setStatus("mapper processed " + numRecords + " records so far");
      }
    }
  }

  public static class Reducer extends TableReducer<Text, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      StringBuilder test = new StringBuilder();
      for (Text val : values) {
        test.append(val);
        test.append(" ");
      }
      Put put = new Put(key.getBytes());
      put.addColumn(Bytes.toBytes("test"), Bytes.toBytes("anchors"), Bytes.toBytes(test.toString()));
      context.write(key, put);
    }
  }

  public static void main(String[] args) throws Exception {
    Job job = new Job(HBase.getInstance().getConfig(), "Repetitive Anchors");
    job.setJarByClass(RepetitiveAnchors.class);


    Scan scan = new Scan();
    scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    scan.setCacheBlocks(false);
    scan.setStopRow(Bytes.toBytes("00000ef7231ffc5c50df"));
    scan.addColumn(Bytes.toBytes("pageData"), Bytes.toBytes("pageData"));
    scan.addColumn(Bytes.toBytes("pageData"), Bytes.toBytes("myPageData"));

    TableMapReduceUtil
        .initTableMapperJob("nimroo", scan, RepetitiveAnchors.Mapper.class, Text.class, Text.class, job);
    TableMapReduceUtil.initTableReducerJob("result", RepetitiveAnchors.Reducer.class, job);
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }

}
