
package eu.openanalytics.rpooli.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

import eu.openanalytics.rpooli.api.spec.model.ErrorJson;

public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
    private static final Log LOGGER = LogFactory.getLog(ThrowableExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable t)
    {
        if (t instanceof WebApplicationException)
        {
            return ((WebApplicationException) t).getResponse();
        }
        else if (t instanceof IllegalArgumentException)
        {
            LOGGER.warn("Received an invalid request (" + t.getMessage() + ")");

            return Response.status(BAD_REQUEST)
                .type(APPLICATION_JSON)
                .entity(new ErrorJson().withError(t.getMessage()))
                .build();
        }
        else if (t instanceof JsonMappingException)
        {
            LOGGER.warn("Received invalid JSON (" + t.getMessage() + ")");

            return Response.status(BAD_REQUEST)
                .type(APPLICATION_JSON)
                .entity(new ErrorJson().withError("Invalid JSON entity"))
                .build();
        }
        else
        {
            LOGGER.error(t.getMessage(), t);

            return Response.status(INTERNAL_SERVER_ERROR)
                .type(APPLICATION_JSON)
                .entity(
                    new ErrorJson().withError("Server-side guru meditation :( The issue has been logged."))
                .build();
        }
    }
}
