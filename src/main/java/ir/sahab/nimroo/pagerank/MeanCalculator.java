package ir.sahab.nimroo.pagerank;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.view.TestSmallPageRank;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class MeanCalculator {
	private static Configuration hBaseConfiguration = null;
	private static Logger logger = Logger.getLogger(TestSmallPageRank.class);

	public static void main(String[] args) {
		Config.load();
		PropertyConfigurator.configure("log4j.properties");

		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("page rank");
		JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

		hBaseConfiguration = HBaseConfiguration.create();
		hBaseConfiguration.set(TableInputFormat.INPUT_TABLE, "PageRankTable");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMN_FAMILY, "PageRankFamily");
		hBaseConfiguration.addResource(Config.hBaseCoreSite);
		hBaseConfiguration.addResource(Config.hBaseSite);

		JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = javaSparkContext
				.newAPIHadoopRDD(hBaseConfiguration, TableInputFormat.class
						, ImmutableBytesWritable.class, Result.class);

		JavaRDD<Double> pageRanksRDD = hBaseRDD.map(pairRow -> {
			Result result = pairRow._2;

			byte[] bytes = result.getValue(Bytes.toBytes("PageRankFamily"), Bytes.toBytes("myPageRank"));
			double myPageRank = Bytes.toDouble(bytes);

			return myPageRank;
		});

		double pageRankSum = pageRanksRDD.reduce((a, b) -> a + b);
		long num = pageRanksRDD.count();

		System.out.println(pageRankSum / num);
	}
}