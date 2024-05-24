package eu.ows.mosaic;

/**
 * BoundingBox class to define the bounding box for the search service.
 */
public class BoundingBox {

    private float east;
    private float west;
    private float north;
    private float south;

    public BoundingBox(float east, float west, float north, float south) {
        this.east = east;
        this.west = west;
        this.north = north;
        this.south = south;
    }

    public float getEast() {
        return east;
    }

    public void setEast(float east) {
        this.east = east;
    }

    public float getWest() {
        return west;
    }

    public void setWest(float west) {
        this.west = west;
    }

    public float getNorth() {
        return north;
    }

    public void setNorth(float north) {
        this.north = north;
    }

    public float getSouth() {
        return south;
    }

    public void setSouth(float south) {
        this.south = south;
    }
    
}
