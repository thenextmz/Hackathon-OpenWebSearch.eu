package eu.ows.mosaic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/index-info")
public class IndexInfoResource {

    private static Logger LOGGER = LoggerFactory.getLogger(IndexInfoResource.class);

    private Map<String, FSDirectory> indexes;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String indexInfo(@Context UriInfo uriInfo) throws SQLException, IOException {

        LOGGER.info("Received index-info request: {}", uriInfo.getQueryParameters());

        ResourceManager resourceManager = ResourceManager.getInstance();
        indexes = resourceManager.getIndexes();

        JsonArray resultsPerIndex = new JsonArray();

        // Iterate over all indexes and retrieve the document count and languages
        for (String indexName : indexes.keySet()) {
            JsonObject indexObject = new JsonObject();

            // Retrieve the document count
            FSDirectory indexDir = indexes.get(indexName);
            IndexReader reader = DirectoryReader.open(indexDir);
            int documentCount = reader.maxDoc();
            indexObject.addProperty("documentCount", documentCount);

            // Retrieve the languages
            DbConnection dbConn = new DbConnection();
            List<String> languages = dbConn.retrieveIndexInfo(indexName);
            dbConn.closeConnection();
            Gson gson = new GsonBuilder().create();
            indexObject.add("languages", gson.toJsonTree(languages).getAsJsonArray());

            // Add the index object to the results
            JsonObject indexResultsObject = new JsonObject();
            indexResultsObject.add(indexName, indexObject);
            resultsPerIndex.add(indexResultsObject);
        }

        JsonObject resultsObject = new JsonObject();
        resultsObject.add("results", resultsPerIndex);

        LOGGER.info("Returning index-info results");
        return resultsObject.toString();
    }

}
