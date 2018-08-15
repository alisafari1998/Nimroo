package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.pagerank.PageRank;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;

public class TestSmallPageRank {
	private static Configuration hBaseConfiguration = null;
	private static Logger logger = Logger.getLogger(TestSmallPageRank.class);

	public static void main(String[] args) {
		Config.load();
		PropertyConfigurator.configure("log4j.properties");
		PageRank pageRank = new PageRank();

		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("page rank test");
//		sparkConf.setMaster("spark://94.23.203.156:7077");
//		sparkConf.setJars(new String[]{"/home/spark/Nimroo/target/"});
		JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

		Scan scan = new Scan();
		scan.setCaching(500);
		scan.setStopRow(Bytes.toBytes("01337b2f88"));
		scan.setCacheBlocks(false);

		System.out.println("Configuring hBaseConfiguration");
		hBaseConfiguration = HBaseConfiguration.create();
		hBaseConfiguration.set(TableInputFormat.INPUT_TABLE, "testPageRankTable");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMN_FAMILY, "pageRankFamily");
		try {
			hBaseConfiguration.set(TableInputFormat.SCAN, convertScanToString(scan));
		} catch (IOException e) {
			System.out.println("hBaseConfiguration set scan failed:\t" + e);
		}
		hBaseConfiguration.addResource(Config.hBaseCoreSite);
		hBaseConfiguration.addResource(Config.hBaseSite);
		System.out.println("hBase configuration done.");

		JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = javaSparkContext
				.newAPIHadoopRDD(hBaseConfiguration, TableInputFormat.class
						, ImmutableBytesWritable.class, Result.class);

		JavaPairRDD<String, Tuple2<Double, List<String>>> sourceRankSinks = hBaseRDD.mapToPair(pairRow-> {
			Result result = pairRow._2;
			byte[] bytes = result.getValue(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myPageRank"));
			double myPageRank = Bytes.toDouble(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myUrl"));
			String myUrl = Bytes.toString(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myLinks"));
			List<Link> links = LinkArraySerializer.getInstance().deserialize(bytes);

			List<String> sinks = new ArrayList<>();
			for (Link link: links) {
				sinks.add(link.getLink());
			}

			return new Tuple2<>(myUrl, new Tuple2<>(myPageRank, sinks));
		});

		sourceRankSinks = sourceRankSinks.filter(pairRow -> !pairRow._1.contains("#"));

		sourceRankSinks = sourceRankSinks.mapToPair(pairRow -> {
			String source = pairRow._1;
			List<String> sinks = pairRow._2._2;

			sinks = new ArrayList<>(new HashSet<>(sinks));

			for (int i = 0; i < sinks.size(); i++) {
				String link = sinks.get(i);
				if (link.contains("#")){
					sinks.remove(i);
					i--;
				}
			}
			if (!sinks.contains(source))
				sinks.add(source);

			return new Tuple2<>(source, new Tuple2<>(pairRow._2._1, sinks));
		});  //adds self_edge and remove # in sinks and remove multiple_edges

		System.out.println("before PageRank calculation");
		for (int i = 0; i < 40; i++) {
			sourceRankSinks = pageRank.calcPageRank(sourceRankSinks);
		}
		System.out.println("after PageRank calculation");

//		sourceRankSinks.saveAsTextFile("tmp/myPageRankOutput");

		Job job = null;
		System.out.println("start configuring job");
		try {
			job = Job.getInstance(hBaseConfiguration);
			job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "PageRankTable2");
			job.setOutputFormatClass(TableOutputFormat.class);
			System.out.println("Job configured");
		} catch (IOException e) {
			System.out.println("Job not configured.\t" + e);
		}

		System.out.println("creating hBasePuts rdd...");
		JavaPairRDD<ImmutableBytesWritable, Put> hBasePuts = sourceRankSinks.mapToPair(sourcePageRankSinks -> {
			String source = sourcePageRankSinks._1;
			double newPageRank = sourcePageRankSinks._2._1;

			Put put = new Put(DigestUtils.md5Hex(source).getBytes()); // TODO: 8/13/18 correct?
			put.addColumn(Bytes.toBytes("PageRankFamily"), Bytes.toBytes("myPageRank"), Bytes.toBytes(newPageRank));

			return new Tuple2<>(new ImmutableBytesWritable(), put);
		});
		System.out.println("hBasePuts rdd created.");

		System.out.println("saving data in HBase...");
		hBasePuts.saveAsNewAPIHadoopDataset(job.getConfiguration());
		System.out.println("data saved.");
	}
}
