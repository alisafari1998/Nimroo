package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.ElasticClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ElasticsearchUI {
  private Scanner scanner;
  private ElasticClient elasticClient;

  public static void main(String[] args) {
    Config.load();
    ElasticsearchUI elasticsearchUI = new ElasticsearchUI();
    elasticsearchUI.start();
  }

  public void start() {
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
      scanner.nextLine();
      switch (input) {
        case "must":
          System.out.println("Enter your phrase:\n");
          must.add(scanner.nextLine());
          break;
        case "mustnot":
          System.out.println("Enter your phrase:\n");
          mustNot.add(scanner.nextLine());
          break;
        case "should":
          System.out.println("Enter your phrase:\n");
          should.add(scanner.nextLine());
          break;
        case "done":
          ArrayList<String> ans =
              elasticClient.searchInElasticForWebPage(
                  must, mustNot, should, Config.elasticsearchIndexName);
          for (String tmp : ans) {
            System.out.println(tmp);
          }
          return;
        default:
          System.out.println("input is not valid.\nplease try again.\n");
          break;
      }
    }
  }
}