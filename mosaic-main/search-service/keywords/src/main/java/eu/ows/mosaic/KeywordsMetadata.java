package eu.ows.mosaic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Metadata module for the keywords metadata.
 */
public class KeywordsMetadata extends MetadataModule {
    
    private static Logger LOGGER = LoggerFactory.getLogger(KeywordsMetadata.class);

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    @Override
    public Set<String> getMetadataColumns() {
        return Set.of("keywords");
    }

    @Override
    public Set<String> getFilterColumns() {
        return Set.of("keyword");
    }

    @Override
    public void validateParams(Map<String, String> queryParams) {
        super.validateParams(queryParams);
    }

    @Override
    public Map<String, Object> parseQueryParams(Map<String, String> queryParams) {
        Map<String, Object> parsedParams = new TreeMap<>();

        String keywordValue = (queryParams.containsKey("keyword")) ? queryParams.get("keyword") : null;
        parsedParams.put("keyword", keywordValue);

        return parsedParams;
    }

    @Override
    public String getSqlFilterClauses(Map<String, Object> queryParams, Set<String> metadataColumns) {
        String filter = "";

        if (metadataColumns.contains("keywords") && queryParams.get("keyword") != null) {
            filter += " AND json_contains(json_extract(keywords, '$'), json_array(?)) ";
        }

        return filter;
    }

    @Override
    public List<Object> getSqlFilterValues(Map<String, Object> queryParams, Set<String> metadataColumns) {
        List<Object> values = new ArrayList<>();

        if (metadataColumns.contains("keywords") && queryParams.get("keyword") != null) {
            values.add(queryParams.get("keyword"));
        }

        return values;
    }

    @Override
    public boolean inManualFilter(Map<String, String> result, Map<String, Object> queryParams) {
        return super.inManualFilter(result, queryParams);
    }

    @Override
    public JsonObject serializeJson(Map<String, String> result, Map<String, Object> queryParams) {
        JsonObject json = new JsonObject();

        List<String> keywords = GSON.fromJson(result.get("keywords"), LIST_TYPE);
        JsonArray jsonKeywords = new JsonArray();
        if (keywords != null) {
            keywords.stream().forEach(jsonKeywords::add);
        }
        json.add("keywords", jsonKeywords);

        return json;
    }

    @Override
    public String serializeXml(Map<String, String> result, Map<String, Object> queryParams) {
        String xml = "";                

        xml += "<keywords>";
        if (result.containsKey("keywords")) {
            List<String> keywords = GSON.fromJson(result.get("keywords"), LIST_TYPE);
            for (String keyword : keywords) {
                xml += "<keyword>" + keyword + "</keyword>";
            }
        }
        xml += "</keywords>";        

        return xml;
    }
}
