package eu.ows.mosaic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Provides constants and helper methods with regards to the core module of the MOSAIC search service.
 */
public abstract class CoreUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(CoreUtils.class);

    public static final String RANKING_ASC = "asc";
    public static final String RANKING_DESC = "desc";
    public static final int DEFAULT_RESULTS_LIMIT = 20;

    public static final String DEFAULT_INDEX_DIR_PATH = 
        System.getProperty("user.dir").substring(0, 
            (System.getProperty("user.dir").contains("search-service") ? System.getProperty("user.dir").indexOf("search-service") : System.getProperty("user.dir").length()))
        + "lucene" + File.separator;

    public static final String DEFAULT_PARQUET_DIR_PATH = 
        System.getProperty("user.dir").substring(0, 
            (System.getProperty("user.dir").contains("search-service") ? System.getProperty("user.dir").indexOf("search-service") : System.getProperty("user.dir").length()))
        + "resources" + File.separator;

    public static final String DEFAULT_CONFIG_FILE_PATH = 
        System.getProperty("user.dir")
        + (System.getProperty("user.dir").endsWith("core") ? "" : File.separator + "core")
        + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "config.json";

    public static final String DEFAULT_OPEN_SEARCH_PATH = 
        System.getProperty("user.dir")
        + (System.getProperty("user.dir").endsWith("core") ? "" : File.separator + "core")
        + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator 
        + "META-INF" + File.separator + "resources" + File.separator + "opensearch.xml";

    public static final String DEFAULT_DATABASE_FILE_PATH = "/tmp/mosaic_db";

    public static final String DEFAULT_ID_COLUMN = "record_id";

    private static final SimpleDateFormat WARC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    private static String indexDirPath;
    private static String parquetDirPath;
    private static String configFilePath;
    private static String databaseFilePath;
    private static String idColumn;

    /**
     * Getter method for the index directory path.
     * @return Path of directory where indexes are stored
     */
    public static String getIndexDirPath() {
        return indexDirPath;
    }

    /**
     * Setter method for the index directory path.
     * @param newIndexDirPath New path of index directory
     */
    public static void setIndexDirPath(String newIndexDirPath) {
        LOGGER.info("Setting index directory path to: {}", newIndexDirPath);
        indexDirPath = newIndexDirPath;
    }

    /**
     * Getter method for the parquet directory path.
     * @return Path of directory where metadata as parquet files are stored
     */
    public static String getParquetDirPath() {
        return parquetDirPath;
    }

    /**
     * Setter method for the parquet directory path.
     * @param newParquetDirPath New path of parquet directory
     */
    public static void setParquetDirPath(String newParquetDirPath) {
        LOGGER.info("Setting parquet directory path to: {}", newParquetDirPath);
        parquetDirPath = newParquetDirPath;
    }

    /**
     * Getter method for the config file path.
     * @return Path of the configuration file
     */
    public static String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * Setter method for the config file path.
     * @param newConfigFilePath New path of config file
     */
    public static void setConfigFilePath(String newConfigFilePath) {
        LOGGER.info("Setting config file path to: {}", newConfigFilePath);
        configFilePath = newConfigFilePath;
    }

    /**
     * Getter method for the database file path.
     * @return Path of the database file
     */
    public static String getDatabaseFilePath() {
        return databaseFilePath;
    }

    /**
     * Setter method for the database file path.
     * @param newDatabaseFilePath New path of database file
     */
    public static void setDatabaseFilePath(String newDatabaseFilePath) {
        LOGGER.info("Setting database file path to: {}", newDatabaseFilePath);
        databaseFilePath = newDatabaseFilePath;
    }

    /**
     * Getter method for the ID column name in the Parquet file(s).
     * @return Name of the ID column
     */
    public static String getIdColumn() {
        return idColumn;
    }

    /**
     * Setter method for the ID column name in the Parquet file(s).
     * @param newIdColumn New name of the ID column
     */
    public static void setIdColumn(String newIdColumn) {
        LOGGER.info("Setting ID column name to: {}", newIdColumn);
        idColumn = newIdColumn;
    }

    /**
     * Sets the OpenSearch URL template in the XML file.
     * @param openSearchUrlTemplate New OpenSearch URL template of this search service
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws TransformerException
     * @throws URISyntaxException
     */
    public static void setOpenSearchUrlTemplate(String openSearchUrlTemplate) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, TransformerException, URISyntaxException {

        LOGGER.info("Setting OpenSearch URL template to: {}", openSearchUrlTemplate);

        InputStream inputStream = CoreConfig.getInstance().getClass().getClassLoader().getResourceAsStream("META-INF/resources/opensearch-template.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document input = factory
            .newDocumentBuilder()
            .parse(inputStream);

        XPath xpath = XPathFactory
            .newInstance()
            .newXPath();
        String expr = String.format("//*[contains(@%s, '%s')]", "template", "");
        NodeList nodes = (NodeList) xpath.evaluate(expr, input, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element value = (Element) nodes.item(i);
            if (value.getNodeName().equals("Url")) {
                value.setAttribute("template", openSearchUrlTemplate);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();

        FileOutputStream fileOutputStream = new FileOutputStream(new File(DEFAULT_OPEN_SEARCH_PATH));
        transformer.transform(new DOMSource(input), new StreamResult(fileOutputStream));

        LOGGER.info("Updated the Open Search URL template in the XML file");
    }

    /**
     * Retrieves the full text of a document from the Parquet file(s).
     * @param indexName Name of the index
     * @param id ID of the document
     * @param idColumn Name of the ID column
     * @return Full text of the document
     * @throws SQLException
     */
    public static String getFullTextOfDocument(String indexName, String id, String idColumn) throws SQLException {
        LOGGER.info("Retrieving full text of document with ID: {}", id);

        DbConnection dbConn = new DbConnection();
        String fullText = dbConn.retrieveFullText(indexName, id, idColumn);
        dbConn.closeConnection();

        return fullText;
    }

    /**
     * Highlights the most relevant sentence in a text snippet based on the query terms.
     * Optionally loads the full text of the document if no sentence has been found.
     * @param textSnippet Text snippet to be highlighted
     * @param queryTerms Query terms used for highlighting
     * @return Highlighted text snippet
     * @throws SQLException
     */
    public static String extractTextSnippet(String textSnippet, String[] queryTerms, String indexName, String id, boolean loadFullTextDynamically) {
        LOGGER.info("Extracting text snippet from document with ID {} and query terms {}", id, queryTerms);
        List<String> mostFrequentSentences = getMostFrequentSentences(textSnippet, queryTerms);

        // If no sentence has been found, load the full text of the document or use the first 200 characters
        if (mostFrequentSentences.isEmpty()) {
            if (loadFullTextDynamically) {
                try {
                    textSnippet = getFullTextOfDocument(indexName, id, DEFAULT_ID_COLUMN);
                } catch (SQLException e) {
                    LOGGER.error("SQLException while trying to retrieve the full text of the document with ID {}", id, e);
                }
                mostFrequentSentences = getMostFrequentSentences(textSnippet, queryTerms);
            } else {
                return textSnippet.substring(0, Math.min(textSnippet.length(), 200));
            }

        }

        // Highlight the most relevant sentence
        String highlightedTextSnippet = mostFrequentSentences.size() > 0 ? mostFrequentSentences.get(0) : textSnippet.substring(0, Math.min(textSnippet.length(), 200));
        highlightedTextSnippet = textSnippet.substring(textSnippet.indexOf(highlightedTextSnippet), Math.min(textSnippet.indexOf(highlightedTextSnippet)+Math.max(200, highlightedTextSnippet.length()), textSnippet.length()));

        return StringUtils.trim(highlightedTextSnippet);
    }

    /**
     * Retrieves the most frequent sentence(s) in a text snippet based on the query terms.
     * @param textSnippet Text snippet to be analyzed
     * @param queryTerms Query terms used for analysis
     * @return List of most frequent sentence(s)
     */
    private static List<String> getMostFrequentSentences(String textSnippet, String[] queryTerms) {
        Map<String, Integer> sentenceOccurrences = new HashMap<>();

        // Split plain text into sentences
        String[] sentences = textSnippet.split("[.!?\\n]+");

        // Initialize sentenceOccurrences map
        for (String sentence : sentences) {
            sentenceOccurrences.put(StringUtils.trim(sentence), 0);
        }

        // Count occurrences of query terms in each sentence
        for (String term : queryTerms) {
            for (String sentence : sentences) {
                if (sentence.toLowerCase().contains(term.toLowerCase())) {
                    sentenceOccurrences.put(StringUtils.trim(sentence), sentenceOccurrences.getOrDefault(StringUtils.trim(sentence), 0) + 1);
                }
            }
        }

        // Find the sentence(s) with the highest occurrence count
        int maxOccurrences = Math.max(1, Collections.max(sentenceOccurrences.values()));
        List<String> mostFrequentSentences = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sentenceOccurrences.entrySet()) {
            if (entry.getValue() == maxOccurrences) {
                mostFrequentSentences.add(entry.getKey());
            }
        }

        return mostFrequentSentences;
    }

    /**
     * Re-ranks the results of a search by word count in the specified sort order.
     * @param results List of results from a search
     * @param ranking Type of sort order
     * @return Re-ranked results sorted by the specified sort order
     */
    public static List<Map<String, String>> reRankResults(List<Map<String, String>> searchResults, String ranking) {
        LOGGER.info("Re-ranking search results with key: {}", ranking);

        if (ranking != null && (ranking.equalsIgnoreCase(RANKING_ASC) || ranking.equalsIgnoreCase(RANKING_DESC))) {
            searchResults.sort(new Comparator<Map<String, String>>() {

                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    if (ranking.equalsIgnoreCase(RANKING_DESC)) {
                        return o2.get("plain_text").split("\\s+").length - o1.get("plain_text").split("\\s+").length;
                    }
                    return o1.get("plain_text").split("\\s+").length - o2.get("plain_text").split("\\s+").length;
                }

            });
        }

        return searchResults;
    }

    /**
     * Converts a string date in WARC format to epoch time.
     * @param warcDate String date in WARC format
     * @return Epoch time in milliseconds
     */
    public static long convertWarcDateToEpoch(String warcDate) {
        try {
            return WARC_DATE_FORMAT.parse((String) warcDate).getTime() * 1000;
        } catch (ParseException e) {
            LOGGER.error("ParseException while trying to convert the string warc date to epoch", e);
        }
        return -1;
    }

    /**
     * Parses the keywords from a JSON string.
     * @param keywordsAsJson JSON string containing the keywords
     * @return List of keywords
     */
    public static List<String> parseKeywords(String keywordsAsJson) {
        return GSON.fromJson(keywordsAsJson, LIST_TYPE);
    }

    /**
     * Checks if the index exists in the indexes map.
     * @param indexName Name of the index
     * @return True if the index exists, false otherwise
     */
    public static boolean isValidIndex(String indexName) {
        return indexName != null && !indexName.isEmpty() && ResourceManager.getInstance().getIndexes().containsKey(indexName);
    }

    /**
     * Checks if the associated metadata exists for the index.
     * @param indexName Name of the index
     * @return True if the metadata exists, false otherwise
     */
    public static boolean metadataExistsForIndex(String indexName) {
        return indexName != null && !indexName.isEmpty() && ResourceManager.getInstance().getMetadataDirectoryNames().contains(indexName);
    }

    /**
     * Checks if the limit parameter is valid.
     * @param limit Limit parameter
     * @return True if the limit is valid, false otherwise
     */
    public static boolean isValidLimit(String limit) {
        return NumberUtils.toInt(limit, CoreUtils.DEFAULT_RESULTS_LIMIT) > 0;
    }

    /**
     * Converts the limit parameter to an integer.
     * @param limit Limit parameter
     * @return Integer value of the limit parameter
     */
    public static int convertLimit(String limit) {
        return NumberUtils.toInt(limit, CoreUtils.DEFAULT_RESULTS_LIMIT);
    }

    /**
     * Checks if the page parameter is valid.
     * @param page Page parameter
     * @return True if the page is valid, false otherwise
     */
    public static boolean isValidPage(String page) {
        return NumberUtils.toInt(page, 1) > 0;
    }

    /**
     * Converts the page parameter to an integer.
     * @param page Page parameter
     * @return Integer value of the page parameter
     */
    public static int convertPage(String page) {
        return NumberUtils.toInt(page, 1);
    }

}
