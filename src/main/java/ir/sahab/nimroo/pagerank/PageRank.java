package ir.sahab.nimroo.pagerank;

import org.apache.spark.api.java.JavaPairRDD;

import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class PageRank {
	private static final double d = 0.85;
//	private static final double dComp = 0.15;

	public JavaPairRDD<String, Tuple2<Double, List<String>>> calcPageRank(JavaPairRDD<String, Tuple2<Double, List<String>>> sourceRankAndLinks) {
		JavaPairRDD<String, Double> sinkRankRDD = sourceRankAndLinks.flatMapToPair(string_doubleListString -> {
			double pr = string_doubleListString._2._1;
			List<String> sinks = string_doubleListString._2._2;
			int out = sinks.size();

			List<Tuple2<String, Double>> sinkRankResults = new ArrayList<>();
			for (String sink: sinks) {
				sinkRankResults.add(new Tuple2<>(sink, pr/out));
			}

			return sinkRankResults.iterator();
		});

		JavaPairRDD<String, List<String>> sourceAndLinks = sourceRankAndLinks.mapToPair(string_doubleListString ->
				new Tuple2<>(string_doubleListString._1, string_doubleListString._2._2));

		JavaPairRDD<String, Double> reducedSinkRankRDD = sinkRankRDD.reduceByKey((a, b) -> a + b);

		JavaPairRDD<String, Double> finalSinkRank = reducedSinkRankRDD.mapToPair(sinkAndSum ->
				new Tuple2<>(sinkAndSum._1, (d * sinkAndSum._2) + (1 - d)));

/*		JavaPairRDD<String, Tuple2<Optional<Double>, List<String>>> finalSourceRankLinks = finalSinkRank.rightOuterJoin(sourceAndLinks);
		return finalSourceRankLinks.mapToPair(newSourceRankLink -> {
			Optional<Double> optionalDouble = newSourceRankLink._2._1;
			if (optionalDouble.isPresent()) {
				return new Tuple2<>(newSourceRankLink._1, new Tuple2<>(optionalDouble.get(), newSourceRankLink._2._2));
			}
			else {
				return new Tuple2<>(newSourceRankLink._1, new Tuple2<>(dComp, newSourceRankLink._2._2));
			}
		});*/
		return finalSinkRank.join(sourceAndLinks);
	}
}