package eu.ows.mosaic;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Metadata module for the geo metadata.
 */
public class GeoMetadata extends MetadataModule {

    private static Logger LOGGER = LoggerFactory.getLogger(GeoMetadata.class);

    @Override
    public Set<String> getMetadataColumns() {
        return Set.of("locations");
    }

    @Override
    public Set<String> getFilterColumns() {
        return super.getFilterColumns();
    }

    @Override
    public void validateParams(Map<String, String> queryParams) {
        super.validateParams(queryParams);
    }

    @Override
    public Map<String, Object> parseQueryParams(Map<String, String> queryParams) {
        Map<String, Object> parsedParams = new TreeMap<>();

        if (queryParams.containsKey("east") && GeoUtils.isValidLongitude(queryParams.get("east")) &&
            queryParams.containsKey("west") && GeoUtils.isValidLongitude(queryParams.get("west")) &&
            queryParams.containsKey("north") && GeoUtils.isValidLatitude(queryParams.get("north")) &&
            queryParams.containsKey("south") && GeoUtils.isValidLatitude(queryParams.get("south"))) {
            
            parsedParams.put("east", queryParams.get("east"));
            parsedParams.put("west", queryParams.get("west"));
            parsedParams.put("north", queryParams.get("north"));
            parsedParams.put("south", queryParams.get("south"));

            String operatorValue = (queryParams.containsKey("operator")) ? queryParams.get("operator") : GeoUtils.DEFAULT_OPERATOR;
            parsedParams.put("operator", operatorValue);
        }

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
        BoundingBox bbox = null;
        if (queryParams.containsKey("east") && queryParams.containsKey("west") && 
            queryParams.containsKey("north") && queryParams.containsKey("south")) {
            bbox = new BoundingBox(
                GeoUtils.convertLongitude((String) queryParams.get("east")),
                GeoUtils.convertLongitude((String) queryParams.get("west")),
                GeoUtils.convertLatitude((String) queryParams.get("north")),
                GeoUtils.convertLatitude((String) queryParams.get("south")));
        }

        Locations locations = GeoUtils.parseLocations(result.get("locations"));
        String operator = (queryParams.containsKey("operator")) ? (String) queryParams.get("operator") : GeoUtils.DEFAULT_OPERATOR;
        return bbox == null || GeoUtils.locationsInBoundingBox(locations, bbox, operator);
    }

    @Override
    public JsonObject serializeJson(Map<String, String> result, Map<String, Object> queryParams) {
        JsonObject json = new JsonObject();

        Locations locations = (result.containsKey("locations")) ? GeoUtils.parseLocations(result.get("locations")) : new Locations();
    
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        JsonArray locationsArray = new JsonArray();
        try {
			locationsArray = new Gson().fromJson(ow.writeValueAsString(locations), JsonArray.class);
		} catch (JsonSyntaxException | JsonProcessingException e) {
            LOGGER.error("Error serializing locations to JSON", e);
		}

        json.add("locations", locationsArray);

        return json;
    }

    @Override
    public String serializeXml(Map<String, String> result, Map<String, Object> queryParams) {
        String xml = "";                

        Locations locations = (result.containsKey("locations")) ? GeoUtils.parseLocations(result.get("locations")) : new Locations();

        xml += "<locations>";
        for (Location location : locations) {
            xml += "<location>";
            xml += "<locationName>" + location.getLocationName() + "</locationName>";
            xml += "<locationEntries>";
            for (LocationEntry locationEntry : location.getLocationEntries()) {
                xml += "<locationEntry>";
                xml += "<latitude>" + locationEntry.getLatitude() + "</latitude>" +
                       "<longitude>" + locationEntry.getLongitude() + "</longitude>" + 
                       "<alpha2CountryCode>" + locationEntry.getAlpha2CountryCode() + "</alpha2CountryCode>";
                xml += "</locationEntry>";
            }
            xml += "</locationEntries>";
            xml += "</location>";
        }
        xml += "</locations>";

        return xml;
    }

}
