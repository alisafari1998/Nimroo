package ir.sahab.nimroo.monitor;

import org.apache.hadoop.hbase.shaded.org.apache.commons.io.input.Tailer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Monitor {

    NimrooTailer tailer;

    public static void main(String [] args) throws IOException, InterruptedException {
        Monitor monitor = new Monitor();
        HashMap<Pattern, Consumer> handles = new HashMap<>();

        handles.put(Pattern.compile("\\[MONITOR\\] value:"), metric->{
            System.err.println("Fucking works: " + metric);
        });

        monitor.tailer = new NimrooTailer(handles);
        Tailer tailer = Tailer.create(new File("/home/ali/tmp/log"), monitor.tailer, 50);
        while (true) {
            Thread.sleep(1000);
        }
    }

    public Monitor() throws IOException {

    }

    public void start() throws IOException {

    }

}

// server:filePath:regex