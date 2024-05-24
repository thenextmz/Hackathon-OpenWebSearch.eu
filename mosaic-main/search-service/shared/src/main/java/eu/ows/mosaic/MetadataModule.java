package eu.ows.mosaic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;

import com.google.gson.JsonObject;

/**
 * Abstract class for metadata modules.
 * Provides methods for parsing query parameters, filtering results, and serializing results.
 */
public abstract class MetadataModule {

    /**
     * Returns the metadata columns that are expected to be returned by the module.
     * Subclasses should override this method to add metadata columns if needed.
     * @return Set of metadata columns in the Parquet file(s)
     */
    public Set<String> getMetadataColumns() {
        return Set.of();
    }

    /**
     * Returns the filter columns that are expected to be used for filtering the results using DuckDB.
     * Subclasses should override this method to add filter columns if needed.
     * @return Set of filter columns in the Parquet file(s)
     */
    public Set<String> getFilterColumns() {
        return Set.of();
    }

    /**
     * Validates the query parameters.
     * Subclasses should override this method to add additional validation.
     * @param queryParams Map of query parameters
     */
    public void validateParams(Map<String, String> queryParams) {}

    public Map<String, Object> parseQueryParams(Map<String, String> queryParams) {
        Map<String, Object> parsedParams = new TreeMap<>();

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (getFilterColumns().contains(parsedParams.get(entry.getKey()))) {
                parsedParams.put(entry.getKey(), entry.getValue());
            }
        }

        return parsedParams;
    }

    /**
     * Returns the SQL filter clauses for the query parameters.
     * By default, this method returns a string concatenating additional where clauses for each filter column 
     * using AND operation with the equal sign (=) as comparison operator.
     * @param queryParams Map of query parameters
     * @param metadataColumns Set of available metadata columns in the Parquet file(s)
     * @return SQL filter clauses using parameters for the PreparedStatement
     */
    public String getSqlFilterClauses(Map<String, Object> queryParams, Set<String> metadataColumns) {
        String filter = "";

        for (String column : getFilterColumns()) {
            if (metadataColumns.contains(column) && queryParams.get(column) != null) {
                filter += " AND " + column + " = ? ";
            }
        }

        return filter;
    }

    /**
     * Returns the SQL filter values for the query parameters which are used as parameters for the PreparedStatement.
     * By default, this method returns a list of filter values for each filter column that are included in the query parameters.
     * @param queryParams Map of query parameters
     * @param metadataColumns Set of available metadata columns in the Parquet file(s)
     * @return List of SQL filter values used as parameters for the PreparedStatement
     */
    public List<Object> getSqlFilterValues(Map<String, Object> queryParams, Set<String> metadataColumns) {
        List<Object> values = new ArrayList<>();

        for (String column : getFilterColumns()) {
            if (metadataColumns.contains(column) && queryParams.get(column) != null) {
                values.add(queryParams.get(column));
            }
        }

        return values;
    }

    /**
     * Checks if the result is in the manual filter.
     * Subclasses should override this method to add additional filtering that is not covered by the SQL filter.
     * @param result Map of metadata columns and their values of a search result
     * @param queryParams Map of query parameters
     * @return True if the result is in the manual filter, false otherwise
     */
    public boolean inManualFilter(Map<String, String> result, Map<String, Object> queryParams) {
        return true;
    }

    /**
     * Serializes the result as a JSON object.
     * By default, this method serializes the metadata columns defined in getMetadataColumns() that are included in the metadata columns set.
     * @param result Map of metadata columns and their values of a search result
     * @return JSON object of the serialized result
     */
    public JsonObject serializeJson(Map<String, String> result, Map<String, Object> queryParams) {
        JsonObject json = new JsonObject();

        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (getMetadataColumns().contains(entry.getKey())) {
                json.addProperty(entry.getKey(), entry.getValue());
            }
        }

        return json;
    }

    /**
     * Serializes the result as an XML string.
     * By default, this method serializes the metadata columns defined in getMetadataColumns() that are included in the metadata columns set.
     * @param result Map of metadata columns and their values of a search result
     * @return XML string of the serialized result
     */
    public String serializeXml(Map<String, String> result, Map<String, Object> queryParams) {
        String serializedXml = "";

        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (getMetadataColumns().contains(entry.getKey())) {
                String value = new UnicodeUnescaper().translate(StringEscapeUtils.escapeXml11(StringUtils.trim(entry.getValue())));
                serializedXml += "<" + entry.getKey() + ">" + value + "</" + entry.getKey() + ">";
            }
        }

        return serializedXml;
    }

}