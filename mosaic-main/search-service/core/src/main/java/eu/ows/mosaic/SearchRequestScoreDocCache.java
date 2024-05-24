package eu.ows.mosaic;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.search.ScoreDoc;

/**
 * Custom cache class to store the ScoreDoc objects for the search service for caching purposes.
 */
public class SearchRequestScoreDocCache extends LinkedHashMap<Map<String, Object>, ScoreDoc> {

    public SearchRequestScoreDocCache(int maxSize) {
        super(maxSize, 0.75f, true);
    }

    /**
     * Get the ScoreDoc object of the previous page from the cache if it exists.
     * @param queryParams The query parameters to get the ScoreDoc object of the previous page.
     * @return The ScoreDoc object from the cache.
     */
    public ScoreDoc getLastScoreDocFromPreviousPage(Map<String, Object> queryParams) {
        ScoreDoc lastScoreDoc = null;

        if (queryParams.containsKey("page")) {
            Map<String, Object> previousPageQueryParams = new LinkedHashMap<>(queryParams);
            previousPageQueryParams.put("page", Integer.parseInt(queryParams.get("page").toString()) - 1);
            return this.get(previousPageQueryParams);
        }

        return lastScoreDoc;
    }

}
