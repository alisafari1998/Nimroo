package ir.sahab.nimroo.view;

import ir.sahab.nimroo.model.Link;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.pagerank.PageRank;
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
	public static void main(String[] args) {
		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("page rank test");
//      sparkConf.setMaster("spark://94.23.203.156:7077");
		JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
		PageRank pageRank = new PageRank();

//		makeBigLoopFile();
		Configuration hBaseConfiguration = HBaseConfiguration.create();
		hBaseConfiguration.set(TableInputFormat.INPUT_TABLE, "nimroo");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMN_FAMILY, "pageRank");
		hBaseConfiguration.set(TableInputFormat.SCAN_COLUMNS, "myUrl myLinks myPageRank");

		JavaPairRDD<ImmutableBytesWritable, Result> boolshit = javaSparkContext
				.newAPIHadoopRDD(hBaseConfiguration, TableInputFormat.class
						, ImmutableBytesWritable.class, Result.class);

		JavaPairRDD<String, Tuple2<Double, List<String>>> sourceRankSinks = boolshit.mapToPair(pairRow-> {
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

			//System.out.println(myUrl + " " + myPageRank + Arrays.toString(sinks.toArray()));

			return new Tuple2<>(myUrl, new Tuple2<>(myPageRank, sinks));
		});


		/*JavaRDD<String> pageRankInfo = javaSparkContext.textFile("pageRankTestWithBigLoop.txt");
		JavaPairRDD<String, Tuple2<Double, List<String>>> sourceRankSinks = pageRankInfo.mapToPair(pageRankInfoLine -> {
			String[] strings = pageRankInfoLine.split(" ");

			String source = strings[0];
			double rank = Double.parseDouble(strings[1]);
			List<String> sinks = new ArrayList<>(Arrays.asList(strings).subList(2, strings.length));

			return new Tuple2<>(source, new Tuple2<>(rank, sinks));
		});*/

		for (int i = 0; i < 40; i++) {
			sourceRankSinks = pageRank.calcPageRank(sourceRankSinks);
		}

		sourceRankSinks.saveAsTextFile("tmp/myPageRankOutput");
	}

/*	private static void makeBigLoopFile() {
		int n = 1000000;
		Random random = new Random();

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < n; i++) {
			stringBuilder.append("node").append(i).append(" 1 node").append(i + 1);
			for (int j = 0; j < 20; j++) {
				stringBuilder.append(" node").append(random.nextInt(n+1));
			}
			stringBuilder.append("\n");
		}
		stringBuilder.append("node").append(n).append(" 1 node").append(0);

		File file = new File("pageRankTestWithBigLoop.txt");
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(stringBuilder.toString());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}