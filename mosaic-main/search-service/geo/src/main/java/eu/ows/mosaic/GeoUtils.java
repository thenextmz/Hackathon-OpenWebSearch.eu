package eu.ows.mosaic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Provides constants and helper methods with regards to the location module of the MOSAIC search service.
 */
public class GeoUtils {
    public static final String OPERATOR_OR = "or";
    public static final String OPERATOR_AND = "and";
    public static final String DEFAULT_OPERATOR = OPERATOR_OR;

    /**
     * Parses the locations from the JSON string.
     * @param locationsAsJson JSON string containing the locations
     * @return Locations object containing the parsed locations
     */
    public static Locations parseLocations(String locationsAsJson) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, List<List<Object>>>>>(){}.getType();
        List<Map<String, List<List<Object>>>> rawLocations = gson.fromJson(locationsAsJson, listType);

        Locations locations = new Locations();

        if (rawLocations != null) {

            for (Map<String,List<List<Object>>> locationMap : rawLocations) {
                for (Map.Entry<String, List<List<Object>>> locationMapEntry : locationMap.entrySet()) {
                    List<LocationEntry> locationEntries = new ArrayList<>();
                    float latitude = Float.parseFloat(String.valueOf(locationMapEntry.getValue().get(0).get(0)));
                    float longitude = Float.parseFloat(String.valueOf(locationMapEntry.getValue().get(0).get(1)));
                    String alpha2CountryCode = String.valueOf(locationMapEntry.getValue().get(0).get(2));

                    locationEntries.add(new LocationEntry(latitude, longitude, alpha2CountryCode));
                    locations.add(new Location(locationMapEntry.getKey(), locationEntries));

                    // Currently, only the first entry of a location is added used for filtering
                    /*
                    for (List<Object> locationsPerLocation : locationMapEntry.getValue()) {
                        float latitude = Float.parseFloat(String.valueOf(locationsPerLocation.get(0)));
                        float longitude = Float.parseFloat(String.valueOf(locationsPerLocation.get(1)));
                        String alpha2CountryCode = String.valueOf(locationsPerLocation.get(2));

                        locationEntries.add(new LocationEntry(latitude, longitude, alpha2CountryCode));
                    }
                    locations.add(new Location(locationMapEntry.getKey(), locationEntries));
                    */
                }
            }
        }

        return locations;
    }

    /**
     * Checks if the given string is a valid latitude.
     * @param latitudeAsString String representation of the latitude
     * @return True if the string is a valid latitude, false otherwise
     */
    public static boolean isValidLatitude(String latitudeAsString) {
        float latitude = NumberUtils.toFloat(latitudeAsString, Float.NaN);
        return latitude >= -90 && latitude <= 90;
    }

    /**
     * Checks if the given string is a valid longitude.
     * @param longitudeAsString String representation of the longitude
     * @return True if the string is a valid longitude, false otherwise
     */
    public static boolean isValidLongitude(String longitudeAsString) {
        float longitude = NumberUtils.toFloat(longitudeAsString, Float.NaN);
        return longitude >= -180 && longitude <= 180;
    }

    /**
     * Converts the given string to a float representing a latitude.
     * @param latitudeAsString String representation of the latitude
     * @return Float representing the latitude
     */
    public static float convertLatitude(String latitudeAsString) {
        return NumberUtils.toFloat(latitudeAsString, Float.NaN);
    }

    /**
     * Converts the given string to a float representing a longitude.
     * @param longitudeAsString String representation of the longitude
     * @return Float representing the longitude
     */
    public static float convertLongitude(String longitudeAsString) {
        return NumberUtils.toFloat(longitudeAsString, Float.NaN);
    }

    /**
     * Checks if the locations are within the given bounding box.
     * @param locations Locations to check
     * @param bbox Bounding box to check
     * @param operator Operator to use for the check
     * @return True if the locations are within the bounding box, false otherwise
     */
    public static boolean locationsInBoundingBox(Locations locations, BoundingBox bbox, String operator) {
        for (Location location : locations) {
            for (LocationEntry locationEntry : location.getLocationEntries()) {
                if (locationEntry.isBetweenWestEast(bbox) && locationEntry.isBetweenNorthSouth(bbox)) {
                    if (operator.equals(GeoUtils.OPERATOR_OR)) {
                        return true;
                    }
                } else if (operator.equals(GeoUtils.OPERATOR_AND)) {
                    return false;
                }
            }
        }

        return locations.isEmpty() || operator.equals(GeoUtils.OPERATOR_AND);
    }
}
