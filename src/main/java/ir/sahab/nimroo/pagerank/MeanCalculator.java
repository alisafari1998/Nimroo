package ir.sahab.nimroo.pagerank;

import ir.sahab.nimroo.Config;
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
	private static Logger logger = Logger.getLogger(MeanCalculator.class);

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

		System.out.println(pageRankSum);
		System.out.println(num);
		System.out.println(pageRankSum / num);

/*		Job job = null;
		System.out.println("start configuring job");
		try {
			job = Job.getInstance(hBaseConfiguration);
			job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "nimroo");
			job.setOutputFormatClass(TableOutputFormat.class);
			System.out.println("Job configured");
		} catch (IOException e) {
			System.out.println("Job not configured.\t" + e);
		}

		System.out.println("creating hBasePuts rdd...");
		JavaPairRDD<ImmutableBytesWritable, Put> hBasePuts = hBaseRDD.mapToPair(pairRow -> {
			Result result = pairRow._2;

			byte[] bytes = result.getValue(Bytes.toBytes("PageRankFamily"), Bytes.toBytes("myPageRank"));
			double myPageRank = Bytes.toDouble(bytes);

			Put put = new Put(pairRow._1.get());
			put.addColumn(Bytes.toBytes("pageRank"), Bytes.toBytes("myPageRank"), Bytes.toBytes(myPageRank));

			return new Tuple2<>(new ImmutableBytesWritable(), put);
		});
		System.out.println("hBasePuts rdd created.");

		System.out.println("saving data in HBase...");
		hBasePuts.saveAsNewAPIHadoopDataset(job.getConfiguration());
		System.out.println("data saved.");*/
	}
}