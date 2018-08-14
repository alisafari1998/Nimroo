package ir.sahab.nimroo.twitter;

import twitter4j.*;

import java.util.ArrayList;

public class Tweet {

    TwitterStream ts = TwitterStreamFactory.getSingleton();
    NimrooStatusListener statusListener = new NimrooStatusListener();

    public Tweet() {
        ts.addListener(statusListener);
    }

    public void start(ArrayList<String> tags) {
        FilterQuery fq = new FilterQuery();
        String [] tmp = new String[tags.size()];
        tmp = tags.toArray(tmp);
        fq.track(tmp);
        fq.language("en");
        ts.filter(fq);
    }

    public void stop() {
        ts.shutdown();
    }

}
