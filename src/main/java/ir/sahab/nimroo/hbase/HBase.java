package ir.sahab.nimroo.hbase;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import com.google.protobuf.InvalidProtocolBufferException;
import ir.sahab.nimroo.kafka.KafkaHtmlConsumer;
import ir.sahab.nimroo.model.Language;
import ir.sahab.nimroo.model.PageData;
import ir.sahab.nimroo.serialization.LinkArraySerializer;
import ir.sahab.nimroo.serialization.PageDataSerializer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
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

  private HBase() {
    String appConfigPath = "app.properties";
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(appConfigPath)) {
      properties.load(fis);
      coreSitePath = properties.getProperty("core.site.path");
      hbaseSitePath = properties.getProperty("hbase.site.path");
      Language.getInstance().init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    executorService =
        new ThreadPoolExecutor(500, 500, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(600));
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
    //    try {
    //      HTable table = new HTable(config, "nimroo");
    //      Get get =
    //          new Get(toBytes(DigestUtils.md5Hex(link))).addColumn(toBytes("links"),
    // toBytes("link"));
    //      if (table.get(get).isEmpty()) {
    //        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
    //        p.addColumn(toBytes(linksFamily), toBytes("link"), toBytes(0));
    //        table.put(p);
    //        return false;
    //      }
    //      return true;
    //    } catch (IOException e) {
    //      logger.warn("some exception happen in duplicateUrl method!" + e);
    //      return false;
    //    }
    return isDuplicateUrl(link, defTable);
  }

  private boolean isDuplicateUrl(String link, Table table) {
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(link)))
              .addColumn(toBytes(linksFamily), toBytes("link"));
      if (table.get(get).isEmpty()) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
        p.addColumn(toBytes(linksFamily), toBytes("link"), toBytes(0));
        table.put(p);
        return false;
      }
      return true;
    } catch (IOException e) {
      logger.warn("some exception happen in duplicateUrl method!" + e);
      return false;
    }
  }

  public void storeFromKafka() {
    Table table;
    try {
      Connection connection = ConnectionFactory.createConnection(config);
      table = connection.getTable(TableName.valueOf(tableName));
    } catch (IOException e) {
      logger.error("can not get connection from HBase!", e);
      return;
    }

    while (true) {
      ArrayList<byte[]> pageDatas = kafkaHtmlConsumer.get();
      for (byte[] bytes : pageDatas) {
        PageData pageData = null;
        try {
          pageData = PageDataSerializer.getInstance().deserialize(bytes);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (!Language.getInstance().detector(pageData.getText())) continue;
        PageData finalPageData = pageData;
        executorService.submit(
            () -> {
              addToPageData(finalPageData.getUrl(), bytes, table);
              addToPageRank(finalPageData, table);
              isDuplicateUrl(finalPageData.getUrl(), table);
            });
      }
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException ignored) {
      }
    }
  }

  private void addToPageData(String link, byte[] pageData, Table table) {
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(link)))
              .addColumn(toBytes(pageDataFamily), toBytes("pageData"));
      if (table.get(get).isEmpty()) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(link)));
        p.addColumn(toBytes(pageDataFamily), toBytes("myPageData"), pageData);
        table.put(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in duplicateUrl method!" + e);
    }
  }

  private void addToPageRank(PageData pageData, Table table) {
    String myUrl = pageData.getUrl();
    byte[] myLinks = LinkArraySerializer.getInstance().serialize(pageData.getLinks());
    double myPageRank = 1.000;
    try {
      Get get =
          new Get(toBytes(DigestUtils.md5Hex(myUrl)))
              .addColumn(toBytes(pageRankFamily), toBytes("url"));
      if (table.get(get).isEmpty()) {
        Put p = new Put(toBytes(DigestUtils.md5Hex(myUrl)));
        p.addColumn(toBytes(pageRankFamily), toBytes("myUrl"), toBytes(myUrl));
        p.addColumn(toBytes(pageRankFamily), toBytes("myLinks"), myLinks);
        p.addColumn(toBytes(pageRankFamily), toBytes("myPageRank"), toBytes(myPageRank));
        table.put(p);
      }
    } catch (IOException e) {
      logger.warn("some exception happen in duplicateUrl method!" + e);
    }
  }
}
