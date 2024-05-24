package eu.ows.mosaic;

import java.util.List;

/**
 * Location class to define the location object for the search service.
 */
public class Location {

    private String locationName;
    private List<LocationEntry> locationEntries;

    public Location(String locationName) {
        this.locationName = locationName;
    }

    public Location(String locationName, List<LocationEntry> locationEntries) {
        this.locationName = locationName;
        this.locationEntries = locationEntries;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<LocationEntry> getLocationEntries() {
        return locationEntries;
    }

    public void setLocationEntries(List<LocationEntry> locationEntries) {
        this.locationEntries = locationEntries;
    }

}
