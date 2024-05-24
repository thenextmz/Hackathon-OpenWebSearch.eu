package eu.ows.mosaic;

/**
 * Location entry class to define one specific entry of a location.
 */
public class LocationEntry {

    private float latitude;
    private float longitude;
    private String alpha2CountryCode;
    
    public LocationEntry(float latitude, float longitude, String alpha2CountryCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.alpha2CountryCode = alpha2CountryCode;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getAlpha2CountryCode() {
        return alpha2CountryCode;
    }

    public void setAlpha2CountryCode(String alpha2CountryCode) {
        this.alpha2CountryCode = alpha2CountryCode;
    }

    /**
     * Check if the location is within the longitude area of the bounding box.
     * @param boundingBox The bounding box to check
     * @return True if the location entry is within the longitude area of the bounding box, false otherwise
     */
    public boolean isBetweenWestEast(BoundingBox boundingBox) {
        if (boundingBox.getWest() > boundingBox.getEast()) {
            return this.longitude >= boundingBox.getWest() || this.longitude <= boundingBox.getEast();
        }
        return this.longitude >= boundingBox.getWest() && this.longitude <= boundingBox.getEast();
    }

    /**
     * Check if the location is within the latitude area of the bounding box.
     * @param boundingBox The bounding box to check
     * @return True if the location entry is within the latitude area of the bounding box, false otherwise
     */
    public boolean isBetweenNorthSouth(BoundingBox boundingBox) {
        return latitude >= boundingBox.getSouth() && latitude <= boundingBox.getNorth();
    }
    
}
