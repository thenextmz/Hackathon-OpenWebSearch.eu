package eu.ows.mosaic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Query class to define the Query for the search service.
 * Change the implementation of the modifyQuery method stub to return the desired Query.
 */
public class CustomQuery implements QueryComponent {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomQuery.class);

    /**
     * Modifies the query string before it is parsed.
     * Change the implementation of this method stub to modify the query string.
     * @param q Original query string
     * @return Modified query string
     */
    @Override
    public String modifyQuery(String q) {
        
        // Implement the desired query modification

        LOGGER.info("Using original query");
        String modifiedQuery = q;
        return modifiedQuery;
    }

}