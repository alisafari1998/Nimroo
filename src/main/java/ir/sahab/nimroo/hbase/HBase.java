package ir.sahab.nimroo.hbase;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.kafka.KafkaHtmlConsumer;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HBase {

  private static HBase ourInstance = new HBase();

  public static HBase getInstance() {
    return ourInstance;
  }

  private static final Logger logger = Logger.getLogger(HBase.class);
  private Configuration config;
  private String hbaseSitePath;
  private String coreSitePath;
  private String linksFamily;
  private String pageDataFamily;
  private String pageRankFamily;
  private String tableName;
  private KafkaHtmlConsumer kafkaHtmlConsumer;
  private ExecutorService executorService;
  private Connection defConn;
  private Table defTable;
  private int counter = 0;
  private int total = 0;
  private List<Put> batch;
  static long firstStartTime;

  private HBase() {
    firstStartTime = System.currentTimeMillis();
    String appConfigPath = "app.properties";
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(appConfigPath)) {
      properties.load(fis);
      coreSitePath = properties.getProperty("core.site.path");
      hbaseSitePath = properties.getProperty("hbase.site.path");
    } catch (IOException e) {
      e.printStackTrace();
    }
    executorService =
        new ThreadPoolExecutor(
            15, 15, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000));
    kafkaHtmlConsumer = new KafkaHtmlConsumer();
    PropertyConfigurator.configure("log4j.properties");
    config = HBaseConfiguration.create();
    config.addResource(new Path(hbaseSitePath));
    config.addResource(new Path(coreSitePath));
    tableName = "nimroo";
    linksFamily = "links";
    pageDataFamily = "pageData";
    pageRankFamily = "pageRank";

    try {
      createTable();
    } catch (IOException e) {
      logger.error("possibly we can not get admin from HBase connection!", e);
    }

    try {
      defConn = ConnectionFactory.createConnection(config);
      defTable = defConn.getTable(TableName.valueOf(tableName));
    } catch (IOException e) {
      logger.error("can not get connection from HBase!", e);
      System.exit(499);
    }
  }

  private Admin getAdmin() {
    try {
      return ConnectionFactory.createConnection(config).getAdmin();
    } catch (IOException e) {
      return null;
    }
  }

  public void setHbaseSitePath(String hbaseSitePath) {
    this.hbaseSitePath = hbaseSitePath;
  }

  public void setCoreSitePath(String coreSitePath) {
    this.coreSitePath = coreSitePath;
  }

  public void createTable() throws IOException {
    Admin admin = getAdmin();
    if (admin == null) return;
    if (admin.tableExists(TableName.valueOf(tableName))) {
      return;
    }
    HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
    tableDescriptor.addFamily(new HColumnDescriptor(linksFamily));
    tableDescriptor.addFamily(new HColumnDescriptor(pageDataFamily));
    tableDescriptor.addFamily(new HColumnDescriptor(pageRankFamily));

    byte[][] regions =
        new byte[][] {
          toBytes("0"),
          toBytes("1"),
          toBytes("2"),
          toBytes("3"),
          toBytes("4"),
          toBytes("5"),
          toBytes("6"),
          toBytes("7"),
          toBytes("8"),
          toBytes("9"),
          toBytes("a"),
          toBytes("b"),
          toBytes("c"),
          toBytes("d"),
          toBytes("e"),
          toBytes("f")
        };

    admin.createTable(tableDescriptor, regions);
  }

  public void dropTable(String tableName) throws IOException {
    Admin admin = getAdmin();
    if (admin == null) return;
    if (!admin.tableExists(TableName.valueOf(tableName))) {
      return;
    }
    admin.disableTable(TableName.valueOf(tableName));
    admin.deleteTable(TableName.valueOf(tableName));
  }

  public boolean isDuplicateUrl(String link) {
    return isUrlExist(link, defTable);
  }

  private boolean isUrlExist(String link, Table table) {
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(link)))
              .addColumn(toBytes(linksFamily), toBytes("link"));
      if (!table.exists(get)) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
        p.addColumn(toBytes(linksFamily), toBytes("link"), toBytes(0));
        table.put(p);
        return false;
      }
      return true;
    } catch (IOException e) {
      logger.warn("some exception happen in isUrlExist method!" + e);
      return false;
    }
  }

  public void storeFromKafka() throws InterruptedException {
    Table table;
    try {
      Connection connection = ConnectionFactory.createConnection(config);
      table = connection.getTable(TableName.valueOf(tableName));
    } catch (IOException e) {
      logger.error("can not get connection from HBase!", e);
      return;
    }

    while (true) {
      long startTime = System.currentTimeMillis();
      long startTimeKafka = System.currentTimeMillis();
      ArrayList<byte[]> pageDatas = kafkaHtmlConsumer.get();
      long finishTimeKafka = System.currentTimeMillis();
      logger.info("get from kafka = " + pageDatas.size());
      for (byte[] bytes : pageDatas) {
        PageData pageData = null;
        try {
          pageData = PageDataSerializer.getInstance().deserialize(bytes);
        } catch (com.github.os72.protobuf351.InvalidProtocolBufferException e) {
          continue;
        }
        PageData finalPageData = pageData;
        while (true) {
          try {
            executorService.submit(
                () -> {
                    addPageDataToHBase(finalPageData.getUrl(), bytes, table);
                    isUrlExist(finalPageData.getUrl(), table);
                    addPageRankToHBase(finalPageData, table);
                    counter++;
                    total++;
                });
            break;
          } catch (RejectedExecutionException e) {
            Thread.sleep(40);
          }
        }
      }
      long finishTime = System.currentTimeMillis();
      logger.info("wait for kafka in millisecond = " + (finishTimeKafka - startTimeKafka));
      logger.info("add to HBase = " + counter);
      logger.info("add to HBase per Second. = " + counter / ((finishTime - startTime) / 1000.));
      logger.info(
          "overall time for adding to HBase per Second = "
              + total / ((finishTime - firstStartTime) / 1000.));
      counter = 0;
      try {
        TimeUnit.MILLISECONDS.sleep(5);
      } catch (InterruptedException ignored) {
      }
    }
  }

  private void addPageDataToBatch(String link, byte[] pageData, Table table) {
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(link)))
              .addColumn(toBytes(pageDataFamily), toBytes("pageData"));
      if (!table.exists(get)) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
        p.addColumn(toBytes(pageDataFamily), toBytes("pageData"), pageData);
        batch.add(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in addPageDataToBatch method!" + e);
    }
  }

  private void addPageDataToHBase(String link, byte[] pageData, Table table) {
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(link)))
              .addColumn(toBytes(pageDataFamily), toBytes("pageData"));
      if (!table.exists(get)) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
        p.addColumn(toBytes(pageDataFamily), toBytes("pageData"), pageData);
        table.put(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in addPageDataToHBase method!" + e);
    }
  }

  private void addPageRankToBatch(PageData pageData, Table table) {
    String myUrl = pageData.getUrl();
    byte[] myLinks = LinkArraySerializer.getInstance().serialize(pageData.getLinks());
    double myPageRank = 1.000;
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(myUrl)))
              .addColumn(toBytes(pageRankFamily), toBytes("myUrl"));
      if (!table.exists(get)) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(myUrl)));
        p.addColumn(toBytes(pageRankFamily), toBytes("myUrl"), toBytes(myUrl));
        p.addColumn(toBytes(pageRankFamily), toBytes("myLinks"), myLinks);
        p.addColumn(toBytes(pageRankFamily), toBytes("myPageRank"), toBytes(myPageRank));
        batch.add(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in addPageRankToBatch method!" + e);
    }
  }

  private void addPageRankToHBase(PageData pageData, Table table) {
    String myUrl = pageData.getUrl();
    byte[] myLinks = LinkArraySerializer.getInstance().serialize(pageData.getLinks());
    double myPageRank = 1.000;
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(myUrl)))
              .addColumn(toBytes(pageRankFamily), toBytes("myUrl"));
      if (!table.exists(get)) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(myUrl)));
        p.addColumn(toBytes(pageRankFamily), toBytes("myUrl"), toBytes(myUrl));
        p.addColumn(toBytes(pageRankFamily), toBytes("myLinks"), myLinks);
        p.addColumn(toBytes(pageRankFamily), toBytes("myPageRank"), toBytes(myPageRank));
        table.put(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in addPageRankToHBase method!" + e);
    }
  }

  public Configuration getConfig() {
    return config;
  }
}
