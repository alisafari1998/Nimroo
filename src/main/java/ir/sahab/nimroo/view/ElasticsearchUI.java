package ir.sahab.nimroo.view;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.ElasticClient;
import ir.sahab.nimroo.model.SearchUIConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ElasticsearchUI {
  private Scanner scanner;
  private ElasticClient elasticClient;
  private SearchUIConnector searchUIConnector;

  public static void main(String[] args) {
    Config.load();
    ElasticsearchUI elasticsearchUI = new ElasticsearchUI();
    elasticsearchUI.start();
  }

  public void start() {
    elasticClient = new ElasticClient();
    elasticClient.readObsceneWordsForSearch();
    searchUIConnector = new SearchUIConnector();
    scanner = new Scanner(System.in);
    while (true) {
      if (elasticClient.getSafeSearch()) {
        System.out.println(
            "write \"search\" to start search.\n"
                + "write \"advancedSearch\" to start advancedSearch.\n"
                + "write \"safeOff\" to turn off safe search.\n");
      } else {
        System.out.println(
            "write \"search\" to start search.\n"
                + "write \"advancedSearch\" to start advancedSearch.\n"
                + "write \"safeOn\" to turn on safe search.\n");
      }
      String input = scanner.next().toLowerCase();
      switch (input) {
        case "advancedsearch":
          try {
            advancedSearch();
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case "search":
          try {
            search();
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case "safeon":
          elasticClient.setSafeSearch(true);
          break;
        case "safeoff":
          elasticClient.setSafeSearch(false);
          break;
        default:
          System.out.println("input is not valid.\nplease try again.\n");
          break;
      }
    }
  }

  private void search() throws IOException {
    System.out.println("Enter your search text:\n");
    scanner.nextLine();
    String searchText = scanner.nextLine();
    HashMap<String, Double> ans =
        searchUIConnector.simpleSearch(
            searchText, Config.elasticsearchIndexName, false,true);
    for (HashMap.Entry<String, Double> temp : ans.entrySet()) {
      System.out.println(temp.getKey() + "     " + temp.getValue());
    }
  }

  private void advancedSearch() throws IOException {
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
          HashMap<String, Double> ans =
              searchUIConnector.advancedSearch(
                  must, mustNot, should, Config.elasticsearchIndexName, false,false);
          for (HashMap.Entry<String, Double> temp : ans.entrySet()) {
            System.out.println(temp.getKey() + "     " + temp.getValue());
          }
          return;
        default:
          System.out.println("input is not valid.\nplease try again.\n");
          break;
      }
    }
  }
}
