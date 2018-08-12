package ir.sahab.nimroo.Twitter;

import ir.sahab.nimroo.kafka.KafkaTwitterProducer;
import ir.sahab.nimroo.serialization.TweetProto;
import org.apache.log4j.Logger;
import twitter4j.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NimrooStatusListener implements StatusListener {
    Logger logger = Logger.getLogger(NimrooStatusListener.class);
    KafkaTwitterProducer kafkaTwitterProducer = new KafkaTwitterProducer();

    @Override
    public void onStatus(Status status) {
        if (status.getURLEntities().length < 1) {
            return;
        }

        TweetProto.Tweet.Builder tpb = TweetProto.Tweet.newBuilder();
        tpb.setText(status.getText());
        tpb.addAllUrls(Arrays.stream(status.getURLEntities()).map(URLEntity::getExpandedURL).collect(Collectors.toList()));
        tpb.addAllHashTags(Arrays.stream(status.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.toList()));
        tpb.setPubDate(status.getCreatedAt().toString());
        tpb.setTweetId(String.valueOf(status.getId()));
        tpb.setUserName(status.getUser().getScreenName());
        logger.info(tpb.toString());
        kafkaTwitterProducer.send("twitter", null, tpb.build().toByteArray());
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.err.println("onTrackLimitationNotice\t numberOfLimitedStatuse: " + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {

    }

    @Override
    public void onStallWarning(StallWarning warning) {
        System.err.println("onStallWarning\t warning: " + warning);
    }

    @Override
    public void onException(Exception ex) {
        logger.error(ex);
    }
}
