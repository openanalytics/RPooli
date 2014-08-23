
package eu.openanalytics.rpooli.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        else
        {
            LOGGER.error(t.getMessage(), t);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN)
                .entity(INTERNAL_SERVER_ERROR_PUBLIC_MESSAGE)
                .build();
        }
    }
}
