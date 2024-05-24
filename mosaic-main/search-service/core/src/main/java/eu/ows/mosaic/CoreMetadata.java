package eu.ows.mosaic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Metadata module for the core metadata.
 */
public class CoreMetadata extends MetadataModule {

    private static Logger LOGGER = LoggerFactory.getLogger(CoreMetadata.class);

    @Override
    public Set<String> getMetadataColumns() {
        return Set.of(
            "record_id", "url", "title", "plain_text", "language", "warc_date"
        );
    }

    @Override
    public Set<String> getFilterColumns() {
        return Set.of(
            "language"
        );
    }

    @Override
    public void validateParams(Map<String, String> queryParams) {

        if (queryParams.containsKey("index") && !CoreUtils.isValidIndex(queryParams.get("index"))) {
            LOGGER.error("The selected index {} could not be found", queryParams.get("index"));
            throw new MosaicWebException(String.format("The selected index %s could not be found", queryParams.get("index")));
        }

        if (queryParams.containsKey("index") && !CoreUtils.metadataExistsForIndex(queryParams.get("index"))) {
            LOGGER.error("The metadata for index {} could not be found", queryParams.get("index"));
            throw new MosaicWebException(String.format("The metadata for index %s could not be found", queryParams.get("index")));
        }

        if (queryParams.containsKey("limit") && !CoreUtils.isValidLimit(queryParams.get("limit"))) {
            LOGGER.error("The limit parameter {} is invalid and must be a positive value", queryParams.get("limit"));
            throw new MosaicWebException(String.format("The limit parameter %s is invalid and must be a positive value", queryParams.get("limit")));
        }

        if (queryParams.containsKey("pw") && !CoreUtils.isValidPage(queryParams.get("pw"))) {
            LOGGER.error("The pw parameter {} is invalid and must be a positive value", queryParams.get("pw"));
            throw new MosaicWebException(String.format("The pw parameter %s is invalid and must be a positive value", queryParams.get("pw")));
        }

    }

    @Override
    public Map<String, Object> parseQueryParams(Map<String, String> queryParams) {
        Map<String, Object> parsedParams = new TreeMap<>();

        String qValue = (!queryParams.containsKey("q") || queryParams.get("q").length() == 0) ? "*:*" : queryParams.get("q");
        parsedParams.put("q", qValue);

        String indexValue = (queryParams.containsKey("index")) ? queryParams.get("index") : null;
        parsedParams.put("index", indexValue);

        String languageValue = (queryParams.containsKey("lang")) ? queryParams.get("lang") : null;
        parsedParams.put("language", languageValue);

        String rankingValue = (queryParams.containsKey("ranking")) ? queryParams.get("ranking") : null;
        parsedParams.put("ranking", rankingValue);

        int limitValue = CoreUtils.convertLimit(queryParams.get("limit"));
        parsedParams.put("limit", limitValue);

        int page = CoreUtils.convertPage(queryParams.get("pw"));
        parsedParams.put("page", page);

        boolean loadFullTextDynamicallyIfRequired = BooleanUtils.toBoolean(queryParams.getOrDefault("fulltext", "false"));
        parsedParams.put("fulltext", loadFullTextDynamicallyIfRequired);

        return parsedParams;
    }

    @Override
    public String getSqlFilterClauses(Map<String, Object> queryParams, Set<String> metadataColumns) {
        return super.getSqlFilterClauses(queryParams, metadataColumns);
    }

    @Override
    public List<Object> getSqlFilterValues(Map<String, Object> queryParams, Set<String> metadataColumns) {
        return super.getSqlFilterValues(queryParams, metadataColumns);
    }

    @Override
    public boolean inManualFilter(Map<String, String> result, Map<String, Object> queryParams) {
        return super.inManualFilter(result, queryParams);
    }

    @Override
    public JsonObject serializeJson(Map<String, String> result, Map<String, Object> queryParams) {
        JsonObject json = new JsonObject();

        String id = result.get("record_id");
        String title = result.containsKey("title") ? StringUtils.trim(result.get("title")) : "";
        String url = result.containsKey("url") ? StringUtils.trim(result.get("url")) : "";
        String textSnippet = result.containsKey("plain_text") ? StringUtils.trim(result.get("plain_text")) : "";
        if (result.get("q").length() > 0 && !result.get("q").equals("*:*")) {
            textSnippet = CoreUtils.extractTextSnippet(textSnippet, result.get("q").split(" "), result.get("index"), id, (boolean) queryParams.get("fulltext"));
        }
        String language = result.containsKey("language") ? StringUtils.trim(result.get("language")) : "";
        long warcDate = CoreUtils.convertWarcDateToEpoch(result.get("warc_date"));
        long wordCount = result.get("plain_text").split("\\s+").length;

        json.addProperty("id", id);
        json.addProperty("url", url);
        json.addProperty("title", title);
        json.addProperty("textSnippet", textSnippet);
        json.addProperty("language", language);
        json.addProperty("warcDate", warcDate);
        json.addProperty("wordCount", wordCount);

        return json;
    }

    @Override
    public String serializeXml(Map<String, String> result, Map<String, Object> queryParams) {
        String xml = "";
       
        String id = result.get("record_id");
        String title = result.containsKey("title") ? new UnicodeUnescaper().translate(StringEscapeUtils.escapeXml11(StringUtils.trim(result.get("title")))) : "";
        String url = result.containsKey("url") ? new UnicodeUnescaper().translate(StringEscapeUtils.escapeXml11(StringUtils.trim(result.get("url")))) : "";
        String textSnippet = result.containsKey("plain_text") ? StringUtils.trim(result.get("plain_text")) : "";
        if (result.get("q").length() > 0 && !result.get("q").equals("*:*")) {
            textSnippet = CoreUtils.extractTextSnippet(textSnippet, result.get("q").split(" "), result.get("index"), id, (boolean) queryParams.get("fulltext"));
        }
        textSnippet = new UnicodeUnescaper().translate(StringEscapeUtils.escapeXml11(StringUtils.trim(textSnippet.replace("\n", " "))));
        String language = result.containsKey("language") ? StringUtils.trim(result.get("language")) : "";
        long warcDate = CoreUtils.convertWarcDateToEpoch(result.get("warc_date"));
        long wordCount = result.get("plain_text").split("\\s+").length;

        xml += "<title>" + title + "</title>" +
               "<link>" + url + "</link>" +
               "<description>" + textSnippet + "</description>" +
               "<id>" + id + "</id>" +
               "<language>" + language + "</language>" +
               "<warcDate>" + warcDate + "</warcDate>" +
               "<wordCount>" + wordCount + "</wordCount>";

        return xml;
    }

}
