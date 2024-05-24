package eu.ows.mosaic;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** 
 * Mapper for exceptions that occur during the processing of a request.
 */
@Provider
public class ResponseExceptionMapper implements ExceptionMapper<Exception> {

    private static Logger LOGGER = LoggerFactory.getLogger(ResponseExceptionMapper.class);

    private final String ERROR_MESSAGE_NOT_FOUND = "This is MOSAIC search service. The requested resource could not be found.";

    /**
     * Returns a response object for an exception.
     */
    @Override
    public Response toResponse(Exception exception) {
        Response errorResponse = mapExceptionToResponse(exception);
        return errorResponse;
    }

    /**
     * Maps an exception to a response object.
     * The response object contains a JSON error message.
     * @param exception The exception to map.
     * @return The response object.
     */
    private Response mapExceptionToResponse(Exception exception) {
        if (exception instanceof NotFoundException) {
            LOGGER.error("Resource not found: {}", exception);
            return Response.status(404)
                    .entity(createJsonErrorContent(404, ERROR_MESSAGE_NOT_FOUND, ExceptionUtils.getStackTrace(exception)))
                    .type("application/json")
                    .build();
        } else if (exception instanceof WebApplicationException) {
            LOGGER.error("Failed to process request: {}", exception);
            Response originalErrorResponse = ((WebApplicationException) exception).getResponse();
            return Response.fromResponse(originalErrorResponse)
                    .entity(createJsonErrorContent(400, exception.getMessage(), ExceptionUtils.getStackTrace(exception)))
                    .type("application/json")
                    .status(400)
                    .build();
        } else {
            LOGGER.error("Internal server error caused by exception: {}", exception);
            return Response.serverError()
            .entity(createJsonErrorContent(500, exception.getMessage(), ExceptionUtils.getStackTrace(exception)))
            .type("application/json")
            .build();
        }
    }

    /**
     * Creates a JSON error message.
     * @param errorStatus The HTTP status code.
     * @param errorMessage The error message.
     * @param stackTrace The stack trace.
     * @return The JSON error message.
     */
    private String createJsonErrorContent(int errorStatus, String errorMessage, String stackTrace) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorObject = objectMapper.createObjectNode();
        errorObject.put("status", errorStatus);
        errorObject.put("title", errorMessage);

        if (stackTrace != null) {
            errorObject.put("detail", stackTrace);
        }

        return objectMapper.createObjectNode().set("error", errorObject).toPrettyString();
    }

}
