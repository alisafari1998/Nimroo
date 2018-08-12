package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.RSS.SitesView;
import ir.sahab.nimroo.Twitter.Tweet;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TwitterLauncher {
    private static final Logger LOGGER = Logger.getLogger(TwitterLauncher.class);

    public static void main(String []args)  {
        Config.load();
        PropertyConfigurator.configure("log4j.properties");

        Tweet t = new Tweet();
        SitesView sitesView = null;
        try {
            sitesView = new SitesView();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOGGER.error(e);
            System.exit(1);
        }

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

        SitesView finalSitesView = sitesView;
        t.start(finalSitesView.getKeywordsOfLatestNews());

        ses.scheduleAtFixedRate(()-> {
            t.stop();
            LOGGER.info("resetting keywords");
            t.start(finalSitesView.getKeywordsOfLatestNews());
        },1, 1, TimeUnit.MINUTES);
    }
}
