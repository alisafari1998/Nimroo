package ir.sahab.nimroo.view;

import ir.sahab.nimroo.model.ElasticClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ElasticsearchUI implements Runnable {
  private Scanner scanner;
  private ElasticClient elasticClient;

  @Override
  public void run() {
      elasticClient = new ElasticClient();
    scanner = new Scanner(System.in);
    while (true) {
      System.out.println("Write \"search\" to start search.\n");
      String input = scanner.next().toLowerCase();
      switch (input) {
        case "search":
            try {
                search();
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        default:
          System.out.println("input is not valid.\nplease try again.\n");
          break;
      }
    }
  }

  private void search() throws IOException {
    ArrayList<String> must = new ArrayList<>();
    ArrayList<String> mustNot = new ArrayList<>();
    ArrayList<String> should = new ArrayList<>();
    while (true) {
      System.out.println(
          "Write \"must\" to add a phrase you absolutely want it to be in the page.\n"
              + "write \"mustnot\" to add a phrase you don't want to see in the page.\n"
              + "write \"should\" to add a phrase you prefer to see in the page.\n"
              + "write \"done\" to get 10 best result.\n");
      String input = scanner.next().toLowerCase();
      switch (input) {
        case "must":
          System.out.println("Enter your phrase:\n");
          must.add(scanner.next());
          break;
        case "mustnot":
          System.out.println("Enter your phrase:\n");
          mustNot.add(scanner.next());
          break;
        case "should":
          System.out.println("Enter your phrase:\n");
          should.add(scanner.next());
          break;
        case "done":
            elasticClient.searchInElasticForWebPage(must,mustNot,should,"test11");
            return;
        default:
          System.out.println("input is not valid.\nplease try again.\n");
          break;
      }
    }
  }
}
