package eu.ows.mosaic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/search")
public class SearchResource {

    private static Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@Context UriInfo uriInfo) throws ParseException, IOException, SQLException {
        Map<String, String> queryParams = uriInfo.getQueryParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
        LOGGER.info("Received search request: {}", queryParams);

        String response = SearchUtils.searchJson(queryParams);

        LOGGER.info("Returning results");
        return response;
    }

}
