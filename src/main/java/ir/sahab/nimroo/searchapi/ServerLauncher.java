package ir.sahab.nimroo.searchapi;

import ir.sahab.nimroo.Config;
import ir.sahab.nimroo.model.SearchUIConnector;
import org.spark_project.jetty.server.Server;

public class ServerLauncher {

    public static void main(String[] args) throws Exception {
        Config.load();

        Server server = new Server(8080);
        SearchUIConnector searchUIConnector = new SearchUIConnector();
        JsonRpcSearchService jsonRpcSearchService = new JsonRpcSearchService(searchUIConnector);
        server.setHandler(new HttpRequestHandler(jsonRpcSearchService));

        server.start();
        server.join();
    }

}
