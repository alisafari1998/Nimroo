package ir.sahab.nimroo.monitor;

import org.apache.hadoop.hbase.shaded.org.apache.commons.io.input.TailerListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class NimrooTailer implements TailerListener {
    HashMap<Pattern, Consumer> handles;

    public NimrooTailer(HashMap<Pattern, Consumer> handles) {
        this.handles = handles;
    }

    @Override
    public void init(org.apache.hadoop.hbase.shaded.org.apache.commons.io.input.Tailer tailer) {

    }

    @Override
    public void fileNotFound() {

    }

    @Override
    public void fileRotated() {

    }

    @Override
    public void handle(String s) {
        System.out.println(s);
        handles.forEach((key, value) -> {
            if (key.matcher(s).find()) {
                value.accept(s);
            }
        });
    }

    @Override
    public void handle(Exception e) {

    }
}
