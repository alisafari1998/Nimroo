package ir.sahab.nimroo.searchapi;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.SearchUIConnector;
import org.spark_project.jetty.server.Request;
import org.spark_project.jetty.server.Server;
import org.spark_project.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@JsonRpcService
public class JsonRpcSearchService {
    SearchUIConnector searchUIConnector;

    public JsonRpcSearchService(SearchUIConnector searchUIConnector) {
        this.searchUIConnector = searchUIConnector;
    }

    @JsonRpcMethod
    public String ping() {
        System.out.println("ping called");
        return "pong";
    }

    @JsonRpcMethod
    public Set<Map.Entry<String, Double>> normalSearch(
            @JsonRpcParam("text")final String text,
            @JsonRpcParam("safety")final boolean safety,
            @JsonRpcParam("pageRank")final boolean pageRank) {
        try {
            return searchUIConnector.simpleSearch(text, Config.elasticsearchIndexName, safety, pageRank).entrySet();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    @JsonRpcMethod
    public Set<Map.Entry<String, Double>> advanceSearch(
            @JsonRpcParam("must")final ArrayList<String> must,
            @JsonRpcParam("must_not")final ArrayList<String> mustNot,
            @JsonRpcParam("should")final ArrayList<String> should,
            @JsonRpcParam("safety")final boolean safety,
            @JsonRpcParam("pageRank")final boolean pageRank) {
        try {
            return searchUIConnector.advancedSearch(must, mustNot, should,
                    Config.elasticsearchIndexName, safety, pageRank).entrySet();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

}

