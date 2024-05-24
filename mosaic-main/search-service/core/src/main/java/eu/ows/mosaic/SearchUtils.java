package eu.ows.mosaic;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility class for searching in the Lucene indexes, adding metadata to the search results, and serializing the results.
 */
public class SearchUtils {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SearchUtils.class);

    private static Map<String, FSDirectory> indexes = ResourceManager.getInstance().getIndexes();

    private static SearchRequestScoreDocCache searchRequestScoreDocCache = new SearchRequestScoreDocCache(1000);

    private static final String RESPONSE_TYPE_JSON = "application/json";
    private static final String RESPONSE_TYPE_XML = "application/xml";

    /**
     * Searches in the indexes based on the given query parameters and returns the search results as JSON.
     */
    public static String searchJson(Map<String, String> queryParams) throws ParseException, IOException, SQLException {
        return performSearch(queryParams, RESPONSE_TYPE_JSON);
    }

    /**
     * Searches in the indexes based on the given query parameters and returns the search results as XML.
     */
    public static String searchXml(Map<String, String> queryParams) throws ParseException, IOException, SQLException {
        return performSearch(queryParams, RESPONSE_TYPE_XML);
    }

    /**
     * Performs the search in the indexes based on the given query parameters and returns the search results in the specified response type.
     */
    private static String performSearch(Map<String, String> queryParams, String responseType) throws ParseException, IOException, SQLException {

        // Validate the query parameters of all metadata modules
        for (MetadataModule module : PluginManager.getInstance().getModules().values()) {
            module.validateParams(queryParams);
        }

        // Combine the query parameters of all metadata modules
        Map<String, Object> parsedQueryParams = new TreeMap<>();
        for (MetadataModule module : PluginManager.getInstance().getModules().values()) {
            parsedQueryParams.putAll(module.parseQueryParams(queryParams));
        }
        LOGGER.info("Parsed query parameters: {}", parsedQueryParams);
        String q = (String) parsedQueryParams.get("q");
        String selectedIndexName = (String) parsedQueryParams.get("index");

        // Create an analyzer for the query parser
        Analyzer analyzer;
        if (CoreConfig.getInstance().isPluginEnabled(PluginManager.ANALYZER)) {
            analyzer = ((AnalyzerComponent) PluginManager.getInstance().getComponents().get(PluginManager.ANALYZER)).getAnalyzer(new StandardAnalyzer());
        } else {
            analyzer = new StandardAnalyzer();
        }

        // Create a query parser and modify the query if required
        QueryParser queryParser = new QueryParser("contents", analyzer);
        if (CoreConfig.getInstance().isPluginEnabled(PluginManager.QUERY)) {
            q = ((QueryComponent) PluginManager.getInstance().getComponents().get(PluginManager.QUERY)).modifyQuery(q);
        }
        Query query = queryParser.parse(q);

        LOGGER.info("Query: {}", query.toString("contents"));
        LOGGER.info("Index: {}", selectedIndexName);

        // Generate a set of index names used for searching
        Set<String> indexNamesToBeSearchedIn = indexes.keySet();
        if (selectedIndexName != null) {
            indexNamesToBeSearchedIn = new TreeSet<String>();
            indexNamesToBeSearchedIn.add(selectedIndexName);
        }

        // Serialize the search results based on the response type
        switch (responseType) {
            case RESPONSE_TYPE_JSON:
                return getSerializedJsonResponse(query, q, parsedQueryParams, indexNamesToBeSearchedIn);
            case RESPONSE_TYPE_XML:
                return getSerializedXmlResponse(query, q, parsedQueryParams, indexNamesToBeSearchedIn);
            default:
                return null;
        }
    }

    /**
     * Serializes the search results as JSON.
     * @param query Query object
     * @param q Query string
     * @param parsedQueryParams Parsed query parameters
     * @param indexNamesToBeSearchedIn Set of index names to be searched in
     * @return JSON string of the serialized search results
     */
    private static String getSerializedJsonResponse(Query query, String q, Map<String, Object> parsedQueryParams, Set<String> indexNamesToBeSearchedIn) throws IOException, SQLException {
        JsonArray resultsPerIndex = new JsonArray();

        for (String indexName: indexNamesToBeSearchedIn) {
            // Search in index and fetch search result as JSON object
            List<Map<String, String>> results = searchInIndex(query, indexName, parsedQueryParams);
            JsonArray resultsArray = new JsonArray();

            // Serialize each search result
            for (Map<String, String> result: results) {
                JsonObject jsonResult = new JsonObject();
                result.put("q", q);
                for (MetadataModule module : PluginManager.getInstance().getModules().values()) {
                    JsonObject serializedModuleObject = module.serializeJson(result, parsedQueryParams);
                    serializedModuleObject.keySet().forEach(key -> jsonResult.add(key, serializedModuleObject.get(key)));
                }
                resultsArray.add(jsonResult);
            }

            // Add the serialized search results to the JSON array
            JsonObject resultsObject = new JsonObject();
            resultsObject.add(indexName, resultsArray);
            resultsPerIndex.add(resultsObject);
        }

        JsonObject resultsObject = new JsonObject();
        resultsObject.add("results", resultsPerIndex);

        return resultsObject.toString();
    }

    /**
     * Serializes the search results as XML.
     * @param query Query object
     * @param q Query string
     * @param parsedQueryParams Parsed query parameters
     * @param indexNamesToBeSearchedIn Set of index names to be searched in
     * @return XML string of the serialized search results
     */
    private static String getSerializedXmlResponse(Query query, String q, Map<String, Object> parsedQueryParams, Set<String> indexNamesToBeSearchedIn) throws IOException, SQLException {
        int totalResults = getTotalResults(query, indexNamesToBeSearchedIn);
        int page = (int) parsedQueryParams.get("page");
        int limit = (int) parsedQueryParams.get("limit");
        int startIndex = 1 + limit * (page - 1);
        int itemsPerPage = (int) parsedQueryParams.get("limit");

        // Create the XML response for the search results based on the OpenSearch protocol
        String xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                             "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\">" +
                             "<title>MOSAIC Search: " + query.toString("contents") + "</title>" +
                             "<description>Search results for \"" + query.toString("contents") + "\" at MOSAIC Search Service</description>" +
                             "<author>" +
                                 "<name>OpenWebSearch.eu</name>" +
                             "</author>" +
                             "<opensearch:totalResults>" + totalResults + "</opensearch:totalResults>" +
                             "<opensearch:startIndex>" + startIndex + "</opensearch:startIndex>" +
                             "<opensearch:itemsPerPage>" + itemsPerPage + "</opensearch:itemsPerPage>" +
                             "<opensearch:Query role=\"request\" searchTerms=\"" + query.toString("contents") + "\" startPage=\"1\" />" +
                             "<link rel=\"alternate\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/search?q=" + query.toString("contents") + "&amp;pw=" + page + "&amp;limit=" + limit + "\" type=\"application/json\"/>" +
                             "<link rel=\"self\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/searchxml?q=" + query.toString("contents") + "&amp;pw=" + page + "&amp;limit=" + limit + "\" type=\"application/atom+xml\"/>";

        if (page > 1) {
            xmlResponse += "<link rel=\"first\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/searchxml?q=" + query.toString("contents") + "&amp;pw=1&amp;limit=" + limit + "\" type=\"application/atom+xml\"/>";
            xmlResponse += "<link rel=\"previous\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/searchxml?q=" + query.toString("contents") + "&amp;pw=" + (page-1) + "&amp;limit=" + limit + "\" type=\"application/atom+xml\"/>";
        }

        if (totalResults > startIndex + itemsPerPage) {
            xmlResponse += "<link rel=\"next\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/searchxml?q=" + query.toString("contents") + "&amp;pw=" + (page+1) + "&amp;limit=" + limit + "\" type=\"application/atom+xml\"/>";
            xmlResponse += "<link rel=\"last\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/searchxml?q=" + query.toString("contents") + "&amp;pw=" + (totalResults/itemsPerPage) + "&amp;limit=" + limit + "\" type=\"application/atom+xml\"/>";
        }

        xmlResponse += "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"" + CoreConfig.getInstance().getBaseUrl() + "/opensearch.xml\"/>";

        for (String indexName: indexNamesToBeSearchedIn) {
            // Search in index and fetch search result as XML object
            List<Map<String, String>> results = searchInIndex(query, indexName, parsedQueryParams);

            // Serialize each search result
            for (Map<String, String> result : results) {
                result.put("q", q);
                xmlResponse += "<item>";
                for (MetadataModule module : PluginManager.getInstance().getModules().values()) {
                    xmlResponse += module.serializeXml(result, parsedQueryParams);
                }
                xmlResponse += "<index>" + indexName + "</index></item>";
            }
        }

        xmlResponse += "</feed>";

        return xmlResponse;
    }

    /**
     * Calculates the total number of results in the Lucene index for a search request.
     * @param query Query object
     * @param indexNamesToBeSearchedIn Set of index names to be searched in
     * @return Total number of results for the search request
     */
    private static int getTotalResults(Query query, Set<String> indexNamesToBeSearchedIn) throws IOException, SQLException {
        int totalResults = 0;

        LOGGER.info("Calculating total number of results for search request");
        for (String indexName: indexNamesToBeSearchedIn) {
            LOGGER.info("Searching in index: {}", indexName);

            // Create index from file system directory
            FSDirectory indexDir = ResourceManager.getInstance().getIndexes().get(indexName);

            // Check if the index exists
            boolean indexExists = DirectoryReader.indexExists(indexDir);
            LOGGER.info("Index exists: {}", indexExists);

            // Create index reader and index searcher
            String indexPath = CoreUtils.getIndexDirPath() + indexName;
            LOGGER.info("Used index for search request: {}", indexPath);

            // Perform the search in the Lucene index
            IndexSearcher searcher = createSearcher(indexPath);
            searcher.setSimilarity(new BM25Similarity());

            // Fetch the total number of results in the Lucene index
            TopDocs topDocs = searcher.search(query, 1);
            totalResults += topDocs.totalHits.value;
        }

        return totalResults;
    }
    
    /**
     * Searches in the Lucene index for a given query and returns the search results.
     * @param query Query object
     * @param indexName Name of the Lucene index
     * @param queryParams Query parameters
     * @return List of search results
     */
    public static List<Map<String, String>> searchInIndex(Query query, String indexName, Map<String, Object> queryParams) throws IOException, SQLException {
    
        LOGGER.info("Searching in index: {}", indexName);

        // Create index from file system directory
        FSDirectory indexDir = ResourceManager.getInstance().getIndexes().get(indexName);

        // Check if the index exists
        boolean indexExists = DirectoryReader.indexExists(indexDir);
        LOGGER.info("Index exists: {}", indexExists);

        // Create index reader and index searcher
        String indexPath = CoreUtils.getIndexDirPath() + indexName;
        LOGGER.info("Used index for search request: {}", indexPath);

        // Create index reader and index searcher
        IndexReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = createSearcher(indexPath);
        searcher.setSimilarity(new BM25Similarity());

        // Fetch results in Lucene index
        List<Map<String, String>> results = fetchResults(reader, searcher, query, indexName, queryParams);

        // Close the IndexReader
        reader.close();

        return results;
    }

    /**
     * Creates an index searcher from the file path of the index.
     * @param indexPath Path of the Lucene index
     * @return IndexSearcher created from given path
     * @throws IOException
     */
    private static IndexSearcher createSearcher(String indexPath) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

    /**
     * Fetches the search results from the Lucene index and adds metadata to the search results.
     * @param reader IndexReader of the Lucene index
     * @param searcher IndexSearcher of the Lucene index
     * @param query Query object
     * @param indexName Name of the Lucene index
     * @param queryParams Parsed query parameters
     * @return List of maps representing the search results
     * @throws IOException
     * @throws SQLException
     */
    private static List<Map<String, String>> fetchResults(IndexReader reader, IndexSearcher searcher, Query query, String indexName, Map<String, Object> queryParams) throws IOException, SQLException {
        final List<Map<String, String>> results = new ArrayList<>();

        ScoreDoc lastScoreDoc = searchRequestScoreDocCache.getLastScoreDocFromPreviousPage(queryParams);
        LOGGER.info("Last ScoreDoc: {}", lastScoreDoc);

        // Retrieve available metadata columns for the search results
        DbConnection dbConn = new DbConnection();
        Set<String> metadataColumns = dbConn.retrieveMetadataColumns(indexName);
        LOGGER.info("Available metadata columns: {}", metadataColumns);

        // Retrieve metadata columns of the modules and intersect them with the available metadata columns
        Set<String> moduleMetadataColumns = new HashSet<>();
        PluginManager.getInstance().getModules().values().forEach(module -> moduleMetadataColumns.addAll(module.getMetadataColumns()));
        LOGGER.info("Module metadata columns: {}", moduleMetadataColumns);
        metadataColumns.retainAll(moduleMetadataColumns);

        // Build the metadata query that is used to retrieve metadata for the search results
        String metadataQuery = dbConn.buildMetadataQuery(indexName, metadataColumns, queryParams);
        dbConn.closeConnection();

        // Iteratively increase the number of hits until the hit limit has been reached
        // or no more documents could be found
        int numHitsLimit = (int) queryParams.get("limit");
        if (lastScoreDoc == null) {
            numHitsLimit *= (int) queryParams.get("page");
        }
        LOGGER.info("Number of hits limit for search iteration: {}", numHitsLimit);
        while (results.size() < numHitsLimit) {

            LOGGER.info("Fetching results from Lucene index");

            // Perform the search in the Lucene index
            TopDocs topDocs = null;
            if (lastScoreDoc == null) {
                topDocs = searcher.search(query, numHitsLimit);
            } else {
                topDocs = searcher.searchAfter(lastScoreDoc, query, numHitsLimit - results.size());
            }

            if (topDocs.scoreDocs.length == 0) {
                // No (more) documents found, stop fetching results
                break;
            }

            lastScoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length-1];
            ScoreDoc[] hits = topDocs.scoreDocs;
            LOGGER.info("Fetched {} documents from Lucene index", topDocs.totalHits);

            // Collect the search results from Lucene
            StoredFields allDocs = reader.storedFields();
            List<String> documentIds = new ArrayList<>();
            for (ScoreDoc hit : hits) {
                Document document = allDocs.document(hit.doc);
                documentIds.add(document.get("id"));
            }

            // Fetch metadata for the search results
            LOGGER.info("Fetching metadata for {} documents of search iteration", documentIds.size());

            List<Map<String, String>> resultsToAdd = documentIds.parallelStream()
                .map(documentId -> retrieveMetadataForDocument(indexName, documentId, metadataQuery, queryParams, metadataColumns))
                .filter(result -> result != null)
                .collect(Collectors.toList());

            results.addAll(resultsToAdd);
        }

        // Remove search results that do not belong to the requested page and
        // cache the last ScoreDoc for the search request
        if (searchRequestScoreDocCache.getLastScoreDocFromPreviousPage(queryParams) == null && (int) queryParams.get("page") > 1) {
            LOGGER.info("Removing search results that do not belong to the requested page");
            results.subList(0, Math.min(results.size(), (int) queryParams.get("limit") * ((int) queryParams.get("page")-1))).clear();
        }

        LOGGER.info("Adding last ScoreDoc to search request cache");
        searchRequestScoreDocCache.put(queryParams, lastScoreDoc);

        // Optionally re-rank the search results
        String ranking = (String) queryParams.get("ranking");
        String sortBy = (String) queryParams.get("sortby");
        List<Map<String, String>> reRankedResults = CoreUtils.reRankResults(results, ranking, sortBy);

        return reRankedResults;
    }

    /**
     * Retrieves metadata for a document from an index with a given id from the database.
     * @param indexName Name of the Lucene index
     * @param documentId Id of the document
     * @param metadataQuery SQL query for retrieving metadata
     * @param queryParams Parsed query parameters
     * @param metadataColumns Set of available metadata columns in the Parquet file(s)
     * @return Map of metadata columns and their values representing the search result
     */
    private static Map<String, String> retrieveMetadataForDocument(String indexName, String documentId, String metadataQuery, Map<String, Object> queryParams, Set<String> metadataColumns) {
        try {
            LOGGER.info("Retrieving metadata for document with id: {}", documentId);
            DbConnection dbConn = new DbConnection();
            ResultSet rs = dbConn.retrieveMetadataForDocument(indexName, documentId, metadataQuery, queryParams, metadataColumns);
            ResultSetMetaData rsMetadata = rs.getMetaData();
            LOGGER.info("Retrieved metadata for document with id: {}", documentId);

            while (rs.next()) {

                // Create a map of metadata columns and their values for the search result
                Map<String, String> result = new TreeMap<>();
                for (int i = 1; i <= rsMetadata.getColumnCount(); ++i) {
                    result.put(rsMetadata.getColumnName(i), rs.getString(i));
                }
                result.put("index", indexName);

                // Check if the search result passes the manual filter of the modules
                boolean passedManualFilter = true;
                for (MetadataModule module : PluginManager.getInstance().getModules().values()) {
                    if (!module.inManualFilter(result, queryParams)) {
                        LOGGER.info("Search result did not pass manual filter of module: {}", module.getClass().getSimpleName());
                        passedManualFilter = false;
                        break;
                    }
                }

                // Add the search result to the list of results
                if (passedManualFilter) {
                    rs.close();
                    dbConn.closeConnection();
                    return result;
                }
            }

            rs.close();
            dbConn.closeConnection();
        } catch (SQLException e) {
            LOGGER.error("Error while retrieving metadata for document with id: {}", documentId, e);
        }

        // Return null if no metadata could be retrieved or the search result did not pass the manual filter
        return null;
    }
}
