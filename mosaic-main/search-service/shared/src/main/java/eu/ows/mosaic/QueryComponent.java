package eu.ows.mosaic;

/**
 * Custom Query class to define the Query for the search service.
 * Change the implementation of the modifyQuery method stub to return the desired Query.
 */
public interface QueryComponent extends CoreComponent {
    String modifyQuery(String q);
}
