
package eu.openanalytics.rpooli.api;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
    public static final String INTERNAL_SERVER_ERROR_PUBLIC_MESSAGE = "Server-side guru meditation :( The issue has been logged.";
    private static final Log LOGGER = LogFactory.getLog(ThrowableExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable t)
    {
        if (t instanceof WebApplicationException)
        {
            return ((WebApplicationException) t).getResponse();
        }
        else if (t instanceof JsonMappingException)
        {
            return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity("Invalid JSON entity").build();
        }
        else
        {
            LOGGER.error(t.getMessage(), t);
            return Response.status(INTERNAL_SERVER_ERROR)
                .type(TEXT_PLAIN)
                .entity("Server-side guru meditation :( The issue has been logged.")
                .build();
        }
    }
}
