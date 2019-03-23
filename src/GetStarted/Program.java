package GetStarted;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.exception.ResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class Program
{
        /*
            Example Gremlin queries to perform the following:
            - add vertices and edges
            - query with filters, projections, 
            - traversals, including loops
            - update annd delete vertices and edges
        */
        static final String gremlinQueries[] = new String[] {
            //"g.V().drop()",
            "g.addV('person').property('id', 'thomas').property('firstName', 'Thomas').property('age', 44)",
            "g.addV('person').property('id', 'mary').property('firstName', 'Mary').property('lastName', 'Andersen').property('age', 39)",
            "g.addV('person').property('id', 'ben').property('firstName', 'Ben').property('lastName', 'Miller')",
            "g.addV('person').property('id', 'robin').property('firstName', 'Robin').property('lastName', 'Wakefield')",
            "g.V('thomas').addE('knows').to(g.V('mary'))",
            "g.V('thomas').addE('knows').to(g.V('ben'))",
            "g.V('ben').addE('knows').to(g.V('robin'))",
            "g.V('thomas').property('age', 44)",
            "g.V().count()",
            "g.V().hasLabel('person').has('age', gt(40))",
            "g.V().hasLabel('person').order().by('firstName', decr)",
            "g.V('thomas').outE('knows').inV().hasLabel('person')",
            "g.V('thomas').outE('knows').inV().hasLabel('person').outE('knows').inV().hasLabel('person')",
            "g.V('thomas').repeat(out()).until(has('id', 'robin')).path()",
            "g.V('thomas').outE('knows').where(inV().has('id', 'mary')).drop()",
            "g.V('thomas').drop()" };


    public static void main( String[] args ) throws ExecutionException, InterruptedException {


        /**
         * There typically needs to be only one Cluster instance in an application.
         */
        Cluster cluster;

        /**
         * Use the Cluster instance to construct different Client instances (e.g. one for sessionless communication
         * and one or more sessions). A sessionless Client should be thread-safe and typically no more than one is
         * needed unless there is some need to divide connection pools across multiple Client instances. In this case
         * there is just a single sessionless Client instance used for the entire App.
         */
        Client client;

        try {
            // Attempt to create the connection objects
            cluster = Cluster.build(new File("src/remote.yaml")).create();
            client = cluster.connect();
        } catch (FileNotFoundException e) {
            // Handle file errors.
            System.out.println("Couldn't find the configuration file.");
            e.printStackTrace();
            return;
        }

        // After connection is successful, run all the queries against the server.
        for (String query : gremlinQueries) {
            System.out.println("\nSubmitting this Gremlin query: " + query);

            // Submitting remote query to the server.
            ResultSet results = client.submit(query);

            CompletableFuture<List<Result>> completableFutureResults;
            CompletableFuture<Map<String, Object>> completableFutureStatusAttributes;
            List<Result> resultList;
            Map<String, Object> statusAttributes;

            try{
                completableFutureResults = results.all();
                completableFutureStatusAttributes = results.statusAttributes();
                resultList = completableFutureResults.get();
                statusAttributes = completableFutureStatusAttributes.get();            
            }catch(Exception e){
                ResponseException re = (ResponseException) e.getCause();
                System.out.println(Arrays.toString(re.getStatusAttributes().get().keySet().toArray()));
                System.out.println("Status code: " + re.getStatusAttributes().get().get("x-ms-status-code")); 
                System.out.println("Substatus code: " + re.getStatusAttributes().get().get("x-ms-substatus-code")); 
                System.out.println("Request charge: " + re.getStatusAttributes().get().get("x-ms-request-charge"));
                System.out.println("ActivityId: " + re.getStatusAttributes().get().get("x-ms-activity-id"));
                throw(e);
            }

            for (Result result : resultList) {
                System.out.println("\nQuery result:");
                System.out.println(result.toString());
            }

            System.out.println("Status: " + statusAttributes.get("x-ms-status-code").toString());
            System.out.println("Total charge: " + statusAttributes.get("x-ms-total-request-charge").toString());
        }

        System.out.println("Demo complete!\n Press Enter key to continue...");
        try{
            System.in.read();
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        System.exit(0);
    }
}
