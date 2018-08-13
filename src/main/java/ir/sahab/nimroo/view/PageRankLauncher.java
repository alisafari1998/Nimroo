package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.spark.PageRank;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class PageRankLauncher {
	public static void main(String[] args) {   //todo 32-bit flag
		Config.load();

		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("page rank test");
//      sparkConf.setMaster("spark://94.23.203.156:7077");
		JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
		PageRank pageRank = new PageRank();

		Configuration hBaseConfiguration = HBaseConfiguration.create();
		hBaseConfiguration.set(TableInputFormat.INPUT_TABLE, "nimroo");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMN_FAMILY, "pageRank");
//		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMNS, "myUrl myLinks myPageRank");
		hBaseConfiguration.addResource(Config.hBaseCoreSite);
		hBaseConfiguration.addResource(Config.hBaseSite);

		JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = javaSparkContext
				.newAPIHadoopRDD(hBaseConfiguration, TableInputFormat.class
						, ImmutableBytesWritable.class, Result.class);

		JavaPairRDD<String, Tuple2<Double, List<String>>> sourceRankSinks = hBaseRDD.mapToPair(pairRow-> {
			Result result = pairRow._2;
			byte[] bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myPageRank"));
			double myPageRank = Bytes.toDouble(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myUrl"));
			String myUrl = Bytes.toString(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myLinks"));
			List<Link> links = LinkArraySerializer.getInstance().deserialize(bytes);

			List<String> sinks = new ArrayList<>();
			for (Link link: links) {
				sinks.add(link.getLink());
			}

			return new Tuple2<>(myUrl, new Tuple2<>(myPageRank, sinks));
		});
/*
		for (int i = 0; i < 40; i++) {
			sourceRankSinks = pageRank.calcPageRank(sourceRankSinks);
		}
*/
		sourceRankSinks.saveAsTextFile("tmp/myPageRankOutput");
	}
}