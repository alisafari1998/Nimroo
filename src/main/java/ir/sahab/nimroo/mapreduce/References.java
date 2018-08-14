package ir.sahab.nimroo.mapreduce;

import com.github.os72.protobuf351.InvalidProtocolBufferException;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class References {

  private static References ourInstance = new References();

  public static References getInstance() {
    return ourInstance;
  }
  private References() {
  }

  public class RefMapper extends TableMapper<Text, LongWritable> {
    long numRecords = 0;
    @Override
    public void map(ImmutableBytesWritable row, Result values, Context context)
        throws InvalidProtocolBufferException {
      LongWritable one = new LongWritable(1);
      ArrayList<Link> myLinks = LinkArraySerializer.getInstance().deserialize(values.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myLinks")));
      for (Link link : myLinks){
          try {
            context.write(new Text(link.getLink()), one);
          } catch (IOException | InterruptedException ignored) {
          }
        }
      numRecords++;
      if ((numRecords % 10000) == 0) {
        context.setStatus("mapper processed " + numRecords + " records so far");
      }
    }
  }

  public class RefReducer extends TableReducer<Text, LongWritable, Text> {

    @Override
    public void reduce(Text key, Iterable<LongWritable> values, Context context) {
      long sum = 0;
      for (LongWritable val : values) {
        sum += val.get();
      }
      Put put = new Put(Bytes.toBytes(DigestUtils.md5Hex(key.toString())));
      put.addColumn(Bytes.toBytes("pageRank"), Bytes.toBytes("totalRef"), Bytes.toBytes(sum));
      put.addColumn(Bytes.toBytes("pageRank"), Bytes.toBytes("url"), key.getBytes());
      try {
        context.write(key, put);
      } catch (IOException | InterruptedException ignored) {
      }
    }
  }

  public class RefCombiner extends Reducer<Text, LongWritable, Text, LongWritable> {

    public void reduce(Text key, Iterable<LongWritable> values, Context context) {
      long sum = 0;
      for (LongWritable val : values) {
        sum += val.get();
      }
      try {
        context.write(key, new LongWritable(sum));
      } catch (IOException | InterruptedException ignored) {
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration config = HBaseConfiguration.create();
    config.addResource(new Path("/home/hadoop/hbase-1.2.6.1/conf/hbase-site.xml"));
    config.addResource(new Path("/home/hadoop/hadoop-2.9.1/etc/hadoop/core-site.xml"));
    Job job = Job.getInstance(config, "References");
    job.setJarByClass(References.class);
    job.setCombinerClass(RefCombiner.class);

    Scan scan = new Scan();
    scan.setCaching(500);
    scan.setCacheBlocks(false);
    scan.addColumn(Bytes.toBytes("pageRank"), Bytes.toBytes("myUrl"));
    scan.addColumn(Bytes.toBytes("pageRank"), Bytes.toBytes("myLinks"));
//    scan.setStopRow(Bytes.toBytes(""));

    TableMapReduceUtil.initTableMapperJob(
        "nimroo", scan, RefMapper.class, Text.class, LongWritable.class, job);
    TableMapReduceUtil.initTableReducerJob("dummy", RefReducer.class, job);
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
