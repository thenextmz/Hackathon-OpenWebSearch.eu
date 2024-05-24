package eu.ows.mosaic;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/full-text")
public class FullTextResource {
    
    private static Logger LOGGER = LoggerFactory.getLogger(FullTextResource.class);

    private final String DEFAULT_ID_COLUMN = "record_id";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String fullText(@Context UriInfo uriInfo) throws SQLException {
        Map<String, String> queryParams = uriInfo.getQueryParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
        LOGGER.info("Received full text request: {}", queryParams);

        if (!queryParams.containsKey("id")) {
            throw new MosaicWebException("Missing id parameter");
        }
        
        String id = queryParams.get("id");
        String idColumn = queryParams.containsKey("column") ? queryParams.get("column") : DEFAULT_ID_COLUMN;
        String selectedIndexName = queryParams.containsKey("index") ? queryParams.get("index") : "";

        if (!selectedIndexName.isEmpty() && !CoreUtils.isValidIndex(selectedIndexName)) {
            throw new MosaicWebException("Could not retrieve full text for document with id = " + id + ". Index " + selectedIndexName + " not found");
        }

        // Retrieve full text for the document with the given id
        LOGGER.info("Retrieving full text for id {} using column {}", id, idColumn);
        String fullText = "";
        if (!selectedIndexName.isEmpty()) {
            fullText = CoreUtils.getFullTextOfDocument(selectedIndexName, id, idColumn);
        } else {
            for (String indexName : ResourceManager.getInstance().getIndexes().keySet()) {
                fullText = CoreUtils.getFullTextOfDocument(indexName, id, idColumn);
                if (fullText != null && !fullText.isEmpty()) {
                    break;
                }
            }
        }
        LOGGER.info("Retrieved full text for id {}", id);

        LOGGER.info("Returning full text result");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode().put("id", id).put("fullText", fullText).toPrettyString();
    }
}
