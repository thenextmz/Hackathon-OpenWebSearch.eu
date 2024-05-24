package eu.ows.mosaic;

import jakarta.ws.rs.WebApplicationException;

/**
 * Custom exception class for the MOSAIC search service.
 */
public class MosaicWebException extends WebApplicationException {

    public MosaicWebException(String message) {
        super(message);
    }

    public MosaicWebException(String message, Throwable cause) {
        super(message, cause);
    }

    public MosaicWebException(Throwable cause) {
        super(cause);
    }
}
