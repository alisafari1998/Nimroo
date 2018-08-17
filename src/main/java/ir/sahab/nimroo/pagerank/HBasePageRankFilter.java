package ir.sahab.nimroo.pagerank;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class HBasePageRankFilter {
	private static Configuration hBaseConfiguration;

	public static void main(String[] args) {
		Config.load();

		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("page rank test");
//		sparkConf.setMaster("spark://94.23.203.156:7077");
//		sparkConf.setJars(new String[]{"/home/spark/Nimroo/target/"});
		JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

		hBaseConfiguration = HBaseConfiguration.create();
		hBaseConfiguration.set(TableInputFormat.INPUT_TABLE, "nimroo");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMN_FAMILY, "pageRank");
//		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMNS, "myUrl myLinks myPageRank");
		hBaseConfiguration.addResource(Config.hBaseCoreSite);
		hBaseConfiguration.addResource(Config.hBaseSite);

		JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = javaSparkContext
				.newAPIHadoopRDD(hBaseConfiguration, TableInputFormat.class
						, ImmutableBytesWritable.class, Result.class);

		JavaPairRDD<ImmutableBytesWritable, Result> filteredHBaseRDD = hBaseRDD.filter(pairRow -> {
			Result result = pairRow._2;

			byte[] bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myUrl"));
			String url = Bytes.toString(bytes);
			return !url.contains("#");
		});


//		createTable("testPageRankTable");



		Job newAPIJobConfiguration1 = null;
		try {
			newAPIJobConfiguration1 = Job.getInstance(hBaseConfiguration);
			newAPIJobConfiguration1.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "testPageRankTable");
			newAPIJobConfiguration1.setOutputFormatClass(TableOutputFormat.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JavaPairRDD<ImmutableBytesWritable, Put> hBasePuts = filteredHBaseRDD.mapToPair(pairRow -> {
			ImmutableBytesWritable immutableBytesWritable = pairRow._1;
			Result result = pairRow._2;

			byte[] bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myUrl"));
			String myUrl = Bytes.toString(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myLinks"));
			ArrayList<Link> myLinks = LinkArraySerializer.getInstance().deserialize(bytes);

			bytes = result.getValue(Bytes.toBytes("pageRank"), Bytes.toBytes("myPageRank"));

			for (int i = 0; i < myLinks.size(); i++) {
				String link = myLinks.get(i).getLink();
				if (link.contains("#") || link.equals(myUrl)){
					myLinks.remove(i);
					i--;
				}
			}

			myLinks = new ArrayList<>(new HashSet<>(myLinks));

			Put put = new Put(immutableBytesWritable.get());  // TODO: 8/11/18 correct ?
			put.addColumn(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myUrl"), Bytes.toBytes(myUrl));
			put.addColumn(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myLinks"), LinkArraySerializer.getInstance().serialize(myLinks));
			put.addColumn(Bytes.toBytes("pageRankFamily"), Bytes.toBytes("myPageRank"), bytes);

			return new Tuple2<>(immutableBytesWritable, put);
		});

		hBasePuts.saveAsNewAPIHadoopDataset(newAPIJobConfiguration1.getConfiguration());
	}

	private static void createTable(String nameOfTable) {
		TableName tableName = TableName.valueOf(nameOfTable);
		String pageRankFamily = "pageRankFamily";
		Connection connection = null;
		Admin admin = null;
		try {
			connection = ConnectionFactory.createConnection(hBaseConfiguration);
			admin = connection.getAdmin();
			HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
			hTableDescriptor.addFamily(new HColumnDescriptor(pageRankFamily));
			admin.createTable(hTableDescriptor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
